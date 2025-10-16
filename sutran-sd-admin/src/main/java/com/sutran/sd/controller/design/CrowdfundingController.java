package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.dto.CrowdfundingProjectSimpleCreateDTO;
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
     * 查询众筹项目列表（通用）
     */
    @GetMapping("/list")
    public TableDataInfo<SdCrowdfundingProject> list(SdCrowdfundingProject sdCrowdfundingProject, PageQuery pageQuery) {
        return crowdfundingProjectService.selectPageCrowdfundingProjectList(sdCrowdfundingProject, pageQuery);
    }

    /**
     * 根据类型查询众筹项目列表
     * @param type 类型：published(我发布的)、supported(我购买的)、manufactured(我承接的)
     */
    @GetMapping("/list/{type}")
    public TableDataInfo<SdCrowdfundingProject> listByType(@PathVariable String type, PageQuery pageQuery) {
        return crowdfundingProjectService.selectPageCrowdfundingProjectListByType(type, pageQuery);
    }

    /**
     * 获取众筹项目详细信息
     */
    @GetMapping(value = "/{id}")
    public R<SdCrowdfundingProject> getInfo(@PathVariable("id") Long id) {
        return R.ok(crowdfundingProjectService.selectSdCrowdfundingProjectById(id));
    }



    /**
     * 简化新增众筹项目（前端只需要传厂家ID，其他信息从打样邀约中获取）
     */
    @PostMapping
    public R<Void> addSimple(@RequestBody CrowdfundingProjectSimpleCreateDTO createDTO) {
        crowdfundingProjectService.insertSdCrowdfundingProjectSimple(createDTO);
        return R.ok("发布成功");
    }

    /**
     * 获取进行中的众筹项目列表（前端展示用）
     */
    @GetMapping("/projects")
    public R<List<com.sutran.sd.design.vo.CrowdfundingProjectListVO>> getProjects() {
        return R.ok(crowdfundingProjectService.getActiveCrowdfundingProjects());
    }

    /**
     * 获取众筹项目详情（前端展示用）
     */
    @GetMapping("/projects/{id}")
    public R<com.sutran.sd.design.vo.CrowdfundingProjectDetailVO> getProjectDetail(@PathVariable Long id) {
        return R.ok(crowdfundingProjectService.getCrowdfundingProjectDetail(id));
    }

    /**
     * 获取厂家参与的众筹项目列表
     */
    @GetMapping("/manufacturer/projects")
    public R<List<com.sutran.sd.design.vo.CrowdfundingProjectListVO>> getManufacturerProjects() {
        return R.ok(crowdfundingProjectService.getManufacturerProjects());
    }

    /**
     * 获取厂家参与的众筹成功项目列表
     */
    @GetMapping("/manufacturer/successful-projects")
    public R<List<com.sutran.sd.design.vo.CrowdfundingProjectListVO>> getManufacturerSuccessfulProjects() {
        return R.ok(crowdfundingProjectService.getManufacturerSuccessfulProjects());
    }



}
