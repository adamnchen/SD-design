package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.dto.PresaleOrderCreateDTO;
import com.sutran.sd.design.service.ISdPresaleProjectService;
import com.sutran.sd.design.vo.PresaleOrderDetailVO;
import com.sutran.sd.design.vo.PresaleOrderListVO;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.AliPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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
    private final Map<String, BasePayNotifyService> payNotifyServiceMap;
    private final AliPayService aliPayService;

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

    /**
     * 获取我购买的预售项目列表
     */
    @Operation(summary = "获取发起人预售项目", description = "获取当前发起人的预售项目列表")
    @GetMapping("/creator/projects/{id}")
    public R<List<com.sutran.sd.design.vo.PresaleProjectListVO>> getBuyerProjects(@PathVariable String id) {
        return presaleProjectService.getBuyerPresaleProjects();
    }

    /**
     * 购买预售商品
     */
    @Operation(summary = "购买预售商品", description = "创建预售订单并生成支付二维码")
    @PostMapping("/buy")
    public R<String> buyPresaleProduct(@Valid @RequestBody PresaleOrderCreateDTO createDTO) {
        try {
            String orderNo = presaleProjectService.createPresaleOrder(createDTO);
            return R.ok("订单创建成功", orderNo);
        } catch (Exception e) {
            return R.fail("购买失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的订单列表
     */
    @Operation(summary = "获取我的订单列表", description = "获取当前用户的预售订单列表")
    @GetMapping("/orders")
    public R<List<PresaleOrderListVO>> getMyOrders() {
        return R.ok(presaleProjectService.getMyPresaleOrders());
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情", description = "根据订单号获取订单详细信息")
    @GetMapping("/orders/{orderNo}")
    public R<PresaleOrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        return R.ok(presaleProjectService.getPresaleOrderDetail(orderNo));
    }

    /**
     * 获取支付二维码
     */
    @Operation(summary = "获取支付二维码", description = "根据订单号获取支付二维码")
    @GetMapping("/payment/qr/{orderNo}")
    public R<String> getPaymentQr(@PathVariable String orderNo) {
        try {
            String qrCode = presaleProjectService.getPaymentQr(orderNo);
            return R.ok("获取成功", qrCode);
        } catch (Exception e) {
            return R.fail("获取支付二维码失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝支付成功回调
     */
    @PostMapping("/payment/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        return payNotifyServiceMap.get(PayNotifyServer.PRESALE_ORDER_NOTIFY).handleNotify(request, aliPayService.getConfig());
    }

}
