package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.PresaleProjectListVO;
import cn.dev33.satoken.annotation.SaIgnore;
import com.sutran.sd.draw.domain.vo.SdUserWorkCommentVo;
import com.sutran.sd.draw.domain.vo.UserWorkVo;
import com.sutran.sd.draw.service.SdUserWorkCommentService;
import com.sutran.sd.draw.service.SdUserWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页Controller - 展示正在进行的预售项目和众筹项目
 *
 * @author sutran
 * @date 2025-10-23
 */
@Tag(name = "首页管理", description = "首页展示相关接口")
@RestController
@RequestMapping("/design/home")
@RequiredArgsConstructor
@Slf4j
public class HomePageController {

    private final ISdPresaleProjectService presaleProjectService;
    private final ISdCrowdfundingProjectService crowdfundingProjectService;
    private final SdUserWorkService sdUserWorkService;
    private final SdUserWorkCommentService sdUserWorkCommentService;

    /**
     * 获取销售中的预售项目列表（分页）
     * @param pageQuery 分页查询参数
     */
    @Operation(summary = "获取销售中的预售项目列表（分页）", description = "首页展示所有正在销售中的预售项目")
    @SaIgnore
    @GetMapping("/presale/projects")
    public R<TableDataInfo<PresaleProjectListVO>> getActivePresaleProjects(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getPresaleProjectListPage(pageQuery));
    }

    /**
     * 获取进行中的众筹项目列表（分页）
     * @param pageQuery 分页查询参数
     */
    @Operation(summary = "获取进行中的众筹项目列表（分页）", description = "首页展示所有正在进行中的众筹项目")
    @SaIgnore
    @GetMapping("/crowdfunding/projects")
    public R<TableDataInfo<CrowdfundingProjectListVO>> getActiveCrowdfundingProjects(PageQuery pageQuery) {
        return R.ok(crowdfundingProjectService.getActiveCrowdfundingProjectsPage(pageQuery));
    }

    /**
     * 获取公开作品列表（分页）
     * @param pageQuery 分页查询参数
     */
    @Operation(summary = "获取公开作品列表（分页）", description = "首页展示所有公开的用户作品图片")
    @SaIgnore
    @GetMapping("/public/works")
    public R<TableDataInfo<UserWorkVo>> getPublicWorks(PageQuery pageQuery) {
        return R.ok(sdUserWorkService.getPublicWorksPage(pageQuery));
    }

    /**
     * 获取公开作品的评论列表
     * @param workId 作品ID
     */
    @Operation(summary = "获取公开作品的评论列表", description = "首页展示所有公开作品的评论列表")
    @SaIgnore
    @GetMapping("/public/works/comment")
    public R<List<SdUserWorkCommentVo>> getPublicWorksOfComments(@RequestParam String workId) {
        return R.ok(sdUserWorkCommentService.getPublicWorksOfComments(workId));
    }
}

