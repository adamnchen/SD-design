package com.sutran.sd.design.config;

import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 众筹定时任务配置
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrowdfundingTaskConfig {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;

    /**
     * 自动检查众筹项目状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void autoUpdateProjectStatus() {
        try {
            log.info("开始执行众筹项目状态自动检查任务");
            crowdfundingProjectService.autoUpdateProjectStatus();
            log.info("众筹项目状态自动检查任务执行完成");
        } catch (Exception e) {
            log.error("众筹项目状态自动检查任务执行失败", e);
        }
    }
}
