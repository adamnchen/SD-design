package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitation;
import com.sutran.sd.common.core.domain.vo.ProofingInvitationDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 打样邀约 数据访问层
 *
 * @author YourName
 */
@Mapper
public interface SdProofingInvitationMapper extends BaseMapper<SdProofingInvitation> {

    /**
     * 查询我收到的邀约列表
     * <p>
     * 这个方法需要自定义SQL，因为它可能需要关联查询发送者的信息等，
     * 并将结果封装到 ProofingInvitationDetailVO 中。
     *
     * @param userId 当前登录用户的ID (即接收者ID)
     * @return 邀约详情列表
     */
    List<ProofingInvitationDetailVO> selectReceivedInvitationList(@Param("userId") Long userId);

    /**
     * 查询我发出的邀约列表
     * <p>
     * 同样需要自定义SQL，可能需要关联查询接收者的信息。
     *
     * @param userId 当前登录用户的ID (即发送者ID)
     * @return 邀约详情列表
     */
    List<ProofingInvitationDetailVO> selectSentInvitationList(@Param("userId") Long userId);

    /**
     * 分页查询我收到的邀约列表
     *
     * @param page 分页参数
     * @param userId 当前登录用户的ID (即接收者ID)
     * @return 分页邀约详情列表
     */
    IPage<ProofingInvitationDetailVO> selectReceivedInvitationPage(IPage<ProofingInvitationDetailVO> page, @Param("userId") Long userId);

    /**
     * 分页查询我发出的邀约列表
     *
     * @param page 分页参数
     * @param userId 当前登录用户的ID (即发送者ID)
     * @return 分页邀约详情列表
     */
    IPage<ProofingInvitationDetailVO> selectSentInvitationPage(IPage<ProofingInvitationDetailVO> page, @Param("userId") Long userId);

    /**
     * 根据邀约ID查询邀约详情（多表联查）
     *
     * @param invitationId 邀约ID
     * @return 邀约详情
     */
    ProofingInvitationDetailVO selectInvitationDetailById(@Param("invitationId") Long invitationId);

    /**
     * 统计我收到的邀约总数
     *
     * @param userId 当前登录用户的ID (即接收者ID)
     * @return 邀约总数
     */
    Long countReceivedInvitations(@Param("userId") Long userId);

    /**
     * 统计我发出的邀约总数
     *
     * @param userId 当前登录用户的ID (即发送者ID)
     * @return 邀约总数
     */
    Long countSentInvitations(@Param("userId") Long userId);

    int updateStatusById(@Param("invitationId") Long id, @Param("status") Integer status);

    /**
     * 查询用户成功的打样邀约列表（状态为已接受或已发布）
     * 包括用户作为发起人或被邀约人的邀约
     *
     * @param userId 用户ID
     * @return 成功的打样邀约详情列表
     */
    List<ProofingInvitationDetailVO> selectSuccessfulInvitationsByUserId(@Param("userId") Long userId);
}
