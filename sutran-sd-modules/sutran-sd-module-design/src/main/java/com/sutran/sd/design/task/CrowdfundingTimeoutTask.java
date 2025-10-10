package com.sutran.sd.design.task;

import com.sutran.sd.design.service.CrowdfundingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 众筹超时订单清理任务
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrowdfundingTimeoutTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CrowdfundingOrderService orderService;

    // Redis Key前缀
    private static final String ORDER_TIMEOUT_PREFIX = "crowdfunding:timeout:*";

    /**
     * 每5分钟执行一次超时订单清理
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void cleanupTimeoutOrders() {
        try {
            log.info("开始清理众筹超时订单...");
            
            // 1. 获取所有超时订单的Key
            Set<String> timeoutKeys = redisTemplate.keys(ORDER_TIMEOUT_PREFIX);
            if (timeoutKeys == null || timeoutKeys.isEmpty()) {
                log.debug("没有发现超时订单");
                return;
            }
            
            int processedCount = 0;
            int successCount = 0;
            
            // 2. 处理每个超时订单
            for (String timeoutKey : timeoutKeys) {
                try {
                    // 提取订单号
                    String orderNo = timeoutKey.substring(ORDER_TIMEOUT_PREFIX.length() - 1);
                    
                    // 检查订单是否超时
                    if (orderService.isOrderTimeout(orderNo)) {
                        // 处理超时订单
                        boolean success = orderService.handleOrderTimeout(orderNo);
                        if (success) {
                            successCount++;
                        }
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("处理超时订单失败: {}", timeoutKey, e);
                }
            }
            
            log.info("众筹超时订单清理完成: 处理数量={}, 成功数量={}", processedCount, successCount);
            
        } catch (Exception e) {
            log.error("清理众筹超时订单失败", e);
        }
    }

    /**
     * 每10分钟检查一次众筹项目状态
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10分钟
    public void checkProjectStatus() {
        try {
            log.info("开始检查众筹项目状态...");
            
            // 这里可以添加检查所有众筹中项目的逻辑
            // 确保Redis状态与实际支付状态一致
            
            log.info("众筹项目状态检查完成");
            
        } catch (Exception e) {
            log.error("检查众筹项目状态失败", e);
        }
    }

    /**
     * 每天凌晨2点执行一次全面清理
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyCleanup() {
        try {
            log.info("开始执行众筹订单每日清理...");
            
            // 清理所有超时订单
            cleanupTimeoutOrders();
            
            // 检查项目状态一致性
            checkProjectStatus();
            
            log.info("众筹订单每日清理完成");
            
        } catch (Exception e) {
            log.error("众筹订单每日清理失败", e);
        }
    }
}
