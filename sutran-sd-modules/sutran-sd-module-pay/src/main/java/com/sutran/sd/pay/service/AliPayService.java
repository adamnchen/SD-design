package com.sutran.sd.pay.service;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ijpay.alipay.AliPayApiConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @author zj
 * @date 2025年08月18日 22:01
 */
public interface AliPayService {
    /**
     * 获取支付宝支付配置
     * @return 支付包配置
     */
    AliPayApiConfig getConfig();

    /**
     * 预创建订单
     * @param memberId 会员ID
     * @return 支付二维码
     */
    String preCreateOrder(String memberId);

    /**
     * 支付回调通知
     * @param request 支付回调请求
     * @return 通知地址
     */
    String notifyUrl(HttpServletRequest request);

    /**
     * 查询支付宝指定交易信息
     * @param outTradeNo 订单号
     * @param tradeNo 交易号
     */
    void syncStatus(String outTradeNo, String tradeNo);

    /**
     * 支付宝退款
     * @param outTradeNo 商户订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    boolean refund(String outTradeNo, BigDecimal refundAmount, String refundReason);
}
