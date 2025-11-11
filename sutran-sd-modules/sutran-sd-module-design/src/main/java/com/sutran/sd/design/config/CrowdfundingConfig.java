package com.sutran.sd.design.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 众筹模块配置类
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
@Component
@ConfigurationProperties(prefix = "crowdfunding")
public class CrowdfundingConfig {

    /**
     * 抽奖延迟秒数
     */
    private int drawDelaySeconds = 2;

    /**
     * 定时任务间隔毫秒
     */
    private long scheduleIntervalMs = 60000;

    /**
     * 订单超时分钟数
     */
    private int orderTimeoutMinutes = 30;

    /**
     * Redis锁过期秒数
     */
    private int redisLockExpireSeconds = 30;

    /**
     * 分布式锁等待时间（秒）
     */
    private int lockWaitTime = 10;

    /**
     * 分布式锁持有时间（秒）
     */
    private int lockLeaseTime = 30;

    /**
     * 定时任务检查间隔毫秒
     */
    private long taskCheckIntervalMs = 300000; // 5分钟

    /**
     * 订单清理间隔毫秒
     */
    private long orderCleanupIntervalMs = 300000; // 5分钟

    /**
     * 订单超时检查间隔毫秒
     */
    private long orderTimeoutCheckIntervalMs = 600000; // 10分钟
}
