package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.dto.CrowdfundingProjectCreateDTO;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.dto.CrowdfundingDrawClaimDTO;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.design.vo.CrowdfundingProjectDetailVO;
import com.sutran.sd.design.vo.CrowdfundingProjectListVO;
import com.sutran.sd.design.vo.CrowdfundingSupportVO;
import com.sutran.sd.design.vo.CrowdfundingDrawVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 众筹项目管理Controller
 *
 * @author sutran
 * @date 2025-10-10
 */
@RestController
@RequestMapping("/design/crowdfunding")
@RequiredArgsConstructor
public class CrowdfundingController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;

    /**
     * 获取众筹项目列表
     */
    @GetMapping
    public R<List<CrowdfundingProjectListVO>> getCrowdfundingProjects() {
        List<CrowdfundingProjectListVO> list = crowdfundingProjectService.getCrowdfundingProjectList();
        
        if (list == null || list.isEmpty()) {
            return R.ok("暂无众筹项目", list);
        }
        
        return R.ok("成功获取 " + list.size() + " 个众筹项目", list);
    }

    /**
     * 获取众筹项目详细信息
     */
    @GetMapping("/{id}")
    public R<CrowdfundingProjectDetailVO> getCrowdfundingProject(@PathVariable("id") Long id) {
        CrowdfundingProjectDetailVO project = crowdfundingProjectService.getCrowdfundingProjectDetail(id);
        if (project == null) {
            return R.fail("众筹项目不存在");
        }
        return R.ok("获取众筹项目详情成功", project);
    }

    /**
     * 从打样邀约创建众筹项目
     */
    @PostMapping("/create-from-invitation/{invitationId}")
    public R<CrowdfundingProjectDetailVO> createFromInvitation(@PathVariable("invitationId") Long invitationId) {
        try {
            CrowdfundingProjectDetailVO project = crowdfundingProjectService.createFromProofingInvitation(invitationId);
            if (project == null) {
                return R.fail("创建众筹项目失败");
            }
            return R.ok("众筹项目创建成功", project);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("创建众筹项目失败: " + e.getMessage());
        }
    }

    /**
     * 参与众筹
     */
    @PostMapping("/{id}/support")
    public R<CrowdfundingSupportVO> supportProject(@PathVariable("id") Long projectId, @RequestBody CrowdfundingSupportDTO supportDTO) {
        try {
            supportDTO.setProjectId(projectId);
            CrowdfundingSupportVO support = crowdfundingProjectService.supportProject(supportDTO);
            if (support == null) {
                return R.fail("参与众筹失败");
            }
            return R.ok("参与众筹成功", support);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("参与众筹失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的众筹支持记录
     */
    @GetMapping("/my-supports")
    public R<List<CrowdfundingSupportVO>> getMySupports() {
        try {
            List<CrowdfundingSupportVO> list = crowdfundingProjectService.getMySupports();
            if (list == null || list.isEmpty()) {
                return R.ok("暂无支持记录", list);
            }
            return R.ok("成功获取 " + list.size() + " 条支持记录", list);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取支持记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的抽奖记录
     */
    @GetMapping("/my-draws")
    public R<List<CrowdfundingDrawVO>> getMyDraws() {
        try {
            List<CrowdfundingDrawVO> list = crowdfundingProjectService.getMyDraws();
            if (list == null || list.isEmpty()) {
                return R.ok("暂无抽奖记录", list);
            }
            return R.ok("成功获取 " + list.size() + " 条抽奖记录", list);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取抽奖记录失败: " + e.getMessage());
        }
    }

    /**
     * 领取奖品
     */
    @PostMapping("/draw/claim")
    public R<Void> claimPrize(@RequestBody CrowdfundingDrawClaimDTO claimDTO) {
        try {
            boolean success = crowdfundingProjectService.claimPrize(claimDTO);
            if (success) {
                return R.ok("奖品领取成功");
            } else {
                return R.fail("奖品领取失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("奖品领取失败: " + e.getMessage());
        }
    }

    /**
     * 开始抽奖
     */
    @PostMapping("/{id}/start-draw")
    public R<Void> startDraw(@PathVariable("id") Long id) {
        try {
            boolean success = crowdfundingProjectService.startDraw(id);
            if (success) {
                return R.ok("抽奖已开始");
            } else {
                return R.fail("开始抽奖失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("开始抽奖失败: " + e.getMessage());
        }
    }

    /**
     * 执行抽奖
     */
    @PostMapping("/{id}/execute-draw")
    public R<Void> executeDraw(@PathVariable("id") Long id) {
        try {
            boolean success = crowdfundingProjectService.executeDraw(id);
            if (success) {
                return R.ok("抽奖执行成功");
            } else {
                return R.fail("执行抽奖失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("执行抽奖失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发众筹项目状态检查
     */
    @PostMapping("/auto-update-status")
    public R<String> autoUpdateProjectStatus() {
        try {
            crowdfundingProjectService.autoUpdateProjectStatus();
            return R.ok("众筹项目状态自动检查完成");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("众筹项目状态检查失败: " + e.getMessage());
        }
    }

    /**
     * 厂家上传实物照片
     */
    @PostMapping("/{id}/upload-photos")
    public R<Void> uploadManufacturerPhotos(@PathVariable("id") Long id, @RequestBody String photos) {
        try {
            boolean success = crowdfundingProjectService.uploadManufacturerPhotos(id, photos);
            if (success) {
                return R.ok("照片上传成功");
            } else {
                return R.fail("照片上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("照片上传失败: " + e.getMessage());
        }
    }

    /**
     * 释放资金给厂家
     */
    @PostMapping("/{id}/release-funds")
    public R<Void> releaseFundsToManufacturer(@PathVariable("id") Long id) {
        try {
            boolean success = crowdfundingProjectService.releaseFundsToManufacturer(id);
            if (success) {
                return R.ok("资金释放成功");
            } else {
                return R.fail("资金释放失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("资金释放失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发退款
     */
    @PostMapping("/{id}/manual-refund")
    public R<String> manualRefund(@PathVariable("id") Long id) {
        try {
            boolean success = crowdfundingProjectService.manualRefund(id);
            if (success) {
                return R.ok("退款处理成功");
            } else {
                return R.fail("退款处理失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("退款处理失败: " + e.getMessage());
        }
    }
}
