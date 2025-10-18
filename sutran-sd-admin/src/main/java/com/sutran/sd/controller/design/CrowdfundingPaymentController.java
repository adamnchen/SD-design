package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.dto.CrowdfundingSupportDTO;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 众筹支付和支持记录Controller
 *
 * @author chenshan
 * @date 2025-10-10
 */
@Slf4j
@RestController
@RequestMapping("/design/crowdfunding")
@RequiredArgsConstructor
public class CrowdfundingPaymentController extends BaseController {

    private final ISdCrowdfundingProjectService crowdfundingProjectService;
    private final PayOrderService payOrderService;
    private final AliPayService aliPayService;
    private final Map<String, BasePayNotifyService> payNotifyServiceMap;

    /**
     * 参与众筹支持
     */
    @PostMapping("/support")
    public R<String> supportProject(@Valid @RequestBody CrowdfundingSupportDTO supportDTO) {
        try {
            String orderNo = crowdfundingProjectService.createSupportOrder(supportDTO);
            return R.ok("参与众筹成功", orderNo);
        } catch (Exception e) {
            return R.fail("参与众筹失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单号查询支持记录
     */
    @GetMapping("/support/order/{orderNo}")
    public R<SdCrowdfundingSupport> getByOrderNo(@PathVariable String orderNo) {
        SdCrowdfundingSupport support = crowdfundingProjectService.getSupportByOrderNo(orderNo);
        return R.ok(support);
    }

    /**
     * 获取我的支持记录
     */
    @GetMapping("/support/my-supports")
    public R<List<com.sutran.sd.design.vo.CrowdfundingSupportVO>> getMySupports() {
        return R.ok(crowdfundingProjectService.getMySupports());
    }

    /**
     * 获取我的抽奖记录
     */
    @GetMapping("/support/my-draws")
    public R<List<com.sutran.sd.design.vo.CrowdfundingDrawVO>> getMyDraws() {
        return R.ok(crowdfundingProjectService.getMyDraws());
    }

    /**
     * 根据订单号获取支付二维码
     */
    @GetMapping("/payment/qr/{orderNo}")
    public R<String> getPaymentQr(@PathVariable String orderNo) {
        try {
            String qrCode = payOrderService.getPayQr(orderNo, getUserId());
            return R.ok(qrCode);
        } catch (Exception e) {
            log.error("获取支付二维码失败: 订单号={}", orderNo, e);
            return R.fail("获取支付二维码失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝支付成功回调
     */
    @PostMapping("/payment/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        return payNotifyServiceMap.get(PayNotifyServer.PROOF_CROWDFUND_NOTIFY).handleNotify(request,aliPayService.getConfig());
    }

}
