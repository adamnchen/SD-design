package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.design.domain.SdPresaleProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预售项目Mapper接口
 *
 * @author sutran
 * @date 2025-01-12
 */
@Mapper
public interface SdPresaleProjectMapper extends BaseMapper<SdPresaleProject> {

    /**
     * 查询预售项目
     *
     * @param id 预售项目主键
     * @return 预售项目
     */
    SdPresaleProject selectSdPresaleProjectById(Long id);

    /**
     * 查询预售项目列表
     *
     * @param sdPresaleProject 预售项目
     * @return 预售项目集合
     */
    List<SdPresaleProject> selectSdPresaleProjectList(SdPresaleProject sdPresaleProject);

    /**
     * 分页查询预售项目列表
     *
     * @param page 分页对象
     * @param sdPresaleProject 预售项目
     * @return 预售项目分页数据
     */
    IPage<SdPresaleProject> selectPagePresaleProjectList(IPage<SdPresaleProject> page, @Param("sdPresaleProject") SdPresaleProject sdPresaleProject);

    /**
     * 根据众筹项目ID查询预售项目
     *
     * @param crowdfundingProjectId 众筹项目ID
     * @return 预售项目
     */
    SdPresaleProject selectByCrowdfundingProjectId(Long crowdfundingProjectId);
}
