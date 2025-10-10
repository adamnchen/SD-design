package com.sutran.sd.pay.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.pay.domain.PayOrder;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author zj
 * @date 2025年08月23日 22:59
 */
public interface PayOrderService {

    /**
     * 分页获取指定人的订单记录
     * @param order 订单
     * @param pageQuery 分页查询
     * @return 订单记录
     */
    TableDataInfo<PayOrder> selectPageOrderList(PayOrder order, PageQuery pageQuery);

    /**
     * 查询指定订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    PayOrder detailById(String id);

    /**
     * 查询指定订单详情
     * @param id 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    PayOrder detailByIdAndUserId(String id, Long userId);

    /**
     * 查询指定订单详情
     * @param outTradeNo 订单号
     * @return 订单详情
     */
    PayOrder detailByOutTradeNo(String outTradeNo);

    /**
     * 检查用户是否存在未处理订单
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 订单
     */
    PayOrder isExistNoDealOrder(Long userId, String appId);

    /**
     * 新增订单
     * @param order 订单
     */
    void insert(PayOrder order);

    /**
     * 支付成功
     * @param outTradeNo 订单号
     * @param tradeNo 交易号
     * @param totalAmount 总金额
     * @param gmtPayment 支付时间
     * @return 是否成功
     */
    boolean successPay(String outTradeNo, String tradeNo, String totalAmount, String gmtPayment);

    /**
     * 支付失败
     * @param outTradeNo 订单号
     * @param tradeNo 交易号
     * @param totalAmount 总金额
     * @return 更新是否成功
     */
    boolean failPay(String outTradeNo, String tradeNo, String totalAmount);

    /**
     * 保存二维码
     * @param outTradeNo 订单号
     * @param qrCode 二维码
     */
    void saveQrCode(String outTradeNo, String qrCode);

    /**
     * 处理支付未支付的数据
     * @param outTradeNo 当前时间
     */
    void handleNoPayOfDataByOutTradeNo(String outTradeNo);

    /**
     * 获取支付二维码
     * @param outTradeNo 订单号
     * @param userId 当前用户ID
     * @return 支付二维码地址
     */
    String getPayQr(String outTradeNo, Long userId);
}
