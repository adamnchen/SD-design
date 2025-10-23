package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.service.ISdCrowdfundingSampleDeliveryService;
import com.sutran.sd.design.vo.SampleDeliveryListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 厂家样品发货管理Controller
 *
 * @author cs
 * @date 2025-01-12
 */
@Tag(name = "厂家发货管理", description = "厂家样品发货相关接口")
@RestController
@RequestMapping("/design/sample-delivery")
@RequiredArgsConstructor
public class SampleDeliveryController extends BaseController {

    private final ISdCrowdfundingSampleDeliveryService sampleDeliveryService;

    /**
     * 获取我承接的已完成众筹项目及发货信息（分页）
     */
    @Operation(summary = "获取我的发货项目", description = "获取我承接的已完成众筹项目，包含中奖者和发起人自留样品信息")
    @GetMapping("/my-projects")
    public R<TableDataInfo<SampleDeliveryListVO>> getMyDeliveryProjects(PageQuery pageQuery) {
        return R.ok(sampleDeliveryService.getMyDeliveryProjects(pageQuery));
    }

    /**
     * 更新快递单号
     */
    @Operation(summary = "更新快递单号", description = "为发货记录更新快递单号")
    @PostMapping("/update-tracking/{id}")
    public R<String> updateTrackingNumber(@PathVariable Long id, @RequestParam String trackingNumber) {
        return sampleDeliveryService.updateTrackingNumber(id, trackingNumber);
    }

    /**
     * 上传样品图片
     */
    @Operation(summary = "上传样品图片", description = "上传样品实物图片")
    @PostMapping("/upload-sample-image/{id}")
    public R<String> uploadSampleImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return sampleDeliveryService.uploadSampleImage(id, file);
    }

    /**
     * 根据众筹项目ID查询所有发货信息
     */
    @Operation(summary = "查询项目发货信息", description = "根据众筹项目ID查询该项目所有中奖人和发起人自留的发货信息")
    @GetMapping("/project/{projectId}")
    public R<TableDataInfo<SampleDeliveryListVO>> getProjectDeliveryInfo(@PathVariable Long projectId, PageQuery pageQuery) {
        return R.ok(sampleDeliveryService.getProjectDeliveryInfo(projectId, pageQuery));
    }
}
