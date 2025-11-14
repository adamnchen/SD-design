package com.sutran.sd.controller.design;

import cn.dev33.satoken.annotation.SaIgnore;
import com.ijpay.alipay.AliPayApiConfig;
import com.sutran.sd.common.annotation.RequireMember;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.dto.PresaleOrderCreateDTO;
import com.sutran.sd.design.dto.PresaleProjectPublishDTO;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.PresaleOrderDetailVO;
import com.sutran.sd.design.vo.PresaleOrderListVO;
import com.sutran.sd.pay.config.AliPayConfig;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.controller.BaseAliPayApiController;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.impl.PayOrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 预售项目管理Controller
 *
 * @author 陈善
 * @date 2025-10-19
 */
@Tag(name = "预售项目管理", description = "预售项目相关接口")
@RestController
@RequestMapping("/design/presale")
@RequiredArgsConstructor
public class PresaleController extends BaseAliPayApiController {

    private final ISdPresaleProjectService presaleProjectService;
    private final Map<String, BasePayNotifyService> payNotifyServiceMap;
    private final AliPayService aliPayService;
    private final AliPayConfig aliPayConfig;
    private final PayOrderServiceImpl payOrderService;


    /**
     * 获取支付宝配置：主要是为了让当前线程上下文都能加入支付宝配置
     */
    @Override
    public AliPayApiConfig getApiConfig() {
        return aliPayService.getConfig();
    }
    /**
     * 查询预售项目列表
     */
    @Operation(summary = "查询预售项目列表", description = "分页查询预售项目列表")
    @GetMapping("/list")
    public R<TableDataInfo<SdPresaleProject>> list(SdPresaleProject sdPresaleProject, PageQuery pageQuery) {
        return R.ok(presaleProjectService.selectPagePresaleProjectList(sdPresaleProject, pageQuery));
    }

    /**
     * 获取预售项目详细信息
     */
    @Operation(summary = "获取预售项目详情", description = "根据ID获取预售项目详细信息")
    @GetMapping(value = "/{id}")
    public R<com.sutran.sd.design.vo.PresaleProjectDetailVO> getInfo(@PathVariable("id") Long id) {
        return presaleProjectService.getPresaleProjectDetail(id);
    }

    /**
     * 获取预售项目列表（非分页）
     */
    @Operation(summary = "获取预售项目列表", description = "获取所有销售中的预售项目列表")
    @GetMapping("/projects")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getProjects() {
        return presaleProjectService.getPresaleProjectList();
    }

    /**
     * 获取预售项目列表（分页）
     */
    @Operation(summary = "获取预售项目列表（分页）", description = "分页获取所有销售中的预售项目列表")
    @GetMapping("/projects/page")
    public R<TableDataInfo<com.sutran.sd.design.vo.PresaleProjectListVO>> getProjectsPage(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getPresaleProjectListPage(pageQuery));
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
     * 获取厂家参与的预售项目列表（分页）
     */
    @Operation(summary = "获取厂家预售项目（分页）", description = "分页获取当前厂家参与的预售项目列表")
    @GetMapping("/manufacturer/projects/page")
    public R<TableDataInfo<com.sutran.sd.design.vo.PresaleProjectListVO>> getManufacturerProjectsPage(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getManufacturerPresaleProjectsPage(pageQuery));
    }

    /**
     * 获取发起人的预售项目列表
     */
    @Operation(summary = "获取发起人预售项目", description = "获取当前发起人的预售项目列表")
    @GetMapping("/creator/projects/initiate")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getCreatorProjects() {
        return presaleProjectService.getCreatorPresaleProjects();
    }

    /**
     * 获取发起人的预售项目列表（分页）
     */
    @Operation(summary = "获取发起人预售项目（分页）", description = "分页获取当前发起人的预售项目列表")
    @GetMapping("/creator/projects/initiate/page")
    public R<TableDataInfo<com.sutran.sd.design.vo.PresaleProjectListVO>> getCreatorProjectsPage(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getCreatorPresaleProjectsPage(pageQuery));
    }

    /**
     * 获取我购买的预售项目列表
     */
    @Operation(summary = "获取我购买的预售项目", description = "获取我购买的预售项目列表")
    @GetMapping("/creator/projects/purchase")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getBuyerProjects() {
        return presaleProjectService.getBuyerPresaleProjects();
    }

    /**
     * 获取我购买的预售项目列表（分页）
     */
    @Operation(summary = "获取我购买的预售项目（分页）", description = "分页获取我购买的预售项目列表")
    @GetMapping("/creator/projects/purchase/page")
    public R<TableDataInfo<com.sutran.sd.design.vo.PresaleProjectListVO>> getBuyerProjectsPage(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getBuyerPresaleProjectsPage(pageQuery));
    }

    /**
     * 购买预售商品
     */
    @Operation(summary = "购买预售商品", description = "创建预售订单并生成支付二维码")
    @PostMapping("/buy")
    public R<String> buyPresaleProduct(@Valid @RequestBody PresaleOrderCreateDTO createDTO) {
        return presaleProjectService.createPresaleOrder(createDTO);
    }

    /**
     * 获取我的订单列表
     */
    @Operation(summary = "获取我的订单列表", description = "获取当前用户的预售订单列表")
    @GetMapping("/orders")
    public R<List<PresaleOrderListVO>> getMyOrders() {
        return presaleProjectService.getMyPresaleOrders();
    }

    /**
     * 获取我的预售订单列表（分页）
     */
    @Operation(summary = "获取我的预售订单列表（分页）", description = "分页获取当前用户的所有预售订单")
    @GetMapping("/orders/page")
    public R<TableDataInfo<PresaleOrderListVO>> getMyPresaleOrdersPage(PageQuery pageQuery) {
        return R.ok(presaleProjectService.getMyPresaleOrdersPage(pageQuery));
    }

    /**
     * 获取预售项目的订单列表（分页）- 发货用
     */
    @Operation(summary = "获取预售项目订单列表（分页）", description = "分页获取指定预售项目的订单列表，用于发货管理")
    @GetMapping("/orders/page/{id}")
    public R<TableDataInfo<PresaleOrderListVO>> getProjectOrdersPage(@PathVariable("id") Long projectId, PageQuery pageQuery) {
        return R.ok(presaleProjectService.getProjectPresaleOrdersPage(projectId, pageQuery));
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情", description = "根据订单号获取订单详细信息")
    @GetMapping("/orders/{orderNo}")
    public R<PresaleOrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        return presaleProjectService.getPresaleOrderDetail(orderNo);
    }

    /**
     * 获取支付二维码
     */
    @Operation(summary = "获取支付二维码", description = "根据订单号获取支付二维码")
    @GetMapping("/payment/qr/{orderNo}")
    public R<String> getPaymentQr(@PathVariable String orderNo) {
        try {
            Long userId = LoginHelper.getUserId();
            String qrCode = payOrderService.getPayQr(orderNo, userId);
            return R.ok("获取支付二维码成功",qrCode);
        } catch (Exception e) {
            return R.fail("获取支付二维码失败: " + e.getMessage());
        }
    }

    /**
     * 发布预售项目
     */
    @Operation(summary = "发布预售项目", description = "厂家发布预售项目，包含AI设计图和实物照片")
    @PostMapping("/publish")
    @RequireMember(value = "发布预售项目", newUserBenefit = {RequireMember.NewUserBenefitType.DESIGN})
    public R<String> publishPresaleProject(@Valid @RequestBody PresaleProjectPublishDTO publishDTO) {
        return presaleProjectService.publishPresaleProject(publishDTO);
    }

    /**
     * 上传实物照片
     */
    @Operation(summary = "上传实物照片", description = "为众筹成功的项目上传实物照片，用于发布预售")
    @PostMapping(value = "/upload/photos", consumes = "multipart/form-data")
    public R<String> uploadManufacturerPhotos(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "proofingInvitationId") Long proofingInvitationId) {
        return presaleProjectService.uploadManufacturerPhotos(file, proofingInvitationId);
    }

    /**
     * 支付宝支付成功回调
     */
    @PostMapping("/payment/alipay/notify")
    @SaIgnore
    public String alipayNotify(HttpServletRequest request) {
        return payNotifyServiceMap.get(PayNotifyServer.PRESALE_ORDER_NOTIFY).handleNotify(request, aliPayConfig.getAliPayCertPath());
    }

}
