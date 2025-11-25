package com.sutran.sd.design.schedule;

import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 打样邀约定时任务
 *
 * @author sutran
 * @date 2025-01-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProofingInvitationScheduleTask {

    private final ISdProofingInvitationService invitationService;

    /**
     * 自动取消超时的邀约
     * 每30分钟执行一次
     */
    @Scheduled(fixedRateString = "${invitation.schedule-interval-ms:1800000}")
    public void autoCancelExpiredInvitations() {
        try {
            log.debug("开始执行自动取消超时邀约任务...");
            invitationService.autoCancelExpiredInvitations();
        } catch (Exception e) {
            log.error("自动取消超时邀约任务异常", e);
        }
    }
}
