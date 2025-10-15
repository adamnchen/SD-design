package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.domain.PageQuery;

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
     * 分页查询当前用户收到的邀约列表
     *
     * @param pageQuery 分页查询参数
     * @return 分页邀约详情列表
     */
    TableDataInfo<ProofingInvitationDetailVO> getReceivedInvitationsPage(PageQuery pageQuery);

    /**
     * 分页查询当前用户发出的邀约列表
     *
     * @param pageQuery 分页查询参数
     * @return 分页邀约详情列表
     */
    TableDataInfo<ProofingInvitationDetailVO> getSentInvitationsPage(PageQuery pageQuery);

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

    /**
     * 发起人从候选厂家中最终选择一家
     */
    void chooseCandidate(ProofingInvitationChooseDto chooseDto);

    /**
     * 查询邀约下的候选列表
     */
    java.util.List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> getInvitationCandidates(Long invitationId);

    /**
     * 查看邀约详情
     *
     * @param invitationId 邀约ID
     * @return 邀约详情
     */
    ProofingInvitationDetailVO getInvitationDetail(Long invitationId);
    
    // === 商家查看已处理邀约相关方法 ===
    
    /**
     * 分页查询商家已处理的邀约列表
     * 商家只能查看自己作为被邀约人的邀约
     *
     * @param pageQuery 分页查询参数
     * @param status 邀约状态（可选）
     * @return 分页邀约详情列表
     */
    TableDataInfo<ProofingInvitationDetailVO> getMerchantProcessedInvitationsPage(PageQuery pageQuery, Integer status);
    
    /**
     * 查询商家已处理的邀约列表（不分页）
     *
     * @param status 邀约状态（可选）
     * @return 邀约详情列表
     */
    List<ProofingInvitationDetailVO> getMerchantProcessedInvitationsList(Integer status);
    
    /**
     * 根据ID查询商家已处理的邀约详情
     *
     * @param invitationId 邀约ID
     * @return 邀约详情
     */
    ProofingInvitationDetailVO getMerchantProcessedInvitationById(Long invitationId);
    
    /**
     * 统计商家已处理的邀约总数
     *
     * @param status 邀约状态（可选）
     * @return 邀约总数
     */
    Long countMerchantProcessedInvitations(Integer status);
}
