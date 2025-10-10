package com.sutran.sd.design.service.Impl;

import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.service.CrowdfundingOrderService;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 众筹订单服务实现
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrowdfundingOrderServiceImpl implements CrowdfundingOrderService {

    private final SdCrowdfundingSupportMapper supportMapper;
    private final SdCrowdfundingProjectMapper projectMapper;
    private final CrowdfundingRedisService redisService;
    private final ISdCrowdfundingProjectService projectService;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key前缀
    private static final String ORDER_PREFIX = "crowdfunding:order:";
    private static final String ORDER_TIMEOUT_PREFIX = "crowdfunding:timeout:";

    // 订单超时时间：20分钟
    private static final int ORDER_TIMEOUT_MINUTES = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSupportOrder(CrowdfundingSupportDTO supportDTO) {
        try {
            Long projectId = supportDTO.getProjectId();
            BigDecimal supportAmount = supportDTO.getSupportAmount();

            // 1. 检查Redis中是否还有剩余金额
            if (!redisService.tryDeductAmount(projectId, supportAmount)) {
                throw new RuntimeException("众筹金额不足，无法创建订单");
            }

            // 2. 生成订单号
            String orderNo = "CF" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();

            // 3. 创建支持记录（待支付状态）
            SdCrowdfundingSupport support = new SdCrowdfundingSupport();
            support.setSupportNo(orderNo);
            support.setProjectId(projectId);
            support.setUserId(supportDTO.getUserId());
            support.setUserName(supportDTO.getUserName());
            support.setSupportAmount(supportAmount);
            support.setMessage(supportDTO.getMessage());
            support.setIsAnonymous(supportDTO.getIsAnonymous() ? 1 : 0);
            support.setStatus(0); // 正常
            support.setPaymentStatus(0); // 待支付
            support.setCreateTime(new Date());
            support.setUpdateTime(new Date());

            // 保存支持记录
            supportMapper.insert(support);

            // 4. 将订单放入Redis，设置超时时间
            String orderKey = ORDER_PREFIX + orderNo;
            String timeoutKey = ORDER_TIMEOUT_PREFIX + orderNo;

            redisTemplate.opsForValue().set(orderKey, support, ORDER_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(timeoutKey, "1", ORDER_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            log.info("创建众筹支持订单成功: 项目={}, 订单号={}, 金额={}", projectId, orderNo, supportAmount);
            return orderNo;

        } catch (Exception e) {
            log.error("创建众筹支持订单失败: 项目={}, 金额={}", supportDTO.getProjectId(), supportDTO.getSupportAmount(), e);
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(String orderNo) {
        try {
            // 1. 查询订单信息
            SdCrowdfundingSupport support = supportMapper.selectBySupportNo(orderNo);
            if (support == null) {
                log.error("订单不存在: {}", orderNo);
                return false;
            }

            // 2. 更新支付状态
            support.setPaymentStatus(1); // 已支付
            support.setPaymentTime(new Date());
            support.setPaymentNo(orderNo);
            support.setUpdateTime(new Date());
            supportMapper.updateById(support);

            // 3. 清理Redis中的订单缓存
            String orderKey = ORDER_PREFIX + orderNo;
            String timeoutKey = ORDER_TIMEOUT_PREFIX + orderNo;
            redisTemplate.delete(orderKey);
            redisTemplate.delete(timeoutKey);

            // 4. 检查项目是否真正完成（基于实际支付金额）
            projectService.checkAndUpdateProjectStatus(support.getProjectId());

            log.info("处理支付成功: 订单号={}, 项目={}, 金额={}", orderNo, support.getProjectId(), support.getSupportAmount());
            return true;

        } catch (Exception e) {
            log.error("处理支付成功失败: 订单号={}", orderNo, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleOrderTimeout(String orderNo) {
        try {
            // 1. 查询订单信息
            SdCrowdfundingSupport support = supportMapper.selectBySupportNo(orderNo);
            if (support == null) {
                log.warn("订单不存在，可能已处理: {}", orderNo);
                return false;
            }

            // 2. 检查订单是否已支付
            if (support.getPaymentStatus() == 1) {
                log.info("订单已支付，无需处理超时: {}", orderNo);
                return true;
            }

            // 3. 退还金额到Redis
            boolean refundSuccess = redisService.refundAmount(support.getProjectId(), support.getSupportAmount());
            if (!refundSuccess) {
                log.error("退还金额失败: 订单号={}, 金额={}", orderNo, support.getSupportAmount());
            }

            // 4. 更新订单状态为已取消
            support.setStatus(1); // 已取消
            support.setCancelReason("订单超时");
            support.setCancelTime(new Date());
            support.setUpdateTime(new Date());
            supportMapper.updateById(support);

            // 5. 清理Redis缓存
            String orderKey = ORDER_PREFIX + orderNo;
            String timeoutKey = ORDER_TIMEOUT_PREFIX + orderNo;
            redisTemplate.delete(orderKey);
            redisTemplate.delete(timeoutKey);

            log.info("处理订单超时: 订单号={}, 项目={}, 金额={}", orderNo, support.getProjectId(), support.getSupportAmount());
            return true;

        } catch (Exception e) {
            log.error("处理订单超时失败: 订单号={}", orderNo, e);
            return false;
        }
    }

    @Override
    public boolean isOrderTimeout(String orderNo) {
        try {
            String timeoutKey = ORDER_TIMEOUT_PREFIX + orderNo;
            return !Boolean.TRUE.equals(redisTemplate.hasKey(timeoutKey));
        } catch (Exception e) {
            log.error("检查订单超时状态失败: 订单号={}", orderNo, e);
            return true; // 出错时认为已超时
        }
    }
}
