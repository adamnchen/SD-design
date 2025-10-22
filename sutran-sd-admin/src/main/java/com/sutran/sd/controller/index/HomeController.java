package com.sutran.sd.controller.index;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 首页控制器
 *
 * @author sutran
 * @date 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
@Tag(name = "首页接口", description = "首页展示相关接口")
public class HomeController extends BaseController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;
    private final ISdPresaleProjectService presaleProjectService;

    /**
     * 获取首页项目列表（众筹+预售，顺序打乱）
     */
    @Operation(summary = "获取首页项目列表", description = "获取所有进行中的众筹和预售项目，顺序打乱")
    @GetMapping("/projects")
    public R<List<Object>> getHomeProjects() {
        log.info("获取首页项目列表");

        try {
            // 1. 获取进行中的众筹项目
            List<CrowdfundingProjectListVO> crowdfundingProjects = crowdfundingProjectService.getActiveCrowdfundingProjects();
            if (crowdfundingProjects == null) {
                crowdfundingProjects = new ArrayList<>();
            }

            // 2. 获取进行中的预售项目
            R<List<PresaleProjectListVO>> presaleResult = presaleProjectService.getPresaleProjectList();
            List<PresaleProjectListVO> presaleProjects = presaleResult.getData() != null ? presaleResult.getData() : new ArrayList<>();

            // 3. 合并项目列表
            List<Object> allProjects = new ArrayList<>();
            allProjects.addAll(crowdfundingProjects);
            allProjects.addAll(presaleProjects);

            // 4. 打乱顺序
            Collections.shuffle(allProjects);

            log.info("首页项目列表获取成功: 众筹项目={}个, 预售项目={}个, 总计={}个", 
                crowdfundingProjects.size(), presaleProjects.size(), allProjects.size());

            return R.ok(allProjects);

        } catch (Exception e) {
            log.error("获取首页项目列表失败", e);
            return R.fail("获取首页项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取首页众筹项目列表
     */
    @Operation(summary = "获取首页众筹项目", description = "获取所有进行中的众筹项目")
    @GetMapping("/crowdfunding")
    public R<List<CrowdfundingProjectListVO>> getHomeCrowdfundingProjects() {
        log.info("获取首页众筹项目列表");

        try {
            List<CrowdfundingProjectListVO> projects = crowdfundingProjectService.getActiveCrowdfundingProjects();
            if (projects == null) {
                projects = new ArrayList<>();
            }

            log.info("首页众筹项目列表获取成功: 项目数量={}个", projects.size());
            return R.ok(projects);

        } catch (Exception e) {
            log.error("获取首页众筹项目列表失败", e);
            return R.fail("获取首页众筹项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取首页预售项目列表
     */
    @Operation(summary = "获取首页预售项目", description = "获取所有进行中的预售项目")
    @GetMapping("/presale")
    public R<List<PresaleProjectListVO>> getHomePresaleProjects() {
        log.info("获取首页预售项目列表");

        try {
            R<List<PresaleProjectListVO>> result = presaleProjectService.getPresaleProjectList();
            List<PresaleProjectListVO> projects = result.getData() != null ? result.getData() : new ArrayList<>();

            log.info("首页预售项目列表获取成功: 项目数量={}个", projects.size());
            return R.ok(projects);

        } catch (Exception e) {
            log.error("获取首页预售项目列表失败", e);
            return R.fail("获取首页预售项目列表失败: " + e.getMessage());
        }
    }
}
