package com.sutran.sd.design.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.design.domain.SdPresaleProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 预售项目Mapper接口
 *
 * @author sutran
 * @date 2025-01-12
 */
@Mapper
public interface SdPresaleProjectMapper extends BaseMapperPlus<SdPresaleProjectMapper,SdPresaleProject,SdPresaleProject> {

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

    /**
     * 多表联查预售项目列表（带用户信息）
     *
     * @param status 项目状态
     * @param now    当前时间
     * @return 预售项目分页数据
     */
    List<SdPresaleProject> selectPresaleProjectListWithUserInfo(@Param("status") Integer status, @Param("now") Date now);

    /**
     * 分页查询多表联查预售项目列表（带用户信息）
     *
     * @param page 分页对象
     * @param status 项目状态
     * @param now    当前时间
     * @return 预售项目分页数据
     */
    IPage<SdPresaleProject> selectPresaleProjectListWithUserInfoOfPage(Page<SdPresaleProject> page, @Param("status") Integer status, @Param("now") Date now);

    /**
     * 多表联查用户参与的预售项目列表
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @param userType 用户类型：creator=发起人，manufacturer=厂家，buyer=买家
     * @return 预售项目分页数据
     */
    IPage<SdPresaleProject> selectUserPresaleProjects(IPage<SdPresaleProject> page, @Param("userId") Long userId, @Param("userType") String userType);

    /**
     * 新增预售项目（使用自定义插入语句，确保所有字段都被正确插入）
     *
     * @param sdPresaleProject 预售项目
     * @return 结果
     */
    int insertSdPresaleProject(SdPresaleProject sdPresaleProject);

    /**
     * 处理过期数据
     * @param now 日期
     */
    void dealExpireData(@Param("now") Date now);
}
