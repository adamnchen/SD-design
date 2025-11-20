package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.design.service.impl.ProofCrowdfundPayNotifyServiceImpl;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.PayOrderService;
import com.sutran.sd.design.enums.CrowdfundingProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 众筹调试控制器 - 用于排查支付回调问题
 */
@Slf4j
@RestController
@RequestMapping("/design/crowdfunding/debug")
@RequiredArgsConstructor
public class CrowdfundingDebugController {

    private final PayOrderService payOrderService;
    private final SdCrowdfundingSupportMapper supportMapper;
    private final SdCrowdfundingProjectMapper projectMapper;
    private final AliPayService aliPayService;
    private final ProofCrowdfundPayNotifyServiceImpl payNotifyService;

    /**
     * 检查指定订单号的支付状态
     */
    @GetMapping("/order-status/{orderNo}")
    public R<Map<String, Object>> checkOrderStatus(@PathVariable String orderNo) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(orderNo);
            if (payOrder != null) {
                result.put("payOrder", Map.of(
                    "id", payOrder.getId(),
                    "outTradeNo", payOrder.getOutTradeNo(),
                    "tradeNo", payOrder.getTradeNo(),
                    "status", payOrder.getStatus(),
                    "totalAmount", payOrder.getTotalAmount(),
                    "businessType", payOrder.getBusinessType(),
                    "gmtPayment", payOrder.getGmtPayment(),
                    "createTime", payOrder.getCreateTime()
                ));
            } else {
                result.put("payOrder", "不存在");
            }

            // 2. 查询支持记录
            SdCrowdfundingSupport support = supportMapper.selectByOrderNo(orderNo);
            if (support != null) {
                result.put("support", Map.of(
                    "id", support.getId(),
                    "projectId", support.getProjectId(),
                    "userId", support.getUserId(),
                    "userName", support.getUserName(),
                    "status", support.getStatus(),
                    "supportAmount", support.getSupportAmount(),
                    "drawStatus", support.getDrawStatus(),
                    "isWinner", support.getIsWinner(),
                    "createTime", support.getCreateTime(),
                    "receiverName", support.getReceiverName(),
                    "receiverPhone", support.getReceiverPhone(),
                    "receiverAddress", support.getReceiverAddress()
                ));

                // 3. 查询项目信息
                SdCrowdfundingProject project = projectMapper.selectById(support.getProjectId());
                if (project != null) {
                    result.put("project", Map.of(
                        "id", project.getId(),
                        "title", project.getTitle(),
                        "status", project.getStatus(),
                        "currentAmount", project.getCurrentAmount(),
                        "targetAmount", project.getTargetAmount(),
                        "supportCount", project.getSupportCount(),
                        "drawStatus", project.getDrawStatus(),
                        "drawTime", project.getDrawTime()
                    ));
                }
            } else {
                result.put("support", "不存在");
            }

            return R.ok(result);
        } catch (Exception e) {
            log.error("检查订单状态失败: orderNo={}", orderNo, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    /**
     * 手动同步支付宝订单状态
     */
    @PostMapping("/sync-order/{orderNo}")
    public R<String> syncOrderStatus(@PathVariable String orderNo) {
        try {
            log.info("[调试] 手动同步订单状态: orderNo={}", orderNo);
            aliPayService.syncOrderStatus(orderNo, null);
            return R.ok("同步成功");
        } catch (Exception e) {
            log.error("同步订单状态失败: orderNo={}", orderNo, e);
            return R.fail("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发支付成功处理
     */
    @PostMapping("/trigger-success/{orderNo}")
    public R<String> triggerSuccess(@PathVariable String orderNo) {
        try {
            log.info("[调试] 手动触发支付成功处理: orderNo={}", orderNo);
            
            PayOrder payOrder = payOrderService.detailByOutTradeNo(orderNo);
            if (payOrder == null) {
                return R.fail("支付订单不存在");
            }
            
            if (payOrder.getStatus() != 1) {
                return R.fail("支付订单状态不是成功状态，当前状态: " + payOrder.getStatus());
            }
            
            // 手动调用支付成功处理
            payNotifyService.handleSuccessBusiness(
                "TRADE_SUCCESS", 
                orderNo, 
                payOrder.getTradeNo(), 
                payOrder.getTotalAmount().toString(), 
                payOrder.getGmtPayment(), 
                payOrder.getBusinessId(), 
                payOrder.getUserId()
            );
            
            return R.ok("触发成功");
        } catch (Exception e) {
            log.error("手动触发支付成功处理失败: orderNo={}", orderNo, e);
            return R.fail("触发失败: " + e.getMessage());
        }
    }

    /**
     * 检查项目的所有支持记录
     */
    @GetMapping("/project-supports/{projectId}")
    public R<Map<String, Object>> checkProjectSupports(@PathVariable Long projectId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 查询项目信息
            SdCrowdfundingProject project = projectMapper.selectById(projectId);
            if (project != null) {
                result.put("project", Map.of(
                    "id", project.getId(),
                    "title", project.getTitle(),
                    "status", project.getStatus(),
                    "currentAmount", project.getCurrentAmount(),
                    "targetAmount", project.getTargetAmount(),
                    "supportCount", project.getSupportCount(),
                    "drawStatus", project.getDrawStatus(),
                    "drawTime", project.getDrawTime()
                ));
            }

            // 查询所有支持记录
            List<SdCrowdfundingSupport> supports = supportMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SdCrowdfundingSupport>()
                    .eq(SdCrowdfundingSupport::getProjectId, projectId)
                    .orderByDesc(SdCrowdfundingSupport::getCreateTime)
            );
            
            result.put("supports", supports.stream().map(support -> Map.of(
                "id", support.getId(),
                "orderNo", support.getOrderNo(),
                "userId", support.getUserId(),
                "userName", support.getUserName(),
                "status", support.getStatus(),
                "supportAmount", support.getSupportAmount(),
                "drawStatus", support.getDrawStatus(),
                "isWinner", support.getIsWinner(),
                "createTime", support.getCreateTime()
            )).toList());

            return R.ok(result);
        } catch (Exception e) {
            log.error("检查项目支持记录失败: projectId={}", projectId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发项目抽奖
     */
    @PostMapping("/trigger-draw/{projectId}")
    public R<String> triggerDraw(@PathVariable Long projectId) {
        try {
            log.info("[调试] 手动触发项目抽奖: projectId={}", projectId);
            
            SdCrowdfundingProject project = projectMapper.selectById(projectId);
            if (project == null) {
                return R.fail("项目不存在");
            }
            
            if (!CrowdfundingProjectStatus.SUCCESS.getCode().equals(project.getStatus())) {
                return R.fail("项目状态不是成功状态，当前状态: " + project.getStatus());
            }
            
            // 调用抽奖服务
            // 这里需要注入 ISdCrowdfundingProjectService
            return R.ok("抽奖功能需要注入服务来实现");
        } catch (Exception e) {
            log.error("手动触发项目抽奖失败: projectId={}", projectId, e);
            return R.fail("抽奖失败: " + e.getMessage());
        }
    }
}
