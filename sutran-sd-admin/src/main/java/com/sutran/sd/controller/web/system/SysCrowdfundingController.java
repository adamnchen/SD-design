package com.sutran.sd.controller.web.system;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台管理系统 - 众筹项目管理
 *
 * @author sutran
 * @date 2025-10-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/crowdfunding")
@Slf4j
@Tag(name = "后台管理-众筹项目管理", description = "众筹项目后台管理相关接口")
public class SysCrowdfundingController extends BaseController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;

    /**
     * 审核资金释放申请（审核通过）
     */
    @PostMapping("/{projectId}/fund-release/approve")
    @Operation(summary = "审核通过资金释放申请", description = "后台管理员审核通过厂家的资金释放申请，审核通过后会自动执行资金释放")
    public R<String> approveFundRelease(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @RequestBody(required = false) Map<String, String> request) {
        
        try {
            String auditRemark = request != null ? request.get("auditRemark") : null;
            
            boolean success = crowdfundingProjectService.auditFundRelease(projectId, 2, auditRemark);
            
            if (success) {
                log.info("[资金释放审核] 审核通过并自动释放资金成功: 项目ID={}, 审核人={}", projectId, LoginHelper.getUsername());
                return R.ok("审核通过并自动释放资金成功");
            } else {
                return R.fail("审核通过失败");
            }
            
        } catch (Exception e) {
            log.error("[资金释放审核] 审核通过失败: 项目ID={}", projectId, e);
            return R.fail("审核通过失败: " + e.getMessage());
        }
    }

    /**
     * 审核资金释放申请（审核拒绝）
     */
    @PostMapping("/{projectId}/fund-release/reject")
    @Operation(summary = "审核拒绝资金释放申请", description = "后台管理员审核拒绝厂家的资金释放申请")
    public R<String> rejectFundRelease(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @RequestBody Map<String, String> request) {
        
        try {
            String auditRemark = request.get("auditRemark");
            if (auditRemark == null || auditRemark.trim().isEmpty()) {
                return R.fail("拒绝原因不能为空");
            }
            
            boolean success = crowdfundingProjectService.auditFundRelease(projectId, 3, auditRemark);
            
            if (success) {
                log.info("[资金释放审核] 审核拒绝成功: 项目ID={}, 审核人={}, 拒绝原因={}", 
                    projectId, LoginHelper.getUsername(), auditRemark);
                return R.ok("审核拒绝成功");
            } else {
                return R.fail("审核拒绝失败");
            }
            
        } catch (Exception e) {
            log.error("[资金释放审核] 审核拒绝失败: 项目ID={}", projectId, e);
            return R.fail("审核拒绝失败: " + e.getMessage());
        }
    }

    /**
     * 待审核资金释放申请列表（后台）
     */
    @GetMapping("/fund-release/pending")
    @Operation(summary = "待审核资金释放申请列表", description = "查询众筹成功且托管中、审核待审的项目列表")
    public TableDataInfo<SdCrowdfundingProject> pendingFundRelease(PageQuery pageQuery) {
        return crowdfundingProjectService.getPendingFundReleasePage(pageQuery);
    }

}

