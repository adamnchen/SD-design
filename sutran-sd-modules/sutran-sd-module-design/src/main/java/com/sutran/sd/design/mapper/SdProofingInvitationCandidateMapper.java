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
}


