package com.sutran.sd.design.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private RedissonClient redissonClient;

    @Value("${crowdfunding.redis-lock-expire-seconds:30}")
    private int defaultExpireTime;

    private static final String LOCK_PREFIX = "crowdfunding:lock:";

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
            RLock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(0, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取分布式锁失败: {}", key, e);
            return false;
        }
    }

    /**
     * 获取分布式锁（使用配置的默认过期时间）
     *
     * @param key 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String key) {
        return tryLock(key, defaultExpireTime);
    }

    /**
     * 释放分布式锁
     *
     * @param key 锁的key
     */
    public void releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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
        return executeWithLock(key, defaultExpireTime, action);
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
