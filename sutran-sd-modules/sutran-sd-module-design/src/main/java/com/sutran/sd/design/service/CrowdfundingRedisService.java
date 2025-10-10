package com.sutran.sd.design.service;

import java.math.BigDecimal;

/**
 * 众筹Redis服务接口
 *
 * @author sutran
 * @date 2025-10-10
 */
public interface CrowdfundingRedisService {

    /**
     * 初始化众筹项目金额缓存
     *
     * @param projectId 项目ID
     * @param targetAmount 目标金额
     * @return 是否成功
     */
    boolean initProjectAmount(Long projectId, BigDecimal targetAmount);

    /**
     * 检查并扣减剩余金额
     *
     * @param projectId 项目ID
     * @param requestAmount 请求金额
     * @return 是否扣减成功
     */
    boolean tryDeductAmount(Long projectId, BigDecimal requestAmount);

    /**
     * 获取剩余金额
     *
     * @param projectId 项目ID
     * @return 剩余金额
     */
    BigDecimal getRemainingAmount(Long projectId);

    /**
     * 退还金额到剩余池
     *
     * @param projectId 项目ID
     * @param refundAmount 退还金额
     * @return 是否成功
     */
    boolean refundAmount(Long projectId, BigDecimal refundAmount);

    /**
     * 检查项目是否已完成
     *
     * @param projectId 项目ID
     * @return 是否完成
     */
    boolean isProjectCompleted(Long projectId);

    /**
     * 清理项目缓存
     *
     * @param projectId 项目ID
     * @return 是否成功
     */
    boolean clearProjectCache(Long projectId);
}
