package com.sutran.sd.controller.design;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.ijpay.alipay.AliPayApiConfig;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.design.service.ISdCrowdfundingProjectService;
import com.sutran.sd.pay.controller.BaseAliPayApiController;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayOrderService;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.enums.BusinessType;
import com.sutran.sd.pay.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹支付Controller
 *
 * @author sutran
 * @date 2025-10-10
 */
@RestController
@RequestMapping("/design/crowdfunding/pay")
@RequiredArgsConstructor
public class CrowdfundingPayController extends BaseAliPayApiController {

    private final AliPayService aliPayService;
    private final PayOrderService payOrderService;
    private final ISdCrowdfundingProjectService crowdfundingProjectService;

    @Override
    public AliPayApiConfig getApiConfig() {
        return aliPayService.getConfig();
    }

    /**
     * [用户] 创建众筹支持支付订单
     * @param projectId 众筹项目ID
     * @param supportAmount 支持金额
     * @param message 支持留言
     * @param isAnonymous 是否匿名
     * @param response HTTP响应
     */
    @GetMapping(value = "/create-order")
    public void createSupportOrder(@RequestParam Long projectId,
                                  @RequestParam BigDecimal supportAmount,
                                  @RequestParam(required = false) String message,
                                  @RequestParam(defaultValue = "false") Boolean isAnonymous,
                                  HttpServletResponse response) throws IOException {
        try {
            // 1. 创建支付订单
            PayOrder payOrder = new PayOrder();
            payOrder.setOutTradeNo("CF" + System.currentTimeMillis());
            payOrder.setUserId(1L); // TODO: 从当前登录用户获取
            payOrder.setUserName("用户" + 1L); // TODO: 从当前登录用户获取
            payOrder.setSubject("众筹项目支持");
            payOrder.setBody("支持众筹项目，参与抽奖机会");
            payOrder.setTotalAmount(supportAmount);
            payOrder.setStatus(0); // 待支付
            payOrder.setChannelType(ChannelType.ALI_PAY.name());
            payOrder.setBusinessType(BusinessType.CROWDFUNDING_SUPPORT.name());
            payOrder.setBusinessId(projectId);
            payOrder.setCreateTime(new Date());
            payOrder.setExpireTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000)); // 30分钟过期
            
            // 保存支付订单
            payOrderService.insert(payOrder);
            
            // 2. 创建支付宝预支付订单
            String qrCode = aliPayService.preCreateOrder(payOrder.getOutTradeNo());
            payOrder.setQrCode(qrCode);
            payOrderService.saveQrCode(payOrder.getOutTradeNo(), qrCode);
            
            // 3. 生成二维码图片返回
            QrCodeUtil.generate(qrCode, 300, 300, "png", response.getOutputStream());
            
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * [回调] 支付宝支付回调
     */
    @PostMapping(value = "/notify_url")
    public String notifyUrl(HttpServletRequest request) {
        try {
            String result = aliPayService.notifyUrl(request);
            
            // 处理众筹支付回调
            if (result != null && result.contains("success")) {
                // 从请求参数中获取订单号
                String outTradeNo = request.getParameter("out_trade_no");
                if (outTradeNo != null && outTradeNo.startsWith("CF")) {
                    // 处理众筹支付成功
                    handleCrowdfundingPaymentSuccess(outTradeNo);
                }
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    /**
     * 处理众筹支付成功
     */
    private void handleCrowdfundingPaymentSuccess(String outTradeNo) {
        try {
            // 1. 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(outTradeNo);
            if (payOrder == null || !payOrder.getBusinessType().equals(BusinessType.CROWDFUNDING_SUPPORT.name())) {
                return;
            }
            
            // 2. 更新支付状态
            payOrder.setStatus(1); // 已支付
            payOrder.setGmtPayment(new Date());
            payOrder.setNotifyTime(new Date());
            payOrder.setNotifyResult("success");
            payOrderService.successPay(outTradeNo, payOrder.getTradeNo(), payOrder.getTotalAmount().toString(), payOrder.getGmtPayment().toString());
            
            // 3. 创建众筹支持记录
            crowdfundingProjectService.createSupportFromPayment(payOrder);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询支付订单状态
     */
    @GetMapping("/order-status/{outTradeNo}")
    public R<PayOrder> getOrderStatus(@PathVariable String outTradeNo) {
        try {
            PayOrder payOrder = payOrderService.detailByOutTradeNo(outTradeNo);
            if (payOrder == null) {
                return R.fail("支付订单不存在");
            }
            return R.ok("查询成功", payOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
