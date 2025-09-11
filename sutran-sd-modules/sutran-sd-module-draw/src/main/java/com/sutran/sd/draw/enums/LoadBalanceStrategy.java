package com.sutran.sd.draw.enums;

/**
 * @author zj
 * @date 2025年09月09日 21:31
 */
public enum LoadBalanceStrategy {
    /**
     * 轮询
     */
    ROUND_ROBIN,
    /**
     * 加权轮询
     */
    WEIGHTED_ROUND_ROBIN,
    /**
     * 最少连接
     */
    LEAST_CONNECTIONS,
    /**
     * 加权最少连接
     */
    WEIGHTED_LEAST_LOAD;
}
