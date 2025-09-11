package com.sutran.sd.draw.enums;

/**
 * @author zj
 * @date 2025年09月09日 21:32
 */
public enum TaskStatus {
    /**
     * 待处理
     */
    PENDING,
    /**
     * 已入队
     */
    QUEUED,
    /**
     * 处理中
     */
    PROCESSING,
    /**
     * 已完成
     */
    COMPLETED,
    /**
     * 已失败
     */
    FAILED,
    /**
     * 已取消
     */
    CANCELLED
}
