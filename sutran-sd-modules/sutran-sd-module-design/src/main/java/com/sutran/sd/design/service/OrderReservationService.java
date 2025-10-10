package com.sutran.sd.design.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 订单预占服务接口
 *
 * @author sutran
 * @date 2025-10-10
 */
public interface OrderReservationService {

    /**
     * 预占金额
     *
     * @param projectId 项目ID
     * @param amount 预占金额
     * @param orderNo 订单号
     * @param expireMinutes 过期时间（分钟）
     * @return 是否预占成功
     */
    boolean reserveAmount(Long projectId, BigDecimal amount, String orderNo, int expireMinutes);

    /**
     * 确认预占（支付成功）
     *
     * @param projectId 项目ID
     * @param orderNo 订单号
     * @return 是否确认成功
     */
    boolean confirmReservation(Long projectId, String orderNo);

    /**
     * 释放预占（支付失败或超时）
     *
     * @param projectId 项目ID
     * @param orderNo 订单号
     * @return 是否释放成功
     */
    boolean releaseReservation(Long projectId, String orderNo);

    /**
     * 获取项目剩余可预占金额
     *
     * @param projectId 项目ID
     * @return 剩余可预占金额
     */
    BigDecimal getAvailableAmount(Long projectId);

    /**
     * 清理过期预占
     */
    void cleanExpiredReservations();
}
