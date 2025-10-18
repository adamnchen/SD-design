package com.sutran.sd.pay.service;

import com.ijpay.alipay.AliPayApiConfig;

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
     * 预创建会员购买订单
     * @param memberId 会员ID
     * @return 支付二维码
     */
    String preCreateMemberOrder(String memberId);

    /**
     * 查询支付宝指定交易信息
     * @param outTradeNo 订单号
     * @param tradeNo 交易号
     */
    void syncStatus(String outTradeNo, String tradeNo);

    /**
     * 创建支付订单
     * @param userId 下单人用户ID
     * @param userName 下单人姓名
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     */
    void createPayOrder(Long userId, String userName, String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl);

    /**
     * 更新订单状态
     * @param outTradeNo 订单号
     * @param tradeNo 支付宝交易流水号
     * @param totalAmount 总金额
     * @param gmtPayment 支付时间
     * @param payStatus 支付状态[0-待支付,1-支付成功,2-支付失败]
     * @return 是否修改成功
     */
    boolean updateOrderStatus(String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Integer payStatus);
}
