package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.mapper.SdPresaleProjectMapper;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.PresaleProjectDetailVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预售项目Service业务层处理
 *
 * @author sutran
 * @date 2025-01-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdPresaleProjectServiceImpl implements ISdPresaleProjectService {

    private final SdPresaleProjectMapper presaleProjectMapper;

    @Override
    public SdPresaleProject selectSdPresaleProjectById(Long id) {
        return presaleProjectMapper.selectSdPresaleProjectById(id);
    }

    @Override
    public List<SdPresaleProject> selectSdPresaleProjectList(SdPresaleProject sdPresaleProject) {
        return presaleProjectMapper.selectSdPresaleProjectList(sdPresaleProject);
    }

    @Override
    public TableDataInfo<SdPresaleProject> selectPagePresaleProjectList(SdPresaleProject sdPresaleProject, PageQuery pageQuery) {
        Page<SdPresaleProject> page = pageQuery.build();
        IPage<SdPresaleProject> result = presaleProjectMapper.selectPagePresaleProjectList(page, sdPresaleProject);
        return TableDataInfo.build(result);
    }

    @Override
    public R<List<PresaleProjectListVO>> getPresaleProjectList() {
        // TODO: 实现获取预售项目列表逻辑
        log.info("获取预售项目列表");
        return R.ok(new ArrayList<>());
    }

    @Override
    public R<PresaleProjectDetailVO> getPresaleProjectDetail(Long id) {
        // TODO: 实现获取预售项目详情逻辑
        log.info("获取预售项目详情: ID={}", id);
        return R.ok(new PresaleProjectDetailVO());
    }

    @Override
    public R<List<PresaleProjectListVO>> getManufacturerPresaleProjects() {
        // TODO: 实现获取厂家参与的预售项目列表逻辑
        log.info("获取厂家参与的预售项目列表");
        return R.ok(new ArrayList<>());
    }

    @Override
    public R<List<PresaleProjectListVO>> getCreatorPresaleProjects() {
        // TODO: 实现获取发起人的预售项目列表逻辑
        log.info("获取发起人的预售项目列表");
        return R.ok(new ArrayList<>());
    }
}
