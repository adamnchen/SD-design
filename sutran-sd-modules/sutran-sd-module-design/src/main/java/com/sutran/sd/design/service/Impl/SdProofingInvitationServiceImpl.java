package com.sutran.sd.design.service.Impl;

import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper;
import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    private static final Integer STATUS_PENDING = 0;   // 待处理
    private static final Integer STATUS_ACCEPTED = 1;  // 已接受
    private static final Integer STATUS_REJECTED = 2;  // 已拒绝
    private static final Integer STATUS_CANCELLED = 3; // 已取消



    /**
     * 创建并发送一个新的合作邀约
     */
    @Override
    @Transactional
    public SdProofingInvitation createInvitation(ProofingInvitationRequestDTO createDTO) {
        // 1. 获取当前登录用户ID
        Long senderId = LoginHelper.getUserId();

        // 2. 获取并校验接收者ID
        Long recipientId = createDTO.getInviteeUserId();

        if (recipientId == null) {

            throw new ServiceException("邀约的接收用户不能为空");
        }
        // 使用 .equals() 进行比较
        if (senderId.equals(recipientId)) {
            // 抛出异常
            throw new ServiceException("不能向自己发起邀约");
        }
        SdProofingInvitation invitation = new SdProofingInvitation();
// 1. 使用 BeanUtils.copyProperties 复制属性
        BeanUtils.copyProperties(createDTO, invitation);
// 2. 设置 DTO 中没有的、由业务逻辑决定的字段
        invitation.setInviterUserId(senderId);
        invitation.setStatus(STATUS_PENDING);
        invitationMapper.insert(invitation);
        System.out.println("Executing: createInvitation");
        return invitation;



    }

    /**
     * 获取当前用户收到的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getReceivedInvitations() {
        Long userId = LoginHelper.getUserId();
        Long currentUserId = LoginHelper.getUserId();
        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectReceivedInvitationList(currentUserId);
        System.out.println("Executing: getReceivedInvitations");
        // 3. 直接返回查询到的列表
        return invitationList;



    }

    /**
     * 获取当前用户发出的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getSentInvitations() {
        Long userId = LoginHelper.getUserId();
        Long currentUserId = LoginHelper.getUserId();
        List<ProofingInvitationDetailVO> invitationList = invitationMapper.selectSentInvitationList(currentUserId);
        System.out.println("Executing: getSentInvitations");
        // 3. 直接返回查询到的列表
        return invitationList;

    }

    /**
     * 接受合作邀约
     */
    @Override
    @Transactional
    public void acceptInvitation(Long invitationId) {


        Long currentUserId = LoginHelper.getUserId();


        SdProofingInvitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new ServiceException("操作失败，该邀约不存在或已被删除");
        }



        if (!invitation.getInviteeUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的接收人");
        }

        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法接受");
        }

        invitation.setStatus(STATUS_ACCEPTED);


        int rows = invitationMapper.updateById(invitation);

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

        if (!invitation.getInviteeUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的接收人");
        }

        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理或已取消，无法拒绝");
        }

        invitation.setStatus(STATUS_REJECTED); // 核心区别：状态设置为“已拒绝”

        int rows = invitationMapper.updateById(invitation);
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

        if (!invitation.getInviteeUserId().equals(currentUserId)) {
            throw new ServiceException("权限不足，您不是该邀约的发起人");
        }

        if (!STATUS_PENDING.equals(invitation.getStatus())) {
            throw new ServiceException("操作失败，该邀约已被处理，无法取消");
        }

        invitation.setStatus(STATUS_CANCELLED);

        int rows = invitationMapper.updateById(invitation);
        if (rows == 0) {

            throw new ServiceException("操作失败，请重试");
        }

        System.out.println("Executing: cancelInvitation for ID: " + invitationId);
    }


    }

