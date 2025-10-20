package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sutran.sd.common.core.domain.entity.SdProofingInvitationCandidate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SdProofingInvitationCandidateMapper extends BaseMapper<SdProofingInvitationCandidate> {

    List<SdProofingInvitationCandidate> selectByInvitationId(@Param("invitationId") Long invitationId);

    SdProofingInvitationCandidate selectOneByInvitationAndInvitee(@Param("invitationId") Long invitationId,
                                                                  @Param("inviteeUserId") Long inviteeUserId);

    List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> selectCandidateVOs(@Param("invitationId") Long invitationId);

    /**
     * 根据商品ID和发起人ID查询候选人
     * 查询同一个商品、同一个发起人的邀约的候选人
     *
     * @param workId 商品ID
     * @param inviterUserId 发起人ID
     * @return 候选人列表
     */
    List<com.sutran.sd.common.core.domain.vo.InvitationCandidateVO> selectCandidateVOsByWorkId(@Param("workId") String workId, @Param("inviterUserId") Long inviterUserId);

    /**
     * 删除其他候选人的记录
     * 当有厂家接受邀约后，删除除当前接受者外的其他候选人
     *
     * @param invitationId 邀约ID
     * @param acceptedUserId 已接受的用户ID
     * @return 删除的记录数
     */
    int deleteOtherCandidates(@Param("invitationId") Long invitationId, @Param("acceptedUserId") Long acceptedUserId);
}


