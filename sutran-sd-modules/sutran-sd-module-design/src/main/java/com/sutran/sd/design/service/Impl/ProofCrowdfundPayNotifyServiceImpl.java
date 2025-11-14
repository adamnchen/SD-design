package com.sutran.sd.design.service.impl;

import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.domain.SdCrowdfundingProject;
import com.sutran.sd.design.enums.CrowdfundingSupportStatus;
import com.sutran.sd.design.enums.CrowdfundingProjectStatus;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
import com.sutran.sd.design.mapper.SdCrowdfundingProjectMapper;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 众筹支付回调服务
 * @author sutran
 * @date 2025-11-14
 */
@Slf4j
@Service(PayNotifyServer.PROOF_CROWDFUND_NOTIFY)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class ProofCrowdfundPayNotifyServiceImpl extends BasePayNotifyService {

    private final PayOrderService payOrderService;
    private final SdCrowdfundingSupportMapper crowdfundingSupportMapper;
    private final SdCrowdfundingProjectMapper crowdfundingProjectMapper;
    private final AliPayService aliPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSuccessBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Long businessId, Long userId) {
        try {
            log.info("[众筹][支付回调] 开始处理: 订单号={}, 交易状态={}, 金额={}", outTradeNo, tradeStatus, totalAmount);

            // 查询众筹支持记录
            SdCrowdfundingSupport support = crowdfundingSupportMapper.selectByOrderNo(outTradeNo);
            if (support == null) {
                log.error("[众筹][支付回调] 支持记录不存在: 订单号={}", outTradeNo);
                throw new ServiceException("众筹支持记录不存在");
            }

            // 支付成功
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                // 更新众筹支持记录状态
                support.setStatus(CrowdfundingSupportStatus.NORMAL.getCode()); // 正常状态
                crowdfundingSupportMapper.updateById(support);

                // 更新众筹项目金额和支持人数
                updateProjectAmountAndSupportCount(support.getProjectId(), new BigDecimal(totalAmount));

                log.info("[众筹][支付回调] 支付成功处理完成: 订单号={}, 项目ID={}", outTradeNo, support.getProjectId());
            } else {
                // 支付失败
                log.warn("[众筹][支付回调] 支付失败: 订单号={}, 交易状态={}", outTradeNo, tradeStatus);
                payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
            }

        } catch (Exception e) {
            log.error("[众筹][支付回调] 处理异常: 订单号={}", outTradeNo, e);
            throw e;
        }
    }

    @Override
    public void handleFailedBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
        SdCrowdfundingSupport support = crowdfundingSupportMapper.selectByOrderNo(outTradeNo);
        if (support == null) {
            log.error("[众筹][支付回调] 支持记录不存在: 订单号={}", outTradeNo);
            payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
            return;
        }

        payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
        // 更新众筹支持记录状态
        support.setStatus(CrowdfundingSupportStatus.CANCELLED.getCode()); // 已取消
        crowdfundingSupportMapper.updateById(support);

        log.error("[众筹][支付回调] 支付失败: 订单号={}, 交易状态={}", outTradeNo, tradeStatus);
    }

    @Override
    public void dealPayTimeoutData(PayTimeoutStatusVo vo) {
        try {
            log.info("[众筹][支付超时] 开始处理: 订单号={}", vo.getOutTradeNo());

            // 支付成功或完成，不需要处理
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(vo.getTradeStatus()) ||
                AliPayTradeStatus.TRADE_FINISHED.name().equals(vo.getTradeStatus())) {
                return;
            }

            // 查询众筹支持记录
            SdCrowdfundingSupport support = crowdfundingSupportMapper.selectByOrderNo(vo.getOutTradeNo());
            if (support != null) {
                // 更新支持记录状态为已取消
                support.setStatus(CrowdfundingSupportStatus.CANCELLED.getCode()); // 已取消
                crowdfundingSupportMapper.updateById(support);
                log.info("[众筹][支付超时] 支持记录已取消: 订单号={}", vo.getOutTradeNo());
            }

        } catch (Exception e) {
            log.error("[众筹][支付超时] 处理异常: 订单号={}", vo.getOutTradeNo(), e);
        }
    }

    /**
     * 处理众筹退款
     * @param orderNo 订单号
     * @param refundReason 退款原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void processCrowdfundingRefund(String orderNo, String refundReason) {
        try {
            log.info("[众筹退款] 开始处理退款: 订单号={}, 退款原因={}", orderNo, refundReason);

            // 1. 查询众筹支持记录
            SdCrowdfundingSupport support = crowdfundingSupportMapper.selectByOrderNo(orderNo);
            if (support == null) {
                log.error("[众筹退款] 支持记录不存在: 订单号={}", orderNo);
                throw new ServiceException("众筹支持记录不存在");
            }

            // 2. 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(orderNo);
            if (payOrder == null) {
                log.error("[众筹退款] 支付订单不存在: 订单号={}", orderNo);
                throw new ServiceException("支付订单不存在");
            }

            // 3. 检查是否已经退款
            if (support.getStatus().equals(CrowdfundingSupportStatus.REFUNDED.getCode())) {
                log.info("[众筹退款] 订单已退款，跳过处理: 订单号={}", orderNo);
                return;
            }

            // 4. 调用支付宝退款API
            if (payOrder.getTradeNo() == null) {
                log.error("[众筹退款] 支付订单缺少支付宝交易号: 订单号={}", orderNo);
                throw new ServiceException("支付订单缺少支付宝交易号");
            }

            BigDecimal refundAmount = support.getSupportAmount();
            aliPayService.tradeRefund(orderNo, payOrder.getTradeNo(),
                refundAmount.setScale(2, RoundingMode.HALF_UP).toString(), refundReason);

            // 5. 更新众筹支持记录状态
            support.setStatus(CrowdfundingSupportStatus.REFUNDED.getCode()); // 已退款
            support.setRefundTime(new Date());
            support.setRefundReason(refundReason);
            crowdfundingSupportMapper.updateById(support);

            log.info("[众筹退款] 退款成功: 订单号={}, 退款金额={}, 用户={}",
                orderNo, refundAmount, support.getUserName());

        } catch (Exception e) {
            log.error("[众筹退款] 退款失败: 订单号={}, 异常：", orderNo, e);
            throw new ServiceException("众筹退款失败: " + e.getMessage());
        }
    }

    /**
     * 更新众筹项目金额和支持人数
     * @param projectId 项目ID
     * @param amount 支持金额
     */
    private void updateProjectAmountAndSupportCount(Long projectId, BigDecimal amount) {
        try {
            log.info("[众筹] 开始更新项目金额和支持人数: 项目ID={}, 金额={}", projectId, amount);

            // 查询众筹项目
            SdCrowdfundingProject project = crowdfundingProjectMapper.selectById(projectId);
            if (project == null) {
                log.error("[众筹] 项目不存在: 项目ID={}", projectId);
                return;
            }

            // 获取当前金额和支持人数
            BigDecimal currentAmount = project.getCurrentAmount() != null ? project.getCurrentAmount() : BigDecimal.ZERO;
            Integer currentSupportCount = project.getSupportCount() != null ? project.getSupportCount() : 0;

            log.info("[众筹] 项目当前信息: 项目ID={}, 当前金额={}, 当前支持人数={}", projectId, currentAmount, currentSupportCount);

            // 计算新的金额和支持人数
            BigDecimal newAmount = currentAmount.add(amount);
            Integer newSupportCount = currentSupportCount + 1;

            // 更新项目信息
            project.setCurrentAmount(newAmount);
            project.setSupportCount(newSupportCount);

            // 检查是否达到目标金额
            if (newAmount.compareTo(project.getTargetAmount()) >= 0) {
                // 众筹成功，自动开始抽奖
                project.setStatus(CrowdfundingProjectStatus.SUCCESS.getCode());
                project.setDrawStatus(1);
                project.setDrawTime(new Date()); // 记录抽奖开始时间
                log.info("[众筹] 众筹成功，自动开始抽奖: 项目ID={}, 项目名称={}", project.getId(), project.getTitle());
            }

            // 更新数据库
            int updateResult = crowdfundingProjectMapper.updateSdCrowdfundingProject(project);
            log.info("[众筹] 数据库更新结果: 项目ID={}, 更新行数={}, 新金额={}, 新支持人数={}",
                projectId, updateResult, newAmount, newSupportCount);

            // 验证更新结果
            SdCrowdfundingProject updatedProject = crowdfundingProjectMapper.selectById(projectId);
            if (updatedProject != null) {
                log.info("[众筹] 更新后验证: 项目ID={}, 数据库中的金额={}, 支持人数={}",
                    projectId, updatedProject.getCurrentAmount(), updatedProject.getSupportCount());
            }

            log.info("[众筹] 更新项目金额和支持人数完成: 项目ID={}, 新增金额={}, 累计金额={}, 累计支持人数={}",
                projectId, amount, newAmount, newSupportCount);

        } catch (Exception e) {
            log.error("[众筹] 更新项目金额和支持人数失败: 项目ID={}, 金额={}", projectId, amount, e);
            throw e;
        }
    }
}
