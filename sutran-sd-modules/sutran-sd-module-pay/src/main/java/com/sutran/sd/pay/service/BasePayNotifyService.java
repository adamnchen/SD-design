package com.sutran.sd.pay.service;

import com.alipay.api.internal.util.AlipaySignature;
import com.ijpay.alipay.AliPayApi;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付回调抽象类
 * @author zj
 * @date 2025年10月18日 11:26
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BasePayNotifyService {

    /**
     * 处理支付回调
     *
     * @param request 支付回调请求
     * @return 处理结果
     */
    public String handleNotify(HttpServletRequest request,String aliPayCertPath) {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            // 商户订单号
            String outTradeNo = params.get("out_trade_no");
            // 支付宝交易流水号
            String tradeNo = params.get("trade_no");
            boolean verifyResult = AlipaySignature.rsaCertCheckV1(params, aliPayCertPath, "UTF-8", "RSA2");
            if (verifyResult) {
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 实际支付金额
                String totalAmount = params.get("total_amount");
                // 支付时间
                String gmtPayment = params.get("gmt_payment");
                return handleBusiness(tradeStatus, outTradeNo, tradeNo, totalAmount, gmtPayment);
            }
            else {
                log.error("[支付宝][支付回调验证]>>>>>>>>>支付回调验证失败,订单号：{},流水号：{}",outTradeNo,tradeNo);
                return "failure";
            }
        }
        catch (Exception e) {
            log.error("[支付宝][支付回调验证]>>>>>>>>>支付结果回调异常:",e);
            return "failure";
        }
    }

    /**
     * 处理业务逻辑
     *
     * @param tradeStatus 交易状态
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易流水号
     * @param totalAmount 实际支付金额
     * @param gmtPayment  支付时间
     * @return 处理结果
     */
    abstract public String handleBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment);

    /**
     * 消费支付模块超时回调数据
     * @param vo 支付订单超时参数
     */
    abstract public void dealPayTimeoutData(PayTimeoutStatusVo vo);
}
