package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 众筹项目Mapper接口
 *
 * @author sutran
 * @date 2025-10-10
 */
@Mapper
public interface SdCrowdfundingProjectMapper extends BaseMapper<SdCrowdfundingProject> {

    /**
     * 查询众筹项目
     *
     * @param id 众筹项目主键
     * @return 众筹项目
     */
    SdCrowdfundingProject selectSdCrowdfundingProjectById(Long id);

    /**
     * 查询众筹项目列表
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 众筹项目集合
     */
    List<SdCrowdfundingProject> selectSdCrowdfundingProjectList(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 新增众筹项目
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 结果
     */
    int insertSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject);



    /**
     * 根据打样邀约ID查询众筹项目
     *
     * @param proofingInvitationId 打样邀约ID
     * @return 众筹项目
     */
    SdCrowdfundingProject selectByProofingInvitationId(@Param("proofingInvitationId") Long proofingInvitationId);

    /**
     * 内部更新众筹项目（仅用于业务逻辑，不对外暴露）
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 结果
     */
    int updateSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 只有在众筹中状态时才更新为成功状态（乐观锁）
     *
     * @param projectId 项目ID
     * @param newStatus 新状态
     * @return 更新行数
     */
    @Update("UPDATE sd_crowdfunding_project SET status = #{newStatus} WHERE id = #{projectId} AND status = 1")
    int updateStatusIfCrowdfunding(@Param("projectId") Long projectId, @Param("newStatus") Integer newStatus);

    /**
     * 根据类型分页查询众筹项目列表
     *
     * @param page 分页对象
     * @param type 类型：published(我发布的)、supported(我购买的)、manufactured(我承接的)
     * @param userId 当前用户ID
     * @return 众筹项目分页数据
     */
    Page<SdCrowdfundingProject> selectPageCrowdfundingProjectListByType(Page<SdCrowdfundingProject> page, @Param("type") String type, @Param("userId") Long userId);
}
