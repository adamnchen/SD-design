package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.enums.CrowdfundingSupportStatus;
import com.sutran.sd.design.mapper.SdCrowdfundingSupportMapper;
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

            // 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(outTradeNo);
            if (payOrder == null) {
                log.error("[众筹][支付回调] 支付订单不存在: 订单号={}", outTradeNo);
                throw new ServiceException("支付订单不存在");
            }

            // 检查订单状态，已处理过直接返回成功
            if (payOrder.getStatus() != 0) {
                log.info("[众筹][支付回调] 订单已处理: 订单号={}, 状态={}", outTradeNo, payOrder.getStatus());
                return;
            }

            // 支付成功
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                // 更新支付订单状态
                payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);

                // 更新众筹支持记录状态
                support.setStatus(CrowdfundingSupportStatus.NORMAL.getCode()); // 正常状态
                crowdfundingSupportMapper.updateById(support);

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
}
