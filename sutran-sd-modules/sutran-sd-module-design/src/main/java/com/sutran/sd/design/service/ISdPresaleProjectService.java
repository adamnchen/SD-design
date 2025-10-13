package com.sutran.sd.design.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.vo.PresaleProjectDetailVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import java.util.List;

/**
 * 预售项目Service接口
 *
 * @author sutran
 * @date 2025-01-12
 */
public interface ISdPresaleProjectService {

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
     * @param sdPresaleProject 预售项目
     * @param pageQuery 分页查询
     * @return 预售项目分页数据
     */
    TableDataInfo<SdPresaleProject> selectPagePresaleProjectList(SdPresaleProject sdPresaleProject, PageQuery pageQuery);

    /**
     * 获取预售项目列表（前端展示用）
     *
     * @return 预售项目列表
     */
    R<List<PresaleProjectListVO>> getPresaleProjectList();

    /**
     * 获取预售项目详情
     *
     * @param id 项目ID
     * @return 预售项目详情
     */
    R<PresaleProjectDetailVO> getPresaleProjectDetail(Long id);

    /**
     * 获取厂家参与的预售项目列表
     *
     * @return 厂家参与的预售项目列表
     */
    R<List<PresaleProjectListVO>> getManufacturerPresaleProjects();

    /**
     * 获取发起人的预售项目列表
     *
     * @return 发起人的预售项目列表
     */
    R<List<PresaleProjectListVO>> getCreatorPresaleProjects();
}
