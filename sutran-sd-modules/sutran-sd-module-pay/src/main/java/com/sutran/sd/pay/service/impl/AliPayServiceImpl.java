package com.sutran.sd.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.*;
import com.alipay.api.response.*;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.OrderNumUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.pay.config.AliPayConfig;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.enums.BusinessType;
import com.sutran.sd.pay.enums.ChannelType;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Date;

import static com.sutran.sd.common.constant.CacheConstants.PAY_ORDER_QR;
import static com.sutran.sd.common.constant.CacheConstants.PAY_ORDER_TASK;

/**
 * @author zj
 * @date 2025年08月18日 22:01
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class AliPayServiceImpl implements AliPayService {

    private final AliPayConfig aliPayConfig;
    private final PayOrderService payOrderService;

    @Override
    public AliPayApiConfig getConfig() {
        AliPayApiConfig aliPayApiConfig;
        try {
            aliPayApiConfig = AliPayApiConfigKit.getApiConfig(aliPayConfig.getAppId());
        }
        catch (Exception e) {
            try {
                aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(aliPayConfig.getAppId())
                    .setAliPayPublicKey(aliPayConfig.getPublicKey())
                    .setPrivateKey(aliPayConfig.getPrivateKey())
                    .setAppCertPath(aliPayConfig.getAppCertPath())
                    .setAliPayCertPath(aliPayConfig.getAliPayCertPath())
                    .setAliPayRootCertPath(aliPayConfig.getAliPayRootCertPath())
                    .setCharset("UTF-8")
                    .setServiceUrl(aliPayConfig.getServerUrl())
                    .setSignType("RSA2")
                    // 证书模式
                    .buildByCert();
            } catch (AlipayApiException ex) {
                throw new RuntimeException(ex);
            }
            AliPayApiConfigKit.setThreadLocalAliPayApiConfig(aliPayApiConfig);
        }
        return aliPayApiConfig;
    }

    /**
     * 手动同步支付宝指定订单支付状态
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 交易流水号（和outTradeNo二选一）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncOrderStatus(String outTradeNo, String tradeNo) {
        AlipayTradeQueryResponse response = tradeQuery(outTradeNo, tradeNo);
        payOrderService.dealOrderBySyncStatus(response,outTradeNo,tradeNo);
    }

    /**
     * 创建会员支付订单
     * @param userId 下单人用户ID
     * @param userName 下单人姓名
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     * @param memberId (会员ID
     * @return 支付二维码
     */
    @Override
    public String createMemberPayOrder(Long userId, String userName, String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl, Long memberId) {
        // 支付宝应用ID
        final String appId = aliPayConfig.getAppId();

        // 获取当前用户在当前支付应用下是否存在未超时且未完成的支付的会员订单
        PayOrder payOrder = payOrderService.isExistNoDealOrder(userId,appId,memberId);
        if (payOrder != null && StringUtils.isNotBlank(payOrder.getQrCode())) {
            return payOrder.getQrCode();
        }
        else if (payOrder != null) {
            // 删除没有qrCode的订单
            payOrderService.deleteById(payOrder.getId());
            // 删除缓存
            RedisUtils.deleteKey(PAY_ORDER_QR+outTradeNo);
            // 移除缓存中的订单
            RedisUtils.delCacheZSet(PAY_ORDER_TASK,outTradeNo);
        }

        // 订单过期时间，11分钟后过期(稍微大于支付认超时时间)
        Date now = new Date();
        Date expireTime = DateUtil.offsetMinute(now, 11);

        // 存入redis,扫描redis进行过期订单处理
        RedisUtils.setCacheZSet(PAY_ORDER_TASK,expireTime.getTime(),outTradeNo);

        // 新增订单记录
        PayOrder order = new PayOrder()
            .setOutTradeNo(outTradeNo).setUserId(userId).setUserName(userName)
            .setAppId(appId).setSubject(subject).setBody(body)
            .setTotalAmount(totalAmount).setStatus(0)
            .setChannelType(ChannelType.ALI_PAY.name())
            .setBusinessType(BusinessType.SD_MEMBER.name())
            .setBusinessId(memberId).setCreateTime(now).setExpireTime(expireTime);
        payOrderService.insert(order);
        try {
            return tradePrecreatePay(outTradeNo, subject, body, totalAmount, notifyUrl);
        }
        catch (Exception e) {
            log.error("[支付宝][扫码支付]>>>>>>>>>创建订单失败,订单号:{}，异常：", outTradeNo, e);
            payOrderService.failPay(outTradeNo,null, totalAmount.setScale(2, RoundingMode.HALF_UP).toString());
            throw new ServiceException("创建订单失败:"+e.getMessage(),500);
        }
    }

    /**
     * 创建订单记录->调用支付宝创建订单->保存二维码到数据库和redis中
     * @param userId 下单人用户ID
     * @param userName 下单人姓名
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     */
    @Override
    public void createPayOrder(Long userId, String userName, String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl) {
        // 支付宝应用ID
        final String appId = aliPayConfig.getAppId();

        // 订单过期时间，5分钟后过期(稍微大于支付认超时时间)
        Date now = new Date();
        Date expireTime = DateUtil.offsetMinute(now, 6);

        // 存入redis,扫描redis进行过期订单处理
        RedisUtils.setCacheZSet(PAY_ORDER_TASK,expireTime.getTime(),outTradeNo);

        // 新增订单记录
        PayOrder order = new PayOrder()
            .setOutTradeNo(outTradeNo)
            .setUserId(userId).setUserName(userName).setAppId(appId)
            .setSubject(subject).setBody(body).setTotalAmount(totalAmount)
            .setChannelType(ChannelType.ALI_PAY.name()).setBusinessType(BusinessType.PROOF_CROWDFUND.name())
            .setStatus(0).setCreateTime(now).setExpireTime(expireTime);
        payOrderService.insert(order);

        try {
            tradePrecreatePay(outTradeNo, subject, body, totalAmount, notifyUrl);
        }
        catch (Exception e) {
            log.error("[支付宝][扫码支付]>>>>>>>>>创建订单失败,订单号:{}，异常：", outTradeNo, e);
            payOrderService.failPay(outTradeNo,null, totalAmount.setScale(2, RoundingMode.HALF_UP).toString());
            throw new ServiceException("创建订单失败:"+e.getMessage(),500);
        }
    }

    /**
     * 更新订单状态
     * @param outTradeNo 订单号
     * @param tradeNo 支付宝交易流水号
     * @param totalAmount 总金额
     * @param gmtPayment 支付时间
     * @param payStatus 支付状态[0-待支付,1-支付成功,2-支付失败]
     * @return 是否修改成功
     */
    @Override
    public boolean updateOrderStatus(String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Integer payStatus) {
        if (payStatus == null) {
            return false;
        }
        else if (payStatus == 1) {
            return payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);
        }
        else {
            return payOrderService.failPay(outTradeNo,tradeNo,totalAmount);
        }
    }


    /**
     * 单笔转账到支付宝账户
     * @param payeeAccount 收款方账号
     * @param amount 转账金额
     * @param payerShowName 付款方显示名称
     * @param payerRealName 付款方真实姓名
     * @param remark 转账备注
     */
    @Override
    public void transfer(String payeeAccount, String amount, String payerShowName, String payerRealName, String remark) {
        getConfig();
        // 获取订单号
        String outTradeNo = OrderNumUtils.getOrderNum(new Date());

        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        // 业务订单号
        model.setOutBizNo(outTradeNo);
        // 收款方类型，固定值ALIPAY_LOGONID
        model.setPayeeType("ALIPAY_LOGONID");
        // 收款方账号
        model.setPayeeAccount(payeeAccount);
        // 转账金额，单位为元，精确到小数点后两位
        model.setAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).toString());
        // 付款方显示名称
        model.setPayerShowName(payerShowName);
        // 付款方真实姓名
        model.setPayerRealName(payerRealName);
        // 转账备注
        model.setRemark(remark);
        try {
            AlipayFundTransToaccountTransferResponse transferResponse = AliPayApi.transferToResponse(model);
        }
        catch (Exception e) {
            log.error("[支付宝][单笔转账到支付宝账户]>>>>>>>>>转账失败,订单号:{},收款方账号:{},转账金额:{},异常：", outTradeNo, payeeAccount, amount, e);
        }
    }


    /**
     * 查询单笔转账到支付宝账户结果
     * @param outTradeNo 业务订单号（和orderId二选一）
     * @param orderId 支付宝转账订单号（和outTradeNo二选一）
     */
    @Override
    public void transferQuery(String outTradeNo, String orderId) {
        getConfig();
        AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
        if (StringUtils.isNotEmpty(outTradeNo)) {
            model.setOutBizNo(outTradeNo);
        }
        if (StringUtils.isNotEmpty(orderId)) {
            model.setOrderId(orderId);
        }

        try {
            AlipayFundTransOrderQueryResponse queryResponse = AliPayApi.transferQueryToResponse(model);
        }
        catch (Exception e) {
            log.error("[支付宝][查询单笔转账到支付宝账户结果]>>>>>>>>>查询失败,订单号:{},支付宝转账订单号:{},异常：", outTradeNo, orderId, e);
        }
    }

    @Override
    public void uniTransfer(String payeeAccount, String amount, String payeeName, String remark) {
        getConfig();
        // 获取订单号
        String outTradeNo = OrderNumUtils.getOrderNum(new Date());

        AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
        // 业务订单号
        model.setOutBizNo(outTradeNo);
        // 转账金额，单位为元，精确到小数点后两位
        model.setTransAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).toString());
        // 产品码，固定值TRANS_ACCOUNT_NO_PWD
        model.setProductCode("TRANS_ACCOUNT_NO_PWD");
        // 业务场景，固定值DIRECT_TRANSFER
        model.setBizScene("DIRECT_TRANSFER");
        // 订单标题
        model.setOrderTitle("统一转账-转账至支付宝账户");
        // 转账备注
        model.setRemark(remark);
        // 收款方信息
        Participant payeeInfo = new Participant();
        // 收款方账号
        payeeInfo.setIdentity(payeeAccount);
        // 收款方账号类型，固定值ALIPAY_LOGON_ID
        payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
        // 收款方姓名
        payeeInfo.setName(payeeName);
        model.setPayeeInfo(payeeInfo);

        try {
            AlipayFundTransUniTransferResponse uniTransferToResponse = AliPayApi.uniTransferToResponse(model, null);
        }
        catch (Exception e) {
            log.error("[支付宝][统一转账到支付宝账户]>>>>>>>>>转账失败,订单号:{},收款方账号:{},转账金额:{},异常：", outTradeNo, payeeAccount, amount, e);
        }
    }

    /**
     * 查询统一转账到支付宝账户结果
     * @param outTradeNo 业务订单号（和orderId二选一）
     * @param orderId 支付宝转账订单号（和outTradeNo二选一）
     */
    @Override
    public void uniTransferQuery(String outTradeNo, String orderId) {
        getConfig();

        AlipayFundTransCommonQueryModel model = new AlipayFundTransCommonQueryModel();
        if (StringUtils.isNotEmpty(outTradeNo)) {
            model.setOutBizNo(outTradeNo);
        }
        if (StringUtils.isNotEmpty(orderId)) {
            model.setOrderId(orderId);
        }

        try {
            AlipayFundTransCommonQueryResponse uniQueryToResponse = AliPayApi.transCommonQueryToResponse(model, null);
        }
        catch (Exception e) {
            log.error("[支付宝][查询统一转账到支付宝账户结果]>>>>>>>>>查询失败,订单号:{},支付宝转账订单号:{},异常：", outTradeNo, orderId, e);
        }
    }

     /**
      * 查询支付宝账户详情
      * @param aliPayUserId 支付宝用户ID
      */
    @Override
    public void accountQuery(String aliPayUserId) {
        getConfig();

        AlipayFundAccountQueryModel model = new AlipayFundAccountQueryModel();
        // 支付宝用户ID
        model.setAlipayUserId(aliPayUserId);
        // 账户类型，固定值ACCTRANS_ACCOUNT
        model.setAccountType("ACCTRANS_ACCOUNT");
        try {
            AlipayFundAccountQueryResponse accounted = AliPayApi.accountQueryToResponse(model, null);
        }
        catch (Exception e) {
            log.error("[支付宝][查询支付宝账户]>>>>>>>>>查询失败,支付宝用户ID:{},异常：", aliPayUserId, e);
        }
    }

    /**
     * 支付宝交易退款
     * @param outTradeNo 订单号
     * @param tradeNo 支付宝交易流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     */
    @Override
    public void tradeRefund(String outTradeNo, String tradeNo, String refundAmount, String refundReason) {
        getConfig();

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        // 订单号
        model.setOutTradeNo(outTradeNo);
        // 支付宝交易流水号
        model.setTradeNo(tradeNo);
        // 退款金额
        model.setRefundAmount(refundAmount);
        // 退款原因
        model.setRefundReason(refundReason);
        try {
            AlipayTradeRefundResponse refundToResponse = AliPayApi.tradeRefundToResponse(model, null);
            //TODO 退款成功，需要修改订单状态
            if (refundToResponse.isSuccess()) {

            }
            else {
                log.error("[支付宝][交易退款]>>>>>>>>>退款失败,订单号:{},支付宝交易流水号:{},退款金额:{},退款原因:{},失败原因：{}", outTradeNo, tradeNo, refundAmount, refundReason, refundToResponse.getSubMsg());
            }
        }
        catch (Exception e) {
            log.error("[支付宝][交易退款]>>>>>>>>>退款失败,订单号:{},支付宝交易流水号:{},退款金额:{},退款原因:{},异常原因：", outTradeNo, tradeNo, refundAmount, refundReason, e);
        }
    }

    /**
     * 查询支付宝交易详情
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 支付宝交易流水号（和outTradeNo二选一）
     * @return 交易详情
     */
    @Override
    public AlipayTradeQueryResponse tradeQuery(String outTradeNo, String tradeNo) {
        getConfig();

        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        if (StringUtils.isNotBlank(outTradeNo)) {
            model.setOutTradeNo(outTradeNo);
        }
        if (StringUtils.isNotBlank(tradeNo)) {
            model.setTradeNo(tradeNo);
        }
        try {
            return AliPayApi.tradeQueryToResponse(model);
        }
        catch (AlipayApiException e) {
            log.error("[支付宝][查询指定交易信息]>>>>>>>>>查询支付宝指定交易信息失败,订单号：{},流水号：{},异常：",outTradeNo,tradeNo,e);
            throw new ServiceException("查询支付宝指定交易信息失败:"+e.getMessage());
        }
    }

     /**
      * 创建扫码支付订单
      * @param outTradeNo 订单号
      * @param subject 商品名称
      * @param body 商品参数或者描述信息(可以用json字符串表示)
      * @param totalAmount 订单总金额
      * @param notifyUrl 支付结果回调接口
      * @return 支付二维码
      */
    @Override
    public String tradePrecreatePay(String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl) {
        getConfig();

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(subject);
        model.setBody(body);
        // 订单总金额，单位为元，精确到小数点后两位
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP).toString());
        // 商户网站 唯一 订单号0
        model.setOutTradeNo(outTradeNo);
        // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：5m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m
        model.setTimeoutExpress("5m");
        try {
            // 支付宝调用回调接口(系统上的接口，用于接收支付宝支付结果通知，要设置不需要登录认证)
            AlipayTradePrecreateResponse response = AliPayApi.tradePrecreatePayToResponse(model, aliPayConfig.getDomain()+notifyUrl);
            if (!response.isSuccess()) {
                throw new ServiceException(response.getSubMsg());
            }
            String qrCode = response.getQrCode();
            // 保存二维码到订单记录
            payOrderService.saveQrCode(outTradeNo,qrCode);
            // redis中存储二维码地址，并设置有效时间6分钟
            RedisUtils.setCacheObject(PAY_ORDER_QR+outTradeNo,qrCode, Duration.ofMinutes(6));
            return qrCode;
        }
        catch (Exception e) {
            log.error("[支付宝][扫码支付]>>>>>>>>>创建订单失败,订单号:{}，异常：", outTradeNo, e);
            throw new ServiceException("创建支付宝扫码支付订单失败:"+e.getMessage());
        }
    }

}
