package com.sutran.sd.design.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.config.CrowdfundingConfig;
import com.sutran.sd.design.service.CrowdfundingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 众筹定时任务
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrowdfundingScheduleTask {

    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;
    private final SdCrowdfundingSupportMapper supportMapper;
    private final CrowdfundingConfig crowdfundingConfig;
    private final CrowdfundingRedisService crowdfundingRedisService;

    /**
     * 检查众筹项目状态
     */
    @Scheduled(fixedRateString = "${crowdfunding.schedule-interval-ms:60000}")
    public void checkCrowdfundingStatus() {
        try {
            log.debug("开始检查众筹项目状态...");
            
            // 1. 检查过期的众筹项目
            checkExpiredProjects();
            
            // 2. 清理超时未支付的订单
            checkTimeoutOrders();
            
        } catch (Exception e) {
            log.error("检查众筹项目状态异常", e);
        }
    }

    /**
     * 检查超时未支付的订单并回退金额
     */
    private void checkTimeoutOrders() {
        try {
            // 查询创建超过配置时间但未支付的支持记录
            int timeoutMinutes = crowdfundingConfig.getOrderTimeoutMinutes();
            Date timeoutTime = new Date(System.currentTimeMillis() - timeoutMinutes * 60 * 1000);
            
            LambdaQueryWrapper<SdCrowdfundingSupport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(SdCrowdfundingSupport::getOrderNo) // 未支付
                       .lt(SdCrowdfundingSupport::getCreateTime, timeoutTime); // 创建时间超过配置时间
            
            List<SdCrowdfundingSupport> timeoutSupports = supportMapper.selectList(queryWrapper);
            
            for (SdCrowdfundingSupport support : timeoutSupports) {
                try {
                    // 1. 回退Redis金额
                    boolean refunded = crowdfundingRedisService.refundAmount(support.getProjectId(), support.getSupportAmount());
                    if (refunded) {
                        log.info("回退Redis金额成功: 项目ID={}, 金额={}", support.getProjectId(), support.getSupportAmount());
                    } else {
                        log.error("回退Redis金额失败: 项目ID={}, 金额={}", support.getProjectId(), support.getSupportAmount());
                    }
                    
                    // 2. 删除超时的支持记录
                    supportMapper.deleteById(support.getId());
                    log.info("清理超时订单: 支持记录ID={}, 项目ID={}", support.getId(), support.getProjectId());
                } catch (Exception e) {
                    log.error("清理超时订单失败: 支持记录ID={}", support.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查超时订单异常", e);
        }
    }

    /**
     * 检查过期的众筹项目
     */
    private void checkExpiredProjects() {
        try {
            // 查询众筹中但已过期的项目
            LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingProject::getStatus, 1) // 众筹中
                       .le(SdCrowdfundingProject::getEndTime, new Date()); // 已过期
            
            List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectList(queryWrapper);
            
            for (SdCrowdfundingProject project : projects) {
                try {
                    // 检查是否达到目标金额
                    if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
                        // 众筹成功
                        project.setStatus(2);
                        log.info("众筹成功: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());
                    } else {
                        // 众筹失败
                        project.setStatus(3);
                        log.info("众筹失败: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());
                    }
                    
                    crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
                } catch (Exception e) {
                    log.error("更新过期项目状态失败: 项目ID={}", project.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查过期项目异常", e);
        }
    }


}
