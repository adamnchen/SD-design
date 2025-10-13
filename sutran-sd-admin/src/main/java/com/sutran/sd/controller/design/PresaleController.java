package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预售项目管理Controller
 *
 * @author sutran
 * @date 2025-01-12
 */
@Tag(name = "预售项目管理", description = "预售项目相关接口")
@RestController
@RequestMapping("/design/presale")
@RequiredArgsConstructor
public class PresaleController extends BaseController {

    private final ISdPresaleProjectService presaleProjectService;

    /**
     * 查询预售项目列表
     */
    @Operation(summary = "查询预售项目列表", description = "分页查询预售项目列表")
    @GetMapping("/list")
    public TableDataInfo<SdPresaleProject> list(SdPresaleProject sdPresaleProject, PageQuery pageQuery) {
        return presaleProjectService.selectPagePresaleProjectList(sdPresaleProject, pageQuery);
    }

    /**
     * 获取预售项目详细信息
     */
    @Operation(summary = "获取预售项目详情", description = "根据ID获取预售项目详细信息")
    @GetMapping(value = "/{id}")
    public R<SdPresaleProject> getInfo(@PathVariable("id") Long id) {
        return R.ok(presaleProjectService.selectSdPresaleProjectById(id));
    }

    /**
     * 获取预售项目列表（前端展示用）
     */
    @Operation(summary = "获取预售项目列表", description = "获取所有预售项目列表，用于前端展示")
    @GetMapping("/projects")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getProjects() {
        return presaleProjectService.getPresaleProjectList();
    }

    /**
     * 获取预售项目详情（前端展示用）
     */
    @Operation(summary = "获取预售项目详情", description = "根据ID获取预售项目详情，用于前端展示")
    @GetMapping("/projects/{id}")
    public R<com.sutran.sd.design.vo.PresaleProjectDetailVO> getProjectDetail(@PathVariable Long id) {
        return presaleProjectService.getPresaleProjectDetail(id);
    }

    /**
     * 获取厂家参与的预售项目列表
     */
    @Operation(summary = "获取厂家预售项目", description = "获取当前厂家参与的预售项目列表")
    @GetMapping("/manufacturer/projects")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getManufacturerProjects() {
        return presaleProjectService.getManufacturerPresaleProjects();
    }

    /**
     * 获取发起人的预售项目列表
     */
    @Operation(summary = "获取发起人预售项目", description = "获取当前发起人的预售项目列表")
    @GetMapping("/creator/projects")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getCreatorProjects() {
        return presaleProjectService.getCreatorPresaleProjects();
    }
}
