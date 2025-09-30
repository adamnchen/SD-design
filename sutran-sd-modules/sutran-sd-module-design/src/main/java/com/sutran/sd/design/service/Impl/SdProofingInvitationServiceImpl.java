package com.sutran.sd.design.service.Impl;

import com.sutran.sd.common.core.domain.dto.ProofingInvitationRequestDTO;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.SdProofingInvitationMapper; // 假设您有这个Mapper
import com.sutran.sd.design.service.ISdProofingInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sutran.sd.common.exception.ServiceException;
import java.util.Collections;
import java.util.Date;
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


    /**
     * 创建并发送一个新的合作邀约
     */
    @Override
    @Transactional // 建议增加事务注解，确保数据一致性
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
        invitation.setStatus(0);
        invitationMapper.insert(invitation);
        System.out.println("Executing: createInvitation");
        return invitation;



    }

    /**
     * 获取当前用户收到的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getReceivedInvitations() {
        // TODO: 1. 获取当前登录用户的ID。
        // TODO: 2. 调用 invitationMapper 的查询方法，根据当前用户ID查询所有 recipientId 是自己的邀约。
        // TODO: 3. Mapper 层建议使用联表查询，直接将结果封装为 ProofingInvitationDetailVO。
        // TODO: 4. 返回查询到的列表。

        System.out.println("Executing: getReceivedInvitations");
        return Collections.emptyList(); // 暂时返回一个空列表，避免空指针
    }

    /**
     * 获取当前用户发出的所有邀约列表
     */
    @Override
    public List<ProofingInvitationDetailVO> getSentInvitations() {
        // TODO: 1. 获取当前登录用户的ID。
        // TODO: 2. 调用 invitationMapper 的查询方法，根据当前用户ID查询所有 senderId 是自己的邀约。
        // TODO: 3. Mapper 层建议使用联表查询，直接将结果封装为 ProofingInvitationDetailVO。
        // TODO: 4. 返回查询到的列表。

        System.out.println("Executing: getSentInvitations");
        return Collections.emptyList(); // 暂时返回一个空列表
    }

    /**
     * 接受合作邀约
     */
    @Override
    @Transactional
    public void acceptInvitation(Long invitationId) {
        // TODO: 1. 根据 invitationId 从数据库查询出邀约实体。
        // TODO: 2. 校验邀约是否存在。
        // TODO: 3. 校验当前登录用户是否是该邀约的接收者 (recipientId)。
        // TODO: 4. 校验邀约当前的状态是否为 PENDING (待处理)。
        // TODO: 5. 如果校验通过，将邀约状态更新为 ACCEPTED。
        // TODO: 6. 调用 invitationMapper.updateById() 方法更新数据库。
        // TODO: 7. (可选) 触发后续业务，如发送通知等。

        System.out.println("Executing: acceptInvitation for ID: " + invitationId);
    }

    /**
     * 拒绝合作邀约
     */
    @Override
    @Transactional
    public void rejectInvitation(Long invitationId) {
        // TODO: 1. 根据 invitationId 从数据库查询出邀约实体。
        // TODO: 2. 校验邀约是否存在。
        // TODO: 3. 校验当前登录用户是否是该邀约的接收者 (recipientId)。
        // TODO: 4. 校验邀约当前的状态是否为 PENDING。
        // TODO: 5. 如果校验通过，将邀约状态更新为 REJECTED。
        // TODO: 6. 调用 invitationMapper.updateById() 方法更新数据库。

        System.out.println("Executing: rejectInvitation for ID: " + invitationId);
    }

    /**
     * (发送方)取消已发出的邀约
     */
    @Override
    @Transactional
    public void cancelInvitation(Long invitationId) {
        // TODO: 1. 根据 invitationId 从数据库查询出邀约实体。
        // TODO: 2. 校验邀约是否存在。
        // TODO: 3. 校验当前登录用户是否是该邀约的发送者 (senderId)。
        // TODO: 4. 校验邀约当前的状态是否为 PENDING。
        // TODO: 5. 如果校验通过，将邀约状态更新为 CANCELLED。
        // TODO: 6. 调用 invitationMapper.updateById() 方法更新数据库。

        System.out.println("Executing: cancelInvitation for ID: " + invitationId);
    }
}
