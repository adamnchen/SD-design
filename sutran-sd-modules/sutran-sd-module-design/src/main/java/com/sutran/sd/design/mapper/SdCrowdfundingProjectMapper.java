package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
     * 修改众筹项目
     *
     * @param sdCrowdfundingProject 众筹项目
     * @return 结果
     */
    int updateSdCrowdfundingProject(SdCrowdfundingProject sdCrowdfundingProject);

    /**
     * 删除众筹项目
     *
     * @param id 众筹项目主键
     * @return 结果
     */
    int deleteSdCrowdfundingProjectById(Long id);

    /**
     * 批量删除众筹项目
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSdCrowdfundingProjectByIds(@Param("ids") Long[] ids);

    /**
     * 只有在众筹中状态时才更新为成功状态（乐观锁）
     *
     * @param projectId 项目ID
     * @param newStatus 新状态
     * @return 更新行数
     */
    @Update("UPDATE sd_crowdfunding_project SET status = #{newStatus} WHERE id = #{projectId} AND status = 1")
    int updateStatusIfCrowdfunding(@Param("projectId") Long projectId, @Param("newStatus") Integer newStatus);
}
