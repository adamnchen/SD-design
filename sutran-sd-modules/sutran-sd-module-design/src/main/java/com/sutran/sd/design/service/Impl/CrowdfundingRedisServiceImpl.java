package com.sutran.sd.design.service.impl;

import com.sutran.sd.design.service.CrowdfundingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
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

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key前缀
    private static final String CROWDFUNDING_AMOUNT_PREFIX = "crowdfunding:amount:";
    private static final String CROWDFUNDING_LOCK_PREFIX = "crowdfunding:lock:";

    // Lua脚本：原子性扣减金额
    private static final String DEDUCT_AMOUNT_SCRIPT = 
        "local key = KEYS[1] " +
        "local deductAmount = tonumber(ARGV[1]) " +
        "local currentAmount = tonumber(redis.call('get', key) or '0') " +
        "if currentAmount >= deductAmount then " +
        "  redis.call('decrbyfloat', key, deductAmount) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";

    // Lua脚本：原子性退还金额
    private static final String REFUND_AMOUNT_SCRIPT = 
        "local key = KEYS[1] " +
        "local refundAmount = tonumber(ARGV[1]) " +
        "redis.call('incrbyfloat', key, refundAmount) " +
        "return 1";

    @Override
    public boolean initProjectAmount(Long projectId, BigDecimal targetAmount) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            redisTemplate.opsForValue().set(key, targetAmount.toString(), 7, TimeUnit.DAYS);
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
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(lockAcquired)) {
                log.warn("获取分布式锁失败: 项目={}", projectId);
                return false;
            }
            
            try {
                // 使用Lua脚本原子性扣减金额
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptText(DEDUCT_AMOUNT_SCRIPT);
                script.setResultType(Long.class);
                
                Long result = redisTemplate.execute(script, Collections.singletonList(key), requestAmount.toString());
                
                if (result != null && result == 1) {
                    log.info("成功扣减众筹金额: 项目={}, 扣减金额={}, 剩余金额={}", 
                        projectId, requestAmount, getRemainingAmount(projectId));
                    return true;
                } else {
                    log.warn("扣减众筹金额失败，金额不足: 项目={}, 请求金额={}, 剩余金额={}", 
                        projectId, requestAmount, getRemainingAmount(projectId));
                    return false;
                }
            } finally {
                // 释放分布式锁
                redisTemplate.delete(lockKey);
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
            String amountStr = (String) redisTemplate.opsForValue().get(key);
            if (amountStr != null) {
                return new BigDecimal(amountStr);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取剩余金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean refundAmount(Long projectId, BigDecimal refundAmount) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            
            // 使用Lua脚本原子性退还金额
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(REFUND_AMOUNT_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Collections.singletonList(key), refundAmount.toString());
            
            if (result != null && result == 1) {
                log.info("成功退还众筹金额: 项目={}, 退还金额={}, 剩余金额={}", 
                    projectId, refundAmount, getRemainingAmount(projectId));
                return true;
            } else {
                log.error("退还众筹金额失败: 项目={}, 金额={}", projectId, refundAmount);
                return false;
            }
        } catch (Exception e) {
            log.error("退还众筹金额失败: 项目={}, 金额={}", projectId, refundAmount, e);
            return false;
        }
    }

    @Override
    public boolean isProjectCompleted(Long projectId) {
        try {
            BigDecimal remainingAmount = getRemainingAmount(projectId);
            return remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
        } catch (Exception e) {
            log.error("检查项目完成状态失败: 项目={}", projectId, e);
            return false;
        }
    }

    @Override
    public boolean clearProjectCache(Long projectId) {
        try {
            String key = CROWDFUNDING_AMOUNT_PREFIX + projectId;
            String lockKey = CROWDFUNDING_LOCK_PREFIX + projectId;
            
            redisTemplate.delete(key);
            redisTemplate.delete(lockKey);
            
            log.info("清理众筹项目缓存: 项目={}", projectId);
            return true;
        } catch (Exception e) {
            log.error("清理众筹项目缓存失败: 项目={}", projectId, e);
            return false;
        }
    }
}
