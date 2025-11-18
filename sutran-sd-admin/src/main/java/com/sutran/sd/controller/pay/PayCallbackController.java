package com.sutran.sd.controller.pay;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.service.PayOrderService;
import com.sutran.sd.design.domain.SdPresaleOrder;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.mapper.SdPresaleOrderMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.enums.PresaleOrderStatus;
import com.sutran.sd.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调接口
 * @author sutran
 * @date 2025-10-24
 */
@Slf4j
@RestController
@RequestMapping("/pay/callback")
@RequiredArgsConstructor
@Tag(name = "支付回调", description = "支付成功回调相关接口")
public class PayCallbackController {

    private final PayOrderService payOrderService;
    private final SdPresaleOrderMapper presaleOrderMapper;
    private final SdCrowdfundingSupportMapper crowdfundingSupportMapper;

    /**
     * 查询支付成功状态
     * @param orderNo 订单号
     * @return 支付状态信息
     */
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付成功状态", description = "根据订单号查询支付成功状态和相关信息")
    public R<Map<String, Object>> getPaymentStatus(
            @Parameter(description = "订单号", required = true)
            @PathVariable String orderNo) {

        try {
            log.info("[支付回调] 查询支付状态: 订单号={}", orderNo);

            // 1. 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(orderNo);
            if (payOrder == null) {
                log.warn("[支付回调] 支付订单不存在: 订单号={}", orderNo);
                return R.fail("订单不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("payOrderId", payOrder.getId());
            result.put("totalAmount", payOrder.getTotalAmount());
            result.put("subject", payOrder.getSubject());
            result.put("payTime", payOrder.getGmtPayment());
            result.put("businessType", payOrder.getBusinessType());

            // 2. 根据业务类型查询具体业务信息
            String businessType = payOrder.getBusinessType();
            if (StringUtils.isBlank(businessType)) {
                log.warn("[支付回调] 业务类型为空: 订单号={}", orderNo);
                return R.fail("业务类型未知");
            }

            // 3. 处理预售订单
            if ("PRESALE".equals(businessType)) {
                SdPresaleOrder presaleOrder = presaleOrderMapper.selectByOrderNo(orderNo);
                if (presaleOrder != null) {
                    Map<String, Object> businessInfo = new HashMap<>();
                    businessInfo.put("orderId", presaleOrder.getId());
                    businessInfo.put("projectId", presaleOrder.getProjectId());
                    businessInfo.put("productTitle", presaleOrder.getProductTitle());
                    businessInfo.put("quantity", presaleOrder.getQuantity());
                    businessInfo.put("finalTotalAmount", presaleOrder.getFinalTotalAmount());
                    businessInfo.put("orderStatus", presaleOrder.getOrderStatus());
                    businessInfo.put("orderStatusText", getPresaleOrderStatusText(presaleOrder.getOrderStatus()));
                    businessInfo.put("receiverName", presaleOrder.getReceiverName());
                    businessInfo.put("receiverPhone", presaleOrder.getReceiverPhone());
                    businessInfo.put("receiverAddress", buildFullReceiverAddress(presaleOrder.getReceiverArea(), presaleOrder.getReceiverAddress()));

                    // 如果有退款金额，显示退款信息
                    if (presaleOrder.getRefundAmount() != null && presaleOrder.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                        businessInfo.put("refundAmount", presaleOrder.getRefundAmount());
                        businessInfo.put("refundTime", presaleOrder.getRefundTime());
                        businessInfo.put("refundReason", presaleOrder.getRefundReason());
                    }

                    result.put("businessInfo", businessInfo);
                    result.put("businessTypeText", "预售订单");
                }
            }
            // 4. 处理众筹支持
            else if ("PROOF_CROWDFUND".equals(businessType)) {
                SdCrowdfundingSupport support = crowdfundingSupportMapper.selectByOrderNo(orderNo);
                if (support != null) {
                    Map<String, Object> businessInfo = new HashMap<>();
                    businessInfo.put("supportId", support.getId());
                    businessInfo.put("projectId", support.getProjectId());
                    businessInfo.put("userName", support.getUserName());
                    businessInfo.put("supportAmount", support.getSupportAmount());
                    businessInfo.put("status", support.getStatus());
                    businessInfo.put("statusText", getCrowdfundingSupportStatusText(support.getStatus()));
                    businessInfo.put("drawStatus", support.getDrawStatus());
                    businessInfo.put("drawStatusText", getDrawStatusText(support.getDrawStatus()));
                    businessInfo.put("isWinner", support.getIsWinner());
                    businessInfo.put("prizeInfo", support.getPrizeInfo());
                    businessInfo.put("receiverName", support.getReceiverName());
                    businessInfo.put("receiverPhone", support.getReceiverPhone());
                    businessInfo.put("receiverAddress", buildFullReceiverAddress(support.getReceiverArea(), support.getReceiverAddress()));

                    // 如果有退款信息，显示退款信息
                    if (support.getRefundTime() != null) {
                        businessInfo.put("refundTime", support.getRefundTime());
                        businessInfo.put("refundReason", support.getRefundReason());
                    }

                    result.put("businessInfo", businessInfo);
                    result.put("businessTypeText", "众筹支持");
                }
            }
            // 5. 处理其他业务类型
            else {
                result.put("businessTypeText", "其他业务");
                log.info("[支付回调] 其他业务类型: 订单号={}, 业务类型={}", orderNo, businessType);
            }

            // 6. 判断支付状态
            boolean isPaid = payOrder.getStatus() == 1; // 1表示已支付
            result.put("isPaid", isPaid);
            result.put("paymentStatus", isPaid ? "支付成功" : "支付失败");

            log.info("[支付回调] 查询支付状态成功: 订单号={}, 支付状态={}", orderNo, isPaid ? "成功" : "失败");
            return R.ok(result);

        } catch (Exception e) {
            log.error("[支付回调] 查询支付状态异常: 订单号={}", orderNo, e);
            return R.fail("查询支付状态失败");
        }
    }

    private String buildFullReceiverAddress(String area, String address) {
        if (StringUtils.isBlank(area)) {
            return address;
        }
        if (StringUtils.isBlank(address)) {
            return area;
        }
        return area + address;
    }

    /**
     * 获取预售订单状态文本
     */
    private String getPresaleOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }

        PresaleOrderStatus orderStatus = PresaleOrderStatus.fromCode(status);
        return orderStatus != null ? orderStatus.getDesc() : "未知状态";
    }

    /**
     * 获取众筹支持状态文本
     */
    private String getCrowdfundingSupportStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }

        switch (status) {
            case 0: return "正常";
            case 1: return "已取消";
            case 2: return "已退款";
            default: return "未知状态";
        }
    }

    /**
     * 获取抽奖状态文本
     */
    private String getDrawStatusText(Integer drawStatus) {
        if (drawStatus == null) {
            return "未知";
        }

        switch (drawStatus) {
            case 0: return "未参与";
            case 1: return "已参与";
            case 2: return "中奖";
            case 3: return "未中奖";
            default: return "未知状态";
        }
    }
}
