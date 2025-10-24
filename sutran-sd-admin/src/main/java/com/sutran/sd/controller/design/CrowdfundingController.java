package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.dto.CrowdfundingProjectSimpleCreateDTO;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.common.helper.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 众筹项目管理Controller
 *
 * @author chenshan
 * @date 2025-10-10
 */
@RestController
@RequestMapping("/design/crowdfunding")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "众筹项目管理", description = "众筹项目相关接口")
public class CrowdfundingController extends BaseController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;
    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;

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
     * 商家修改抽奖数量
     */
    @PutMapping("/{projectId}/draw-number")
    @Operation(summary = "修改抽奖数量", description = "商家可以修改众筹项目的抽奖数量")
    public R<String> updateDrawNumber(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "新的抽奖数量", required = true)
            @RequestBody @Validated Map<String, Integer> request) {
        
        try {
            Integer newDrawNumber = request.get("drawNumber");
            if (newDrawNumber == null) {
                return R.fail("抽奖数量不能为空");
            }
            
            if (newDrawNumber < 2) {
                return R.fail("抽奖数量必须大于等于2个");
            }
            
            // 查询项目信息
            SdCrowdfundingProject project = crowdfundingProjectService.selectSdCrowdfundingProjectById(projectId);
            if (project == null) {
                return R.fail("众筹项目不存在");
            }
            
            // 检查权限：只有厂家可以修改
            Long currentUserId = LoginHelper.getUserId();
            if (!currentUserId.equals(project.getManufacturerUserId())) {
                return R.fail("权限不足，只有厂家可以修改抽奖数量");
            }
            
            // 检查项目状态：只有众筹中的项目可以修改
            if (project.getStatus() != 1) { // 1表示众筹中
                return R.fail("只有众筹中的项目可以修改抽奖数量");
            }
            
            // 检查抽奖数量不能超过样品总数
            if (newDrawNumber > project.getTotalSamples()) {
                return R.fail("抽奖数量不能超过样品总数");
            }
            
            // 更新抽奖数量
            project.setDrawNumber(newDrawNumber);
            crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            
            log.info("[众筹项目] 修改抽奖数量成功: 项目ID={}, 厂家ID={}, 新抽奖数量={}", 
                projectId, currentUserId, newDrawNumber);
            
            return R.ok("修改抽奖数量成功");
            
        } catch (Exception e) {
            log.error("[众筹项目] 修改抽奖数量失败: 项目ID={}", projectId, e);
            return R.fail("修改抽奖数量失败: " + e.getMessage());
        }
    }

}
