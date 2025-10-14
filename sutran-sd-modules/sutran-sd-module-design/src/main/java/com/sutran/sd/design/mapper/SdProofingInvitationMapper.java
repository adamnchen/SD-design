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

}
