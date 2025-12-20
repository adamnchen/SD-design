package com.sutran.sd.design.service.impl;

import com.sutran.sd.design.config.CrowdfundingConfig;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 众筹Redis服务实现
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrowdfundingRedisServiceImpl implements CrowdfundingRedisService {

    private final RedissonClient redissonClient;
    private final CrowdfundingConfig crowdfundingConfig;

    /** Redis Key前缀 **/
    private static final String CROWDFUNDING_AMOUNT_PREFIX = "crowdfunding:amount:";
    private static final String CROWDFUNDING_LOCK_PREFIX = "crowdfunding:lock:";

    @Override
    public boolean initProjectAmount(Long projectId, BigDecimal targetAmount) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
            atomicDouble.set(targetAmount.doubleValue());
            atomicDouble.expire(Duration.ofDays(7));
            log.info("初始化众筹项目金额缓存: 项目={}, 目标金额={}", projectId, targetAmount);
            return true;
        } catch (Exception e) {
            log.error("初始化众筹项目金额缓存失败: 项目={}", projectId, e);
            return false;
        }
    }

    @Override
    public boolean tryDeductAmount(Long projectId, BigDecimal requestAmount) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            String lockKey = CROWDFUNDING_LOCK_PREFIX + projectId;
            // 使用分布式锁确保原子性
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.tryLock(crowdfundingConfig.getLockWaitTime(), crowdfundingConfig.getLockLeaseTime(), TimeUnit.SECONDS)) {
                try {
                    RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
                    double currentAmount = atomicDouble.get();
                    double deductAmount = requestAmount.doubleValue();

                    if (currentAmount >= deductAmount) {
                        atomicDouble.addAndGet(-deductAmount);
                        return true;
                    }
                    else {
                        log.error("[众筹]>>>>>>>>>众筹金额不符: 项目={}, 当前金额={}, 请求金额={}", projectId, currentAmount, deductAmount);
                        return false;
                    }
                }
                finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取分布式锁失败: 项目={}", projectId);
                return false;
            }
        } catch (Exception e) {
            log.error("扣减众筹金额失败: 项目={}, 金额={}", projectId, requestAmount, e);
            return false;
        }
    }

    @Override
    public BigDecimal getRemainingAmount(Long projectId) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
            double amount = atomicDouble.get();
            return BigDecimal.valueOf(amount);
        } catch (Exception e) {
            log.error("获取剩余金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean refundAmount(Long projectId, BigDecimal refundAmount) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
            atomicDouble.addAndGet(refundAmount.doubleValue());

            log.info("成功退还众筹金额: 项目={}, 退还金额={}, 剩余金额={}",
                projectId, refundAmount, atomicDouble.get());
            return true;
        } catch (Exception e) {
            log.error("退还众筹金额失败: 项目={}, 金额={}", projectId, refundAmount, e);
            return false;
        }
    }

    @Override
    public boolean clearProjectCache(Long projectId) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            String lockKey = CROWDFUNDING_LOCK_PREFIX + projectId;

            redissonClient.getAtomicDouble(key).delete();
            redissonClient.getLock(lockKey).forceUnlock();

            log.info("清理众筹项目缓存: 项目={}", projectId);
            return true;
        } catch (Exception e) {
            log.error("清理众筹项目缓存失败: 项目={}", projectId, e);
            return false;
        }
    }
}
