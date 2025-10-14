package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationAcceptDto;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.dto.ProofingInvitationChooseDto;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.constant.ProofingInvitationConstants;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.mapper.SdProofingInvitationCandidateMapper;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitationCandidate;
import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 打样邀约服务实现类
 *
 * @author YourName
 */
@Slf4j
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

        // 检查当前作品是否已有待处理的邀约
        String workId = createDTO.getWorkId();
        if (workId != null) {
            List<SdProofingInvitation> existingInvitations = invitationMapper.selectList(
                new LambdaQueryWrapper<SdProofingInvitation>()
                    .eq(SdProofingInvitation::getWorkId, workId)
                    .eq(SdProofingInvitation::getStatus, ProofingInvitationConstants.STATUS_PENDING)
            );

            if (!existingInvitations.isEmpty()) {
                throw new ServiceException("当前作品已有待处理的邀约，无法重复发起");
            }
        }


        // 1. 获取当前登录用户ID
        Long senderId = LoginHelper.getUserId();

        // 2. 获取并校验接收者ID（优先使用数组，兼容单个，最多3个）
        Set<Long> inviteeIds = new HashSet<>();



        // 优先处理 inviteeUserIds 数组
        if (createDTO.getInviteeUserIds() != null && !createDTO.getInviteeUserIds().isEmpty()) {
            // 过滤掉无效的ID（null、0、负数）
            List<Long> validIds = createDTO.getInviteeUserIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
            inviteeIds.addAll(validIds);
            log.info("从 inviteeUserIds 数组中获取到 {} 个有效ID: {}", validIds.size(), validIds);
        }


        log.info("最终确定的邀约接收者ID列表: {}", inviteeIds);
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

        // 5. 为每个被邀约人创建独立的邀约记录
        SdProofingInvitation createdInvitation = null;
        for (Long inviteeId : inviteeIds) {
            SdProofingInvitation invitation = new SdProofingInvitation();
            BeanUtils.copyProperties(createDTO, invitation);
            invitation.setInviterUserId(senderId);
            invitation.setInviteeUserId(inviteeId); // 设置被邀约人ID
            invitation.setStatus(ProofingInvitationConstants.STATUS_PENDING);

            // 保存到数据库
            invitationMapper.insert(invitation);
            createdInvitation = invitation; // 记录最后一个创建的邀约，作为返回结果
        }

        return createdInvitation;
    }

    /**
     * 获取当前用户收到的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getReceivedInvitations() {
        Long currentUserId = LoginHelper.getUserId();
        log.info("开始查询用户 {} 收到的邀约", currentUserId);

        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectReceivedInvitationList(currentUserId);
        log.info("数据库查询返回 {} 条记录", invitationList != null ? invitationList.size() : "null");

        if (invitationList == null || invitationList.isEmpty()) {
            log.info("用户 {} 没有收到任何邀约", currentUserId);
            return new ArrayList<>(); // 返回空列表而不是null
        }

        log.info("用户 {} 收到 {} 个邀约", currentUserId, invitationList.size());
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
            log.info("用户 {} 没有发出任何邀约", currentUserId);
            return new ArrayList<>();
        }

        log.info("用户 {} 发出 {} 个邀约", currentUserId, invitationList.size());
        return invitationList;
    }

    /**
     * 分页查询当前用户收到的邀约列表
     */
    @Override
    public TableDataInfo<ProofingInvitationDetailVO> getReceivedInvitationsPage(PageQuery pageQuery) {
        Long currentUserId = LoginHelper.getUserId();
        log.info("开始分页查询用户 {} 收到的邀约，分页参数：pageNum={}, pageSize={}",
                currentUserId, pageQuery.getPageNum(), pageQuery.getPageSize());

        IPage<ProofingInvitationDetailVO> page = invitationMapper.selectReceivedInvitationPage(pageQuery.build(), currentUserId);
        log.info("分页查询返回 {} 条记录", page.getRecords() != null ? page.getRecords().size() : "null");

        // 手动设置total，因为自定义SQL可能无法被分页插件正确计算
        Long total = invitationMapper.countReceivedInvitations(currentUserId);
        log.info("统计查询返回总数：{}", total);
        page.setTotal(total);

        return TableDataInfo.build(page);
    }

    /**
     * 分页查询当前用户发出的邀约列表
     */
    @Override
    public TableDataInfo<ProofingInvitationDetailVO> getSentInvitationsPage(PageQuery pageQuery) {
        Long currentUserId = LoginHelper.getUserId();
        IPage<ProofingInvitationDetailVO> page = invitationMapper.selectSentInvitationPage(pageQuery.build(), currentUserId);

        // 手动设置total，因为自定义SQL可能无法被分页插件正确计算
        Long total = invitationMapper.countSentInvitations(currentUserId);
        page.setTotal(total);

        return TableDataInfo.build(page);
    }

    /**
     * 获取某邀约下的候选厂家列表（含报价与用户信息）
     */
    public java.util.List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> getInvitationCandidates(Long invitationId) {
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

        log.info("用户 {} 接受邀约 {} 成功", currentUserId, acceptDTO.getInvitationId());

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

        log.info("用户 {} 拒绝邀约 {} 成功", currentUserId, invitationId);
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

        log.info("用户 {} 取消邀约 {} 成功", currentUserId, invitationId);
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

        log.info("自动取消超时邀约完成，共取消 {} 个邀约", cancelledCount);
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

}

