package com.sutran.sd.pay.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author zj
 * @date 2025年07月28日 10:50
 */
public interface WxPayService {

    /**
     * 湖区哦平台证书列表
     */
    String v3Get();

    /**
     * 保存平台证书
     * @param associatedData 关联数据
     * @param nonce 随机数
     * @param cipherText 密文
     * @param algorithm 算法
     * @return 证书序列号
     */
    String savePlatformCert(String associatedData, String nonce, String cipherText, String algorithm);

    /**
     * jsapi支付
     * @param openId    openId
     */
    Map<String, String> jsApiPay(String openId);

    /**
     * 支付回调
     * @param request   请求参数
     * @param response  响应数据
     */
    void payNotify(HttpServletRequest request, HttpServletResponse response);

    /**
     * 退款
     * @param outTradeNo 交易流水ID
     * @param outTradeNo 商户订单号
     * @return 退款订单号
     */
    String refund(String transactionId, String outTradeNo);

    /**
     * 退款回调
     * @param request   请求参数
     * @param response  响应数据
     */
    void refundNotify(HttpServletRequest request, HttpServletResponse response);

}
