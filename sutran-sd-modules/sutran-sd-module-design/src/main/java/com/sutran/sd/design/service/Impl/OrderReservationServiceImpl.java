package com.sutran.sd.design.service.Impl;

import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.service.OrderReservationService;
import com.sutran.sd.design.util.RedisLockUtil;
import com.sutran.sd.design.config.CrowdfundingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.Set;

/**
 * 订单预占服务实现
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReservationServiceImpl implements OrderReservationService {

    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;
    private final RedisLockUtil redisLockUtil;
    private final CrowdfundingConfig crowdfundingConfig;

    @Autowired
    private RedissonClient redissonClient;

    private static final String RESERVATION_PREFIX = "crowdfunding:reservation:";
    private static final String PROJECT_AMOUNT_PREFIX = "crowdfunding:amount:";

    @Override
    public boolean reserveAmount(Long projectId, BigDecimal amount, String orderNo, int expireMinutes) {
        String lockKey = "reservation:" + projectId;
        String reservationKey = RESERVATION_PREFIX + orderNo;
        
        return redisLockUtil.executeWithLock(lockKey, () -> {
            try {
                // 检查项目是否存在
                SdCrowdfundingProject project = crowdfundingProjectMapper.selectSdCrowdfundingProjectById(projectId);
                if (project == null) {
                    log.warn("项目不存在: {}", projectId);
                    return false;
                }

                // 检查项目状态
                if (project.getStatus() != 1) {
                    log.warn("项目状态不允许预占: 项目={}, 状态={}", projectId, project.getStatus());
                    return false;
                }

                // 检查是否已经预占
                RMap<String, ReservationInfo> reservationMap = redissonClient.getMap(reservationKey);
                if (reservationMap.isExists()) {
                    log.warn("订单已预占: {}", orderNo);
                    return false;
                }

                // 检查项目剩余金额
                String amountKey = PROJECT_AMOUNT_PREFIX + projectId;
                RMap<String, BigDecimal> amountMap = redissonClient.getMap(amountKey);
                BigDecimal remainingAmount = amountMap.get("remaining");
                
                if (remainingAmount == null || remainingAmount.compareTo(amount) < 0) {
                    log.warn("项目剩余金额不足: 项目={}, 剩余={}, 请求={}", projectId, remainingAmount, amount);
                    return false;
                }

                // 创建预占信息
                ReservationInfo reservationInfo = new ReservationInfo();
                reservationInfo.setProjectId(projectId);
                reservationInfo.setOrderNo(orderNo);
                reservationInfo.setAmount(amount);
                reservationInfo.setReserveTime(new Date());
                reservationInfo.setExpireTime(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L));

                // 保存预占信息
                reservationMap.put("info", reservationInfo);
                reservationMap.expire(Duration.ofMinutes(expireMinutes));

                // 更新剩余金额
                BigDecimal newRemaining = remainingAmount.subtract(amount);
                amountMap.put("remaining", newRemaining);
                amountMap.expire(Duration.ofHours(1));

                log.info("预占成功: 项目={}, 订单={}, 金额={}, 剩余={}", projectId, orderNo, amount, newRemaining);
                return true;

            } catch (Exception e) {
                log.error("预占失败: 项目={}, 订单={}, 金额={}", projectId, orderNo, amount, e);
                return false;
            }
        });
    }

    @Override
    public boolean confirmReservation(Long projectId, String orderNo) {
        String reservationKey = RESERVATION_PREFIX + orderNo;
        
        try {
            RMap<String, ReservationInfo> reservationMap = redissonClient.getMap(reservationKey);
            ReservationInfo reservationInfo = reservationMap.get("info");
            
            if (reservationInfo == null) {
                log.warn("预占记录不存在: {}", orderNo);
                return false;
            }

            // 删除预占记录
            reservationMap.delete();
            
            log.info("确认预占成功: 订单={}", orderNo);
            return true;

        } catch (Exception e) {
            log.error("确认预占失败: 订单={}", orderNo, e);
            return false;
        }
    }

    @Override
    public boolean releaseReservation(Long projectId, String orderNo) {
        String reservationKey = RESERVATION_PREFIX + orderNo;
        
        try {
            RMap<String, ReservationInfo> reservationMap = redissonClient.getMap(reservationKey);
            ReservationInfo reservationInfo = reservationMap.get("info");
            
            if (reservationInfo == null) {
                log.warn("预占记录不存在: {}", orderNo);
                return false;
            }

            // 退还金额
            String amountKey = PROJECT_AMOUNT_PREFIX + reservationInfo.getProjectId();
            RMap<String, BigDecimal> amountMap = redissonClient.getMap(amountKey);
            BigDecimal currentRemaining = amountMap.get("remaining");
            if (currentRemaining != null) {
                BigDecimal newRemaining = currentRemaining.add(reservationInfo.getAmount());
                amountMap.put("remaining", newRemaining);
                amountMap.expire(Duration.ofHours(1));
            }

            // 删除预占记录
            reservationMap.delete();
            
            log.info("取消预占成功: 订单={}, 退还金额={}", orderNo, reservationInfo.getAmount());
            return true;

        } catch (Exception e) {
            log.error("取消预占失败: 订单={}", orderNo, e);
            return false;
        }
    }

    @Override
    public void cleanExpiredReservations() {
        try {
            // 获取所有预占记录
            RSet<String> reservationKeys = redissonClient.getSet(RESERVATION_PREFIX + "keys");
            Set<String> keys = reservationKeys.readAll();
            
            int cleanedCount = 0;
            for (String key : keys) {
                try {
                    RMap<String, ReservationInfo> reservationMap = redissonClient.getMap(key);
                    ReservationInfo reservationInfo = reservationMap.get("info");
                    
                    if (reservationInfo != null && reservationInfo.getExpireTime().before(new Date())) {
                        // 退还金额
                        String amountKey = PROJECT_AMOUNT_PREFIX + reservationInfo.getProjectId();
                        RMap<String, BigDecimal> amountMap = redissonClient.getMap(amountKey);
                        BigDecimal currentRemaining = amountMap.get("remaining");
                        if (currentRemaining != null) {
                            BigDecimal newRemaining = currentRemaining.add(reservationInfo.getAmount());
                            amountMap.put("remaining", newRemaining);
                            amountMap.expire(Duration.ofHours(1));
                        }

                        // 删除过期预占记录
                        reservationMap.delete();
                        reservationKeys.remove(key);
                        cleanedCount++;
                        
                        log.info("清理过期预占: 订单={}, 退还金额={}", 
                                reservationInfo.getOrderNo(), reservationInfo.getAmount());
                    }
                } catch (Exception e) {
                    log.error("清理预占记录失败: {}", key, e);
                }
            }
            
            log.info("清理过期预占完成: 清理数量={}", cleanedCount);

        } catch (Exception e) {
            log.error("清理过期预占异常", e);
        }
    }

    @Override
    public BigDecimal getAvailableAmount(Long projectId) {
        try {
            String amountKey = PROJECT_AMOUNT_PREFIX + projectId;
            RMap<String, BigDecimal> amountMap = redissonClient.getMap(amountKey);
            BigDecimal remaining = amountMap.get("remaining");
            return remaining != null ? remaining : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取项目剩余金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }


    /**
     * 预占信息
     */
    public static class ReservationInfo {
        private Long projectId;
        private String orderNo;
        private BigDecimal amount;
        private Date reserveTime;
        private Date expireTime;

        // Getters and Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Date getReserveTime() { return reserveTime; }
        public void setReserveTime(Date reserveTime) { this.reserveTime = reserveTime; }
        
        public Date getExpireTime() { return expireTime; }
        public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    }
}