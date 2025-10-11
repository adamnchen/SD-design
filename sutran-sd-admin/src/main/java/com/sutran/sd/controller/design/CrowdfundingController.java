package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 众筹项目管理Controller
 *
 * @author chenshan
 * @date 2025-10-10
 */
@RestController
@RequestMapping("/design/crowdfunding")
@RequiredArgsConstructor
public class CrowdfundingController extends BaseController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;

    /**
     * 查询众筹项目列表
     */
    @GetMapping("/list")
    public TableDataInfo<SdCrowdfundingProject> list(SdCrowdfundingProject sdCrowdfundingProject, PageQuery pageQuery) {
        return crowdfundingProjectService.selectPageCrowdfundingProjectList(sdCrowdfundingProject, pageQuery);
    }

    /**
     * 获取众筹项目详细信息
     */
    @GetMapping(value = "/{id}")
    public R<SdCrowdfundingProject> getInfo(@PathVariable("id") Long id) {
        return R.ok(crowdfundingProjectService.selectSdCrowdfundingProjectById(id));
    }

    /**
     * 新增众筹项目
     */
    @PostMapping
    public R<Void> add(@RequestBody SdCrowdfundingProject sdCrowdfundingProject) {
        return toAjax(crowdfundingProjectService.insertSdCrowdfundingProject(sdCrowdfundingProject));
    }



    /**
     * 获取众筹项目列表（前端展示用）
     */
    @GetMapping("/projects")
    public R<List<com.sutran.sd.design.vo.CrowdfundingProjectListVO>> getProjects() {
        return R.ok(crowdfundingProjectService.getCrowdfundingProjectList());
    }

    /**
     * 获取众筹项目详情（前端展示用）
     */
    @GetMapping("/projects/{id}")
    public R<com.sutran.sd.design.vo.CrowdfundingProjectDetailVO> getProjectDetail(@PathVariable Long id) {
        return R.ok(crowdfundingProjectService.getCrowdfundingProjectDetail(id));
    }
}
