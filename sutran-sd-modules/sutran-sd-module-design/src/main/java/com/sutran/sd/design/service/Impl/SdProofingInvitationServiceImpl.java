package com.sutran.sd.design.service.Impl;

import com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.constant.ProofingInvitationConstants;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationCandidateMapper;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitationCandidate;
import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 打样邀约服务实现类
 *
 * @author YourName
 */
@Service
@RequiredArgsConstructor // Lombok注解，用于自动生成包含 final 字段的构造函数，实现依赖注入
public class SdProofingInvitationServiceImpl implements ISdProofingInvitationService {

    /**
     * 注入数据访问层依赖 (Mapper or Repository)
     */
    private final SdProofingInvitationMapper invitationMapper;
    private final SdProofingInvitationCandidateMapper candidateMapper;


    // 使用常量类管理状态，不再定义重复常量



    /**
     * 创建并发送一个新的合作邀约
     */
    @Override
    @Transactional
    public SdProofingInvitation createInvitation(ProofingInvitationRequestDTO createDTO) {

        //若已有且未过期，无法邀约
        if (createDTO.getStatus().equals(ProofingInvitationConstants.STATUS_PENDING)){

            throw new ServiceException("当前作品已有邀约，无法发起");
        }


        // 1. 获取当前登录用户ID
        Long senderId = LoginHelper.getUserId();

        // 2. 获取并校验接收者ID（兼容单个/多个，最多3个）
        List<Long> inviteeIds = new ArrayList<>();
        if (createDTO.getInviteeUserIds() != null && !createDTO.getInviteeUserIds().isEmpty()) {
            inviteeIds.addAll(createDTO.getInviteeUserIds());
        }
        if (createDTO.getInviteeUserId() != null) {
            inviteeIds.add(createDTO.getInviteeUserId());
        }
        if (inviteeIds.isEmpty()) {
            throw new ServiceException("邀约的接收用户不能为空");
        }
        if (inviteeIds.size() > 3) {
            throw new ServiceException("最多可选择3个被邀约厂家");
        }





        // 3. 验证取消时限值
        if (createDTO.getCancelTimeLimit() != null &&
            !ProofingInvitationConstants.isValidCancelTimeLimit(createDTO.getCancelTimeLimit())) {
            throw new ServiceException("自动取消时限值无效，必须是1、2、3中的一个");
        }

        // 4. 验证抽奖数量
        if (createDTO.getDrawNumber() == null || createDTO.getDrawNumber() < 1) {
            throw new ServiceException("抽奖数量不能为空且必须大于等于1");
        }

        // 5. 创建邀约实体
        SdProofingInvitation invitation = new SdProofingInvitation();
        BeanUtils.copyProperties(createDTO, invitation);
        invitation.setInviterUserId(senderId);
        invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING);
        createDTO.setStatus(ProofingInvitationConstants.STATUS_PENDING);

        // 6. 保存到数据库
        invitationMapper.insert(invitation);

        // 7. 批量写入候选表（去重）
        java.util.Set<Long> uniqueIds = new java.util.HashSet<>(inviteeIds);
        for (Long inviteeId : uniqueIds) {
            SdProofingInvitationCandidate candidate = new SdProofingInvitationCandidate();
            candidate.setInvitationId(invitation.getId());
            candidate.setInviteeUserId(inviteeId);
            candidate.setStatus(ProofingInvitationConstants.STATUS_PENDING);
            candidateMapper.insert(candidate);
        }

        return invitation;
    }

    /**
     * 获取当前用户收到的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getReceivedInvitations() {
        Long currentUserId = LoginHelper.getUserId();
        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectReceivedInvitationList(currentUserId);

        if (invitationList == null || invitationList.isEmpty()) {
            System.out.println("用户 " + currentUserId + " 没有收到任何邀约");
            return new ArrayList<>(); // 返回空列表而不是null
        }

        System.out.println("用户 " + currentUserId + " 收到 " + invitationList.size() + " 个邀约");
        return invitationList;
    }

    /**
     * 获取当前用户发出的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getSentInvitations() {
        Long currentUserId = LoginHelper.getUserId();
        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectSentInvitationList(currentUserId);

        if (invitationList == null || invitationList.isEmpty()) {
            System.out.println("用户 " + currentUserId + " 没有发出任何邀约");
            return new ArrayList<>();
        }

        System.out.println("用户 " + currentUserId + " 发出 " + invitationList.size() + " 个邀约");
        return invitationList;
    }

    /**
     * 获取某邀约下的候选厂家列表（含报价与用户信息）
     */
    public java.util.List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> getInvitationCandidates(Long invitationId) {
        // 权限：发起人或候选人可见（此处简化为发起人可见，可按需扩展）
        Long currentUserId = LoginHelper.getUserId();
        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("邀约不存在");
        }
        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，只有发起人可查看候选列表");
        }
        return candidateMapper.selectCandidateVOs(invitationId);
    }

    /**
     * 接受合作邀约
     */
    @Override
    @Transactional
    public void acceptInvitation(ProofingInvitationAcceptDto  acceptDTO) {


        Long currentUserId = LoginHelper.getUserId();


        SdProofingInvitation invitation = invitationMapper.selectById(acceptDTO.getInvitationId());
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }


        // 通过候选记录校验：当前用户必须是该邀约的候选厂家之一
        SdProofingInvitationCandidate candidate = candidateMapper.selectOneByInvitationAndInvitee(acceptDTO.getInvitationId(), currentUserId);
        if (candidate == null) {
            throw new ServiceException("权限不足，您不是该邀约的候选接收人");
        }

        if (!ProofingInvitationConstants.STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法接受");
        }

        // 参数基础校验
        if (acceptDTO.getQuotedPrice() == null || acceptDTO.getQuotedPrice().signum() < 0) {
            throw new ServiceException("报价金额无效");
        }
        if (acceptDTO.getQuotedPeriodDays() == null || acceptDTO.getQuotedPeriodDays() <= 0) {
            throw new ServiceException("预计打样周期必须大于0");
        }
        if (acceptDTO.getProfitShareRatio() != null) {
            if (acceptDTO.getProfitShareRatio().compareTo(new java.math.BigDecimal("0")) < 0
                || acceptDTO.getProfitShareRatio().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new ServiceException("利润分成比例区间为0-100");
            }
        }

        // 处理阶梯价格为JSON
        String tieredPricingJson = null;
        if (acceptDTO.getTieredPricing() != null && !acceptDTO.getTieredPricing().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                tieredPricingJson = mapper.writeValueAsString(acceptDTO.getTieredPricing());
            } catch (Exception e) {
                throw new ServiceException("阶梯价格序列化失败");
            }
        }

        // 写入候选报价信息（不改变主表状态）
        candidate.setQuotedPrice(acceptDTO.getQuotedPrice());
        candidate.setQuotedPeriodDays(acceptDTO.getQuotedPeriodDays());
        candidate.setIsQuoteBatchPlan(Boolean.TRUE.equals(acceptDTO.getIsQuoteBatchPlan()));
        candidate.setQuoteSubmitAt(new java.util.Date());
        candidate.setTieredPricing(tieredPricingJson);
        candidate.setProfitShareRatio(acceptDTO.getProfitShareRatio());
        candidate.setStatus(ProofingInvitationConstants.STATUS_ACCEPTED);

        int rows = candidateMapper.updateById(candidate);

        System.out.println("Executing: getSentInvitations");

        if (rows == 0) {
            throw new ServiceException("操作失败，请重试");
        }
    }

    /**
     * 拒绝合作邀约
     */
    @Override
    @Transactional
    public void rejectInvitation(Long invitationId) {
        // 准备工作：获取当前操作的用户ID
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        SdProofingInvitationCandidate candidate = candidateMapper.selectOneByInvitationAndInvitee(invitationId, currentUserId);
        if (candidate == null) {
            throw new ServiceException("权限不足，您不是该邀约的候选接收人");
        }

        if (!ProofingInvitationConstants.STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法拒绝");
        }

        candidate.setStatus(ProofingInvitationConstants.STATUS_REJECTED);

        int rows = candidateMapper.updateById(candidate);
        if (rows == 0) {

            throw new ServiceException("数据库操作失败，请重试");
        }

        System.out.println("Executing: rejectInvitation for ID: " + invitationId);
    }

    /**
     * (发送方)取消已发出的邀约
     */
    @Override
    @Transactional
    public void cancelInvitation(Long invitationId) {
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的发起人");
        }

        if (!ProofingInvitationConstants.STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理，无法取消");
        }

        invitation.setStatus(ProofingInvitationConstants.STATUS_CANCELLED);

        int rows = invitationMapper.updateById(invitation);
        if (rows == 0) {

            throw new ServiceException("操作失败，请重试");
        }

        System.out.println("Executing: cancelInvitation for ID: " + invitationId);
    }

    /**
     * 自动取消超时的邀约
     */
    @Override
    @Transactional
    public void autoCancelExpiredInvitations() {
        // 查询所有待处理状态的邀约
        List<SdProofingInvitation> pendingInvitations = invitationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SdProofingInvitation>()
                .eq(SdProofingInvitation::getStatus, ProofingInvitationConstants.STATUS_PENDING)
        );

        int cancelledCount = 0;
        for (SdProofingInvitation invitation : pendingInvitations) {
            if (isInvitationExpired(invitation)) {
                invitation.setStatus(ProofingInvitationConstants.STATUS_CANCELLED);
                invitationMapper.updateById(invitation);
                cancelledCount++;
            }
        }

        System.out.println("自动取消超时邀约完成，共取消 " + cancelledCount + " 个邀约");
    }

    /**
     * 发起人从候选厂家中最终选择一家
     */
    @Override
    @Transactional
    public void chooseCandidate(ProofingInvitationChooseDto chooseDto) {
        Long currentUserId = LoginHelper.getUserId();

        SdProofingInvitation invitation = invitationMapper.selectById(chooseDto.getInvitationId());
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }

        if (!invitation.getInviterUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的发起人");
        }

        if (!ProofingInvitationConstants.STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("该邀约当前状态不可进行最终选择");
        }

        SdProofingInvitationCandidate selected = candidateMapper.selectOneByInvitationAndInvitee(chooseDto.getInvitationId(), chooseDto.getInviteeUserId());
        if (selected == null || !ProofingInvitationConstants.STATUS_ACCEPTED.equals(selected.getStatus())) {
            throw new ServiceException("所选厂家未接受邀约或不存在");
        }

        // 更新主表最终选择信息
        invitation.setSelectedInviteeUserId(chooseDto.getInviteeUserId());
        invitation.setSelectedAt(new java.util.Date());
        invitation.setQuotedPrice(selected.getQuotedPrice());
        invitation.setQuotedPeriodDays(selected.getQuotedPeriodDays());
        invitation.setIsQuoteBatchPlan(selected.getIsQuoteBatchPlan());
        invitation.setQuoteSubmitAt(selected.getQuoteSubmitAt());
        invitation.setTieredPricing(selected.getTieredPricing());
        invitation.setProfitShareRatio(selected.getProfitShareRatio());
        invitation.setStatus(ProofingInvitationConstants.STATUS_ACCEPTED);
        invitationMapper.updateById(invitation);

        // 关闭其他候选
        List<SdProofingInvitationCandidate> candidates = candidateMapper.selectByInvitationId(chooseDto.getInvitationId());
        for (SdProofingInvitationCandidate c : candidates) {
            if (!c.getInviteeUserId().equals(chooseDto.getInviteeUserId())) {
                c.setStatus(4); // 已关闭
                candidateMapper.updateById(c);
            }
        }
    }

    /**
     * 判断邀约是否已超时
     */
    private boolean isInvitationExpired(SdProofingInvitation invitation) {
        if (invitation.getCancelTimeLimit() == null) {
            return false; // 没有设置取消时限，不自动取消
        }

        // 计算超时时间（天数）
        int expireDays = ProofingInvitationConstants.getCancelTimeDays(invitation.getCancelTimeLimit());

        // 计算创建时间 + 超时天数
        long expireTime = invitation.getCreateTime().getTime() + (expireDays * 24 * 60 * 60 * 1000L);

        // 当前时间是否超过超时时间
        return System.currentTimeMillis() > expireTime;
    }
    /**
     * 商家未在预约周期完成，取消邀约，记录违规
     * 注意：此方法需要根据众筹完成与否和完成时间进行判定，目前暂未实现
     */
    private boolean isInvitationQuotedPeriodExpired(SdProofingInvitation invitation) {
        // TODO: 需要根据众筹完成与否，完成时间判定，目前做不了
        int quotedPeriodDays = invitation.getQuotedPeriodDays();
        return false;


    }

}

