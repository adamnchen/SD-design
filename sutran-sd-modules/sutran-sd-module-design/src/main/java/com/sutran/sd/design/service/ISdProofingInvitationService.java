package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;

import java.util.List;

/**
 * 打样邀约服务接口
 *
 * @author Gemini
 */
public interface ISdProofingInvitationService {

    /**
     * 创建并发送一个新的合作邀约
     *
     * @param createDTO 包含邀约信息的DTO
     * @return 创建成功的邀约实体
     */
    SdProofingInvitation createInvitation(ProofingInvitationRequestDTO createDTO);

    /**
     * 获取当前用户收到的所有邀约列表
     *
     * @return 邀约详情列表
     */
    List<ProofingInvitationDetailVO> getReceivedInvitations();

    /**
     * 获取当前用户发出的所有邀约列表
     *
     * @return 邀约详情列表
     */
    List<ProofingInvitationDetailVO> getSentInvitations();

    /**
     * 接受合作邀约
     *
     * @param acceptDTO 接受邀约所需信息（报价、周期、阶梯价、分成等）
     */
    void acceptInvitation(ProofingInvitationAcceptDto acceptDTO);

    /**
     * 拒绝合作邀约
     *
     * @param invitationId 邀约ID
     */
    void rejectInvitation(Long invitationId);

    /**
     * (发送方)取消已发出的邀约
     *
     * @param invitationId 邀约ID
     */
    void cancelInvitation(Long invitationId);

    /**
     * 自动取消超时的邀约
     * 根据 cancel_time_limit 字段自动取消超时的邀约
     */
    void autoCancelExpiredInvitations();
}
