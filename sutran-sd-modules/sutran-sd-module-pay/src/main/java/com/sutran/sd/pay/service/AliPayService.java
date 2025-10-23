package com.sutran.sd.pay.service;

import com.alipay.api.response.AlipayTradeQueryResponse;
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
     * 手动同步支付宝指定订单支付状态
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 交易流水号（和outTradeNo二选一）
     */
    void syncOrderStatus(String outTradeNo, String tradeNo);

    /**
     * 创建会员支付订单
     * @param userId 下单人用户ID
     * @param userName 下单人姓名
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     * @param businessId 业务ID(会员ID)
     * @return 支付二维码
     */
    String createMemberPayOrder(Long userId, String userName, String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl, Long businessId);

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

    /**
     * 单笔转账到支付宝账户
     * @param payeeAccount 收款方账号
     * @param amount 转账金额
     * @param payerShowName 付款方显示名称
     * @param payerRealName 付款方真实姓名
     * @param remark 转账备注
     */
    void transfer(String payeeAccount, String amount, String payerShowName, String payerRealName, String remark);

    /**
     * 查询单笔转账到支付宝账户结果
     * @param outTradeNo 业务订单号（和orderId二选一）
     * @param orderId 支付宝转账订单号（和outTradeNo二选一）
     */
    void transferQuery(String outTradeNo, String orderId);

    /**
     * 统一转账到支付宝账户
     * @param payeeAccount 收款方账号
     * @param amount 转账金额
     * @param payeeName 付款方姓名
     * @param remark 转账备注
     */
    void uniTransfer(String payeeAccount, String amount, String payeeName, String remark);

    /**
     * 查询统一转账到支付宝账户结果
     * @param outTradeNo 业务订单号（和orderId二选一）
     * @param orderId 支付宝转账订单号（和outTradeNo二选一）
     */
    void uniTransferQuery(String outTradeNo, String orderId);

    /**
     * 查询支付宝账户详情
     * @param aliPayUserId 支付宝用户ID
     */
    void accountQuery(String aliPayUserId);

    /**
     * 支付宝交易退款
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 支付宝交易流水号（和outTradeNo二选一）
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     */
    void tradeRefund(String outTradeNo, String tradeNo, String refundAmount, String refundReason);

    /**
     * 查询支付宝交易详情
     * @param outTradeNo 订单号（和tradeNo二选一）
     * @param tradeNo 支付宝交易流水号（和outTradeNo二选一）
     * @return 交易详情
     */
    AlipayTradeQueryResponse tradeQuery(String outTradeNo, String tradeNo);

    /**
     * 创建扫码支付订单
     * @param outTradeNo 订单号
     * @param subject 商品名称
     * @param body 商品参数或者描述信息(可以用json字符串表示)
     * @param totalAmount 订单总金额
     * @param notifyUrl 支付结果回调接口
     * @return 支付二维码
     */
    String tradePrecreatePay(String outTradeNo, String subject, String body, BigDecimal totalAmount, String notifyUrl);
}
