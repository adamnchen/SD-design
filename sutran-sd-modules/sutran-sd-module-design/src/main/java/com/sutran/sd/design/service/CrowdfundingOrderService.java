package com.sutran.sd.design.service;

import com.sutran.sd.design.dto.CrowdfundingSupportDTO;

/**
 * 众筹订单服务接口
 *
 * @author sutran
 * @date 2025-10-10
 */
public interface CrowdfundingOrderService {

    /**
     * 创建众筹支持订单
     *
     * @param supportDTO 支持信息
     * @return 订单号
     */
    String createSupportOrder(CrowdfundingSupportDTO supportDTO);

    /**
     * 处理支付成功
     *
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean handlePaymentSuccess(String orderNo);

    /**
     * 处理订单超时
     *
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean handleOrderTimeout(String orderNo);

    /**
     * 检查订单是否超时
     *
     * @param orderNo 订单号
     * @return 是否超时
     */
    boolean isOrderTimeout(String orderNo);
}
