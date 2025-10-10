package com.sutran.sd.design.service.Impl;

import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.service.OrderReservationService;
import com.sutran.sd.design.util.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RESERVATION_PREFIX = "crowdfunding:reservation:";
    private static final String PROJECT_AMOUNT_PREFIX = "crowdfunding:amount:";

    @Override
    public boolean reserveAmount(Long projectId, BigDecimal amount, String orderNo, int expireMinutes) {
        String lockKey = "project:" + projectId;

        return redisLockUtil.executeWithLock(lockKey, 30, () -> {
            try {
                // 1. 获取项目信息
                SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
                if (project == null) {
                    log.error("项目不存在: {}", projectId);
                    return false;
                }

                // 2. 原子性计算当前已预占金额和实际金额
                BigDecimal[] amounts = getCurrentAndReservedAmounts(projectId);
                BigDecimal currentAmount = amounts[0];
                BigDecimal reservedAmount = amounts[1];
                BigDecimal totalReserved = currentAmount.add(reservedAmount);

                // 3. 检查是否超过目标金额（预占金额不参与完成状态判断，只用于防止超卖）
                if (totalReserved.add(amount).compareTo(project.getTargetAmount()) > 0) {
                    log.warn("预占金额超过目标金额: 项目={}, 已筹={}, 已预占={}, 本次预占={}, 目标={}",
                        projectId, currentAmount, reservedAmount, amount, project.getTargetAmount());
                    return false;
                }

                // 4. 如果众筹接近完成（剩余金额小于10%），缩短预占时间到2分钟
                BigDecimal remainingAmount = project.getTargetAmount().subtract(currentAmount);
                BigDecimal threshold = project.getTargetAmount().multiply(new BigDecimal("0.1")); // 10%
                int finalExpireMinutes = expireMinutes;
                if (remainingAmount.compareTo(threshold) <= 0) {
                    // 接近完成时，缩短预占时间
                    finalExpireMinutes = Math.min(expireMinutes, 2);
                    log.info("众筹接近完成，缩短预占时间到{}分钟: 项目={}, 剩余金额={}", 
                        finalExpireMinutes, projectId, remainingAmount);
                }

                // 5. 记录预占信息
                String reservationKey = RESERVATION_PREFIX + projectId + ":" + orderNo;
                ReservationInfo reservationInfo = new ReservationInfo();
                reservationInfo.setProjectId(projectId);
                reservationInfo.setAmount(amount);
                reservationInfo.setOrderNo(orderNo);
                reservationInfo.setReserveTime(new Date());
                reservationInfo.setExpireTime(new Date(System.currentTimeMillis() + finalExpireMinutes * 60 * 1000));

                redisTemplate.opsForValue().set(reservationKey, reservationInfo, finalExpireMinutes, TimeUnit.MINUTES);

                // 5. 更新项目预占金额
                updateProjectReservedAmount(projectId, amount, true);

                log.info("预占金额成功: 项目={}, 订单={}, 金额={}", projectId, orderNo, amount);
                return true;

            } catch (Exception e) {
                log.error("预占金额失败: 项目={}, 订单={}, 金额={}", projectId, orderNo, amount, e);
                return false;
            }
        });
    }

    @Override
    public boolean confirmReservation(Long projectId, String orderNo) {
        String lockKey = "project:" + projectId;

        return redisLockUtil.executeWithLock(lockKey, 30, () -> {
            try {
                // 1. 获取预占信息
                String reservationKey = RESERVATION_PREFIX + projectId + ":" + orderNo;
                ReservationInfo reservationInfo = (ReservationInfo) redisTemplate.opsForValue().get(reservationKey);

                if (reservationInfo == null) {
                    log.error("预占信息不存在: 项目={}, 订单={}", projectId, orderNo);
                    return false;
                }

                // 2. 更新项目实际金额
                SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
                if (project == null) {
                    log.error("项目不存在: {}", projectId);
                    return false;
                }

                BigDecimal newAmount = project.getCurrentAmount().add(reservationInfo.getAmount());
                project.setCurrentAmount(newAmount);
                project.setSupportCount(project.getSupportCount() + 1);
                crowdfundingProjectMapper.updateById(project);

                // 3. 删除预占记录
                redisTemplate.delete(reservationKey);

                // 4. 更新项目预占金额
                updateProjectReservedAmount(projectId, reservationInfo.getAmount(), false);

                log.info("确认预占成功: 项目={}, 订单={}, 金额={}", projectId, orderNo, reservationInfo.getAmount());
                return true;

            } catch (Exception e) {
                log.error("确认预占失败: 项目={}, 订单={}", projectId, orderNo, e);
                return false;
            }
        });
    }

    @Override
    public boolean releaseReservation(Long projectId, String orderNo) {
        String lockKey = "project:" + projectId;

        return redisLockUtil.executeWithLock(lockKey, 30, () -> {
            try {
                // 1. 获取预占信息
                String reservationKey = RESERVATION_PREFIX + projectId + ":" + orderNo;
                ReservationInfo reservationInfo = (ReservationInfo) redisTemplate.opsForValue().get(reservationKey);

                if (reservationInfo == null) {
                    log.warn("预占信息不存在，可能已过期: 项目={}, 订单={}", projectId, orderNo);
                    return true; // 认为释放成功
                }

                // 2. 删除预占记录
                redisTemplate.delete(reservationKey);

                // 3. 更新项目预占金额
                updateProjectReservedAmount(projectId, reservationInfo.getAmount(), false);

                log.info("释放预占成功: 项目={}, 订单={}, 金额={}", projectId, orderNo, reservationInfo.getAmount());
                return true;

            } catch (Exception e) {
                log.error("释放预占失败: 项目={}, 订单={}", projectId, orderNo, e);
                return false;
            }
        });
    }

    @Override
    public BigDecimal getAvailableAmount(Long projectId) {
        try {
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            if (project == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal currentAmount = project.getCurrentAmount() != null ? project.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal reservedAmount = getReservedAmount(projectId);
            BigDecimal availableAmount = project.getTargetAmount().subtract(currentAmount).subtract(reservedAmount);

            return availableAmount.compareTo(BigDecimal.ZERO) > 0 ? availableAmount : BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("获取可用金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public void cleanExpiredReservations() {
        try {
            // 获取所有预占记录
            Set<String> keys = redisTemplate.keys(RESERVATION_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            int cleanedCount = 0;
            Date now = new Date();
            
            for (String key : keys) {
                try {
                    // 使用原子操作检查和删除过期预占
                    ReservationInfo reservationInfo = (ReservationInfo) redisTemplate.opsForValue().get(key);
                    if (reservationInfo != null && reservationInfo.getExpireTime().before(now)) {
                        // 使用原子删除操作，避免重复处理
                        Boolean deleted = redisTemplate.delete(key);
                        if (Boolean.TRUE.equals(deleted)) {
                            // 只有成功删除的才更新预占金额
                            updateProjectReservedAmount(reservationInfo.getProjectId(), reservationInfo.getAmount(), false);
                            cleanedCount++;
                            log.info("清理过期预占: 项目={}, 订单={}, 金额={}", 
                                reservationInfo.getProjectId(), reservationInfo.getOrderNo(), reservationInfo.getAmount());
                        }
                    }
                } catch (Exception e) {
                    log.warn("清理单个预占记录失败: {}", key, e);
                }
            }

            if (cleanedCount > 0) {
                log.info("清理了 {} 个过期预占记录", cleanedCount);
            }

        } catch (Exception e) {
            log.error("清理过期预占失败", e);
        }
    }




    /**
     * 原子性获取当前金额和预占金额
     */
    private BigDecimal[] getCurrentAndReservedAmounts(Long projectId) {
        try {
            // 重新查询项目信息，确保获取最新数据
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            BigDecimal currentAmount = project != null && project.getCurrentAmount() != null ? 
                project.getCurrentAmount() : BigDecimal.ZERO;
            
            String amountKey = PROJECT_AMOUNT_PREFIX + projectId;
            Object amount = redisTemplate.opsForValue().get(amountKey);
            BigDecimal reservedAmount = amount != null ? (BigDecimal) amount : BigDecimal.ZERO;
            
            return new BigDecimal[]{currentAmount, reservedAmount};
        } catch (Exception e) {
            log.error("获取金额信息失败: 项目={}", projectId, e);
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }
    }

    /**
     * 获取项目已预占金额
     */
    private BigDecimal getReservedAmount(Long projectId) {
        try {
            String amountKey = PROJECT_AMOUNT_PREFIX + projectId;
            Object amount = redisTemplate.opsForValue().get(amountKey);
            return amount != null ? (BigDecimal) amount : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取预占金额失败: 项目={}", projectId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 更新项目预占金额
     */
    private void updateProjectReservedAmount(Long projectId, BigDecimal amount, boolean isAdd) {
        try {
            String amountKey = PROJECT_AMOUNT_PREFIX + projectId;
            BigDecimal currentReserved = getReservedAmount(projectId);
            BigDecimal newReserved = isAdd ? currentReserved.add(amount) : currentReserved.subtract(amount);

            if (newReserved.compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(amountKey);
            } else {
                redisTemplate.opsForValue().set(amountKey, newReserved, 1, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.error("更新项目预占金额失败: 项目={}, 金额={}, 是否增加={}", projectId, amount, isAdd, e);
        }
    }

    /**
     * 预占信息内部类
     */
    public static class ReservationInfo {
        private Long projectId;
        private BigDecimal amount;
        private String orderNo;
        private Date reserveTime;
        private Date expireTime;

        // getters and setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public Date getReserveTime() { return reserveTime; }
        public void setReserveTime(Date reserveTime) { this.reserveTime = reserveTime; }
        public Date getExpireTime() { return expireTime; }
        public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    }
}
