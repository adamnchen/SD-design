package com.sutran.sd.design.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Component
public class RedisLockUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "crowdfunding:lock:";
    private static final int DEFAULT_EXPIRE_TIME = 30; // 30秒

    /**
     * 获取分布式锁
     *
     * @param key 锁的key
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, int expireTime) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", expireTime, TimeUnit.SECONDS);
            return success != null && success;
        } catch (Exception e) {
            log.error("获取分布式锁失败: {}", key, e);
            return false;
        }
    }

    /**
     * 获取分布式锁（默认30秒过期）
     *
     * @param key 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 释放分布式锁
     *
     * @param key 锁的key
     */
    public void releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("释放分布式锁失败: {}", key, e);
        }
    }

    /**
     * 执行带锁的操作
     *
     * @param key 锁的key
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String key, LockAction<T> action) {
        return executeWithLock(key, DEFAULT_EXPIRE_TIME, action);
    }

    /**
     * 执行带锁的操作
     *
     * @param key 锁的key
     * @param expireTime 过期时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String key, int expireTime, LockAction<T> action) {
        if (tryLock(key, expireTime)) {
            try {
                return action.execute();
            } finally {
                releaseLock(key);
            }
        } else {
            throw new RuntimeException("获取分布式锁失败，请稍后重试");
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {
        T execute();
    }
}
