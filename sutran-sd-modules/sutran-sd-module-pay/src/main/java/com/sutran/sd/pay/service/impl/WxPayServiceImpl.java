package com.sutran.sd.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.AuthTypeEnum;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.AesUtil;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.Apply4SubApiEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.enums.v3.CertAlgorithmTypeEnum;
import com.ijpay.wxpay.model.v3.*;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.pay.config.WxPayConfig;
import com.sutran.sd.pay.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ijpay.core.utils.DateTimeZoneUtil.dateToTimeZone;

/**
 * @author zj
 * @date 2025年07月28日 11:43
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayServiceImpl implements WxPayService {
    private final WxPayConfig wxPayConfig;

    /**
     * 获取平台证书列表
     */
    @Override
    public String v3Get() {
        try {
            IJPayHttpResponse response = WxPayApi.v3(
                RequestMethodEnum.GET,
                WxDomainEnum.CHINA.toString(),
                CertAlgorithmTypeEnum.getCertSuffixUrl(CertAlgorithmTypeEnum.ALL.getCode()),
                wxPayConfig.getMchId(),
                getSerialNumber(),
                null,
                wxPayConfig.getKeyPath(),
                "",
                AuthTypeEnum.RSA.getCode()
            );
            Map<String, List<String>> headers = response.getHeaders();
            String timestamp = response.getHeader("Wechatpay-Timestamp");
            String nonceStr = response.getHeader("Wechatpay-Nonce");
            String serialNumber = response.getHeader("Wechatpay-Serial");
            String signature = response.getHeader("Wechatpay-Signature");

            String body = response.getBody();
            int status = response.getStatus();

            log.info("serialNumber: {}", serialNumber);
            log.info("status: {}", status);
            log.info("body: {}", body);
            int isOk = 200;
            if (status == isOk) {
                JSONObject jsonObject = JSONUtil.parseObj(body);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                // 默认认为只有一个平台证书
                JSONObject encryptObject = dataArray.getJSONObject(0);
                JSONObject encryptCertificate = encryptObject.getJSONObject("encrypt_certificate");
                String associatedData = encryptCertificate.getStr("associated_data");
                String cipherText = encryptCertificate.getStr("ciphertext");
                String nonce = encryptCertificate.getStr("nonce");
                String algorithm = encryptCertificate.getStr("algorithm");
                String serialNo = encryptObject.getStr("serial_no");
                final String platSerialNo = savePlatformCert(associatedData, nonce, cipherText, algorithm);
                log.info("平台证书序列号: {} serialNo: {}", platSerialNo, serialNo);
                // 根据证书序列号查询对应的证书来验证签名结果
                boolean verifySignature = WxPayKit.verifySignature(response, wxPayConfig.getPlatformCertPath());
                log.info("verifySignature:{}", verifySignature);
            }
            return body;
        } catch (Exception e) {
            log.error("获取平台证书列表异常", e);
            return null;
        }
    }

    /**
     * 保存平台证书
     * @param associatedData 关联数据
     * @param nonce 随机数
     * @param cipherText 密文
     * @param algorithm 算法
     * @return 证书序列号
     */
    @Override
    public String savePlatformCert(String associatedData, String nonce, String cipherText, String algorithm) {
        try {
            String key3 = wxPayConfig.getApiKey3();
            String publicKey;
            if (StrUtil.equals(algorithm, AuthTypeEnum.SM2.getPlatformCertAlgorithm())) {
                publicKey = PayKit.sm4DecryptToString(key3, cipherText, nonce, associatedData);
            } else {
                AesUtil aesUtil = new AesUtil(wxPayConfig.getApiKey3().getBytes(StandardCharsets.UTF_8));
                // 平台证书密文解密
                // encrypt_certificate 中的  associated_data nonce  ciphertext
                publicKey = aesUtil.decryptToString(
                    associatedData.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    cipherText
                );
            }
            if (StrUtil.isNotEmpty(publicKey)) {
                // 保存证书
                FileWriter writer = new FileWriter(wxPayConfig.getPlatformCertPath());
                writer.write(publicKey);
                // 获取平台证书序列号
                X509Certificate certificate = PayKit.getCertificate(new ByteArrayInputStream(publicKey.getBytes()));
                return certificate.getSerialNumber().toString(16).toUpperCase();
            }
            return "";
        } catch (Exception e) {
            log.error("保存平台证书异常", e);
            return e.getMessage();
        }
    }

    public String sensitive() {
        // 带有敏感信息接口
        try {
            String body = "处理请求参数";

            IJPayHttpResponse result = WxPayApi.v3(
                RequestMethodEnum.POST,
                WxDomainEnum.CHINA.toString(),
                Apply4SubApiEnum.APPLY_4_SUB.toString(),
                wxPayConfig.getMchId(),
                getSerialNumber(),
                getPlatSerialNumber(),
                wxPayConfig.getKeyPath(),
                body
            );
            System.out.println(result);
            return JSONUtil.toJsonStr(result);
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
    }

    /**
     * jsapi支付
     * @param openId    openId
     * @return 支付参数
     */
    @Override
    public Map<String, String> jsApiPay(String openId) {
        try {
            String timeExpire = dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                // APPID
                .setAppid(wxPayConfig.getAppId())
                // 商户号
                .setMchid(wxPayConfig.getMchId())
                .setDescription("IJPay 让支付触手可及")
                .setOut_trade_no(PayKit.generateStr())
                .setTime_expire(timeExpire)
                .setAttach("微信系开发脚手架 https://gitee.com/javen205/TNWX")
                .setNotify_url("")
                .setAmount(new Amount().setTotal(1))
                .setPayer(new Payer().setOpenid(openId));

            log.info("统一下单参数 {}", JSONUtil.toJsonStr(unifiedOrderModel));
            IJPayHttpResponse response = WxPayApi.v3(
                RequestMethodEnum.POST,
                WxDomainEnum.CHINA.toString(),
                BasePayApiEnum.JS_API_PAY.toString(),
                wxPayConfig.getMchId(),
                getSerialNumber(),
                null,
                wxPayConfig.getKeyPath(),
                JSONUtil.toJsonStr(unifiedOrderModel)
            );
            log.info("统一下单响应 {}", response);
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayConfig.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            if (response.getStatus() == HttpStatus.HTTP_OK && verifySignature) {
                String body = response.getBody();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String prepayId = jsonObject.getStr("prepay_id");
                Map<String, String> map = WxPayKit.jsApiCreateSign(wxPayConfig.getAppId(), prepayId, wxPayConfig.getKeyPath());
                log.info("唤起支付参数:{}", map);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new ServiceException("下单支付失败!");
    }

    /**
     * 微信支付回调
     * @param request       请求参数
     * @param response      响应数据
     */
    @Override
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>(12);
        try {
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String serialNo = request.getHeader("Wechatpay-Serial");
            String signature = request.getHeader("Wechatpay-Signature");

            log.info("timestamp:{} nonce:{} serialNo:{} signature:{}", timestamp, nonce, serialNo, signature);
            String result = HttpKit.readData(request);
            log.info("支付通知密文 {}", result);
            String plainText = null;
            // 需要通过证书序列号查找对应的证书，verifyNotify 中有验证证书的序列号
            // 微信公钥验证签名并解密
            if (StringUtils.equals(serialNo, wxPayConfig.getPublicKeyId())) {
                plainText = WxPayKit.verifyPublicKeyNotify(result, signature, nonce, timestamp,
                    wxPayConfig.getApiKey3(), wxPayConfig.getPlatformCertPath());
            }
            log.info("支付通知明文 {}", plainText);

            if (StrUtil.isNotEmpty(plainText)) {
                response.setStatus(200);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
            } else {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "签名错误");
            }
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            log.error("系统异常", e);
        }
    }

    /**
     * 退款
     * @param transactionId 交易流水ID
     * @param outTradeNo 商户订单号
     * @return 退款订单号
     */
    @Override
    public String refund(String transactionId, String outTradeNo) {
        try {
            String outRefundNo = PayKit.generateStr();
            log.info("商户退款单号: {}", outRefundNo);

            List<RefundGoodsDetail> list = new ArrayList<>();
            RefundGoodsDetail refundGoodsDetail = new RefundGoodsDetail()
                .setMerchant_goods_id("123")
                .setGoods_name("IJPay 测试")
                .setUnit_price(1)
                .setRefund_amount(1)
                .setRefund_quantity(1);
            list.add(refundGoodsDetail);

            RefundModel refundModel = new RefundModel()
                .setOut_refund_no(outRefundNo)
                .setReason("IJPay 测试退款")
                .setNotify_url(wxPayConfig.getDomain().concat("/v3/refundNotify"))
                .setAmount(new RefundAmount().setRefund(1).setTotal(1).setCurrency("CNY"))
                .setGoods_detail(list);

            if (StrUtil.isNotEmpty(transactionId)) {
                refundModel.setTransaction_id(transactionId);
            }
            if (StrUtil.isNotEmpty(outTradeNo)) {
                refundModel.setOut_trade_no(outTradeNo);
            }
            log.info("退款参数 {}", JSONUtil.toJsonStr(refundModel));
            IJPayHttpResponse response = WxPayApi.v3(
                RequestMethodEnum.POST,
                WxDomainEnum.CHINA.toString(),
                BasePayApiEnum.REFUND.toString(),
                wxPayConfig.getMchId(),
                getSerialNumber(),
                null,
                wxPayConfig.getKeyPath(),
                JSONUtil.toJsonStr(refundModel)
            );
            // 根据证书序列号查询对应的证书来验证签名结果
            // 微信支付公钥验证签名
            boolean verifySignature = WxPayKit.verifyPublicKeySignature(response, wxPayConfig.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            log.info("退款响应 {}", response);

            if (verifySignature) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
        return null;
    }

    /**
     * 退款回调
     * @param request   请求参数
     * @param response  响应数据
     */
    @Override
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>(12);
        try {
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String serialNo = request.getHeader("Wechatpay-Serial");
            String signature = request.getHeader("Wechatpay-Signature");

            log.info("退款通知 timestamp:{} nonce:{} serialNo:{} signature:{}", timestamp, nonce, serialNo, signature);
            String result = HttpKit.readData(request);
            log.info("退款通知密文 {}", result);
            String plainText = null;
            // 需要通过证书序列号查找对应的证书，verifyNotify 中有验证证书的序列号
            // 微信公钥验证签名并解密
            if (StringUtils.equals(serialNo, wxPayConfig.getPublicKeyId())) {
                plainText = WxPayKit.verifyPublicKeyNotify(result, signature, nonce, timestamp, wxPayConfig.getApiKey3(), wxPayConfig.getPlatformCertPath());
            }
            log.info("退款通知明文 {}", plainText);

            if (StrUtil.isNotEmpty(plainText)) {
                response.setStatus(200);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
            } else {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "签名错误");
            }
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            log.error("系统异常", e);
        }
    }

    /**
     * 获取证书序列号
     * @return 证书序列号
     */
    private String getSerialNumber() {
        // 获取证书序列号
        X509Certificate certificate = PayKit.getCertificate(wxPayConfig.getCertPath());
        if (null != certificate) {
            String serialNo = certificate.getSerialNumber().toString(16).toUpperCase();
            // 提前两天检查证书是否有效
            boolean isValid = PayKit.checkCertificateIsValid(certificate, wxPayConfig.getMchId(), -2);
            log.info("证书是否可用 {} 证书有效期为 {}", isValid, DateUtil.format(certificate.getNotAfter(), DatePattern.NORM_DATETIME_PATTERN));
            System.out.println("serialNo:" + serialNo);
            return serialNo;
        }
        return null;
    }

    /**
     * 获取平台证书序列号
     * @return 平台证书序列号
     */
    private String getPlatSerialNumber() {
        // 获取平台证书序列号
        X509Certificate certificate = PayKit.getCertificate(FileUtil.getInputStream(wxPayConfig.getPlatformCertPath()));
        return certificate.getSerialNumber().toString(16).toUpperCase();
    }

}
