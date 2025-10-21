package com.sutran.sd.design.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
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

    /**
     * 检查众筹项目状态
     */
    @Scheduled(fixedRateString = "${crowdfunding.schedule-interval-ms:60000}")
    public void checkCrowdfundingStatus() {
        try {
            log.debug("开始检查众筹项目状态...");
            
            // 1. 检查过期的众筹项目
            checkExpiredProjects();
            
            // 2. 检查抽奖状态
            checkDrawStatus();
            
        } catch (Exception e) {
            log.error("检查众筹项目状态异常", e);
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

    /**
     * 检查抽奖状态
     */
    private void checkDrawStatus() {
        try {
            // 查询众筹成功但抽奖未开始的项目
            LambdaQueryWrapper<SdCrowdfundingProject> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdCrowdfundingProject::getStatus, 2) // 众筹成功
                       .eq(SdCrowdfundingProject::getDrawStatus, 0); // 抽奖未开始
            
            List<SdCrowdfundingProject> projects = crowdfundingProjectMapper.selectList(queryWrapper);
            
            for (SdCrowdfundingProject project : projects) {
                try {
                    // 自动开始抽奖
                    project.setDrawStatus(1);
                    project.setDrawTime(new Date());
                    crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
                    log.info("自动开始抽奖: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());
                } catch (Exception e) {
                    log.error("自动开始抽奖失败: 项目ID={}", project.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查抽奖状态异常", e);
        }
    }


}
