package com.sutran.sd.design.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.design.domain.SdPresaleOrder;
import com.sutran.sd.design.domain.SdPresaleProject;
import com.sutran.sd.design.mapper.SdPresaleOrderMapper;
import com.sutran.sd.design.mapper.SdPresaleProjectMapper;
import com.sutran.sd.pay.constants.PayNotifyServer;
import com.sutran.sd.pay.domain.PayOrder;
import com.sutran.sd.pay.domain.vo.PayTimeoutStatusVo;
import com.sutran.sd.pay.enums.AliPayTradeStatus;
import com.sutran.sd.pay.service.BasePayNotifyService;
import com.sutran.sd.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 预售订单支付回调服务
 * @author sutran
 * @date 2025-10-19
 */
@Slf4j
@Service(PayNotifyServer.PRESALE_ORDER_NOTIFY)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class PresaleOrderPayNotifyServiceImpl extends BasePayNotifyService {

    private final PayOrderService payOrderService;
    private final SdPresaleOrderMapper presaleOrderMapper;
    private final SdPresaleProjectMapper presaleProjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSuccessBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment, Long businessId, Long userId) {
        try {
            log.info("[预售订单][支付回调] 开始处理: 订单号={}, 交易状态={}, 金额={}", outTradeNo, tradeStatus, totalAmount);

            // 查询预售订单
            SdPresaleOrder presaleOrder = presaleOrderMapper.selectByOrderNo(outTradeNo);
            if (presaleOrder == null) {
                log.error("[预售订单][支付回调] 订单不存在: 订单号={}", outTradeNo);

            }

            // 查询支付订单
            PayOrder payOrder = payOrderService.detailByOutTradeNo(outTradeNo);
            if (payOrder == null) {
                log.error("[预售订单][支付回调] 支付订单不存在: 订单号={}", outTradeNo);

            }

            // 检查订单状态，已处理过直接返回成功
            if (payOrder.getStatus() != 0) {
                log.info("[预售订单][支付回调] 订单已处理: 订单号={}, 状态={}", outTradeNo, payOrder.getStatus());

            }

            BigDecimal amount = new BigDecimal(totalAmount);

            // 支付成功
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(tradeStatus) || AliPayTradeStatus.TRADE_FINISHED.name().equals(tradeStatus)) {
                // 更新支付订单状态
                payOrderService.successPay(outTradeNo, tradeNo, totalAmount, gmtPayment);

                // 更新预售订单状态
                presaleOrder.setOrderStatus(2); // 已支付
                presaleOrder.setPayOrderId(payOrder.getId());
                presaleOrderMapper.updateById(presaleOrder);

                // 更新项目销售金额和销售数量
                updateProjectSalesInfo(presaleOrder.getProjectId(), amount, presaleOrder.getQuantity());

                // 检查是否需要阶梯价格调整
                checkAndAdjustTieredPricing(presaleOrder);

                log.info("[预售订单][支付回调] 支付成功处理完成: 订单号={}, 项目ID={}", outTradeNo, presaleOrder.getProjectId());
            }


        } catch (Exception e) {
            log.error("[预售订单][支付回调] 处理异常: 订单号={}", outTradeNo, e);
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        }
    }

    public void handleFailedBusiness(String tradeStatus, String outTradeNo, String tradeNo, String totalAmount, String gmtPayment) {
        SdPresaleOrder presaleOrder = presaleOrderMapper.selectByOrderNo(outTradeNo);
        if (presaleOrder == null) {
            log.error("[预售订单][支付回调] 订单不存在: 订单号={}", outTradeNo);
        }

        payOrderService.failPay(outTradeNo, tradeNo, totalAmount);
        // 更新预售订单状态
        presaleOrder.setOrderStatus(6); // 已取消
        presaleOrderMapper.updateById(presaleOrder);

        log.error("[预售订单][支付回调] 支付失败: 订单号={}, 交易状态={}", outTradeNo, tradeStatus);

    }

    @Override
    public void dealPayTimeoutData(PayTimeoutStatusVo vo) {
        try {
            log.info("[预售订单][支付超时] 开始处理: 订单号={}", vo.getOutTradeNo());

            // 支付成功或完成，不需要处理
            if (AliPayTradeStatus.TRADE_SUCCESS.name().equals(vo.getTradeStatus()) ||
                AliPayTradeStatus.TRADE_FINISHED.name().equals(vo.getTradeStatus())) {
                return;
            }

            // 查询预售订单
            SdPresaleOrder presaleOrder = presaleOrderMapper.selectByOrderNo(vo.getOutTradeNo());
            if (presaleOrder != null) {
                // 更新订单状态为已取消
                presaleOrder.setOrderStatus(6); // 已取消
                presaleOrderMapper.updateById(presaleOrder);
                log.info("[预售订单][支付超时] 订单已取消: 订单号={}", vo.getOutTradeNo());
            }

        } catch (Exception e) {
            log.error("[预售订单][支付超时] 处理异常: 订单号={}", vo.getOutTradeNo(), e);
        }
    }

    /**
     * 更新项目销售信息（金额和数量）
     */
    private void updateProjectSalesInfo(Long projectId, BigDecimal amount, Integer quantity) {
        try {
            SdPresaleProject project = presaleProjectMapper.selectSdPresaleProjectById(projectId);
            if (project != null) {
                // 更新销售金额
                BigDecimal currentAmount = project.getTotalSalesAmount() != null ? project.getTotalSalesAmount() : BigDecimal.ZERO;
                project.setTotalSalesAmount(currentAmount.add(amount));              
                presaleProjectMapper.updateById(project);
                log.info("[预售订单] 更新项目销售信息: 项目ID={}, 新增金额={}, 新增数量={}, 累计金额={}",
                    projectId, amount, quantity, project.getTotalSalesAmount());
            }
        } catch (Exception e) {
            log.error("[预售订单] 更新项目销售信息失败: 项目ID={}, 金额={}, 数量={}", projectId, amount, quantity, e);
        }
    }

    /**
     * 检查并调整阶梯价格
     */
    private void checkAndAdjustTieredPricing(SdPresaleOrder presaleOrder) {
        try {
            log.info("[预售订单] 阶梯价格调整检查: 订单号={}, 项目ID={}",
                presaleOrder.getOrderNo(), presaleOrder.getProjectId());

            // 1. 查询项目信息
            SdPresaleProject project = presaleProjectMapper.selectSdPresaleProjectById(presaleOrder.getProjectId());
            if (project == null || project.getTieredPricing() == null) {
                log.info("[预售订单] 项目不存在或无阶梯价格配置: 项目ID={}", presaleOrder.getProjectId());
                return;
            }

            // 2. 解析阶梯价格配置
            List<TieredPricingItem> tieredPricingList = parseTieredPricing(project.getTieredPricing());
            if (tieredPricingList.isEmpty()) {
                log.info("[预售订单] 阶梯价格配置为空: 项目ID={}", presaleOrder.getProjectId());
                return;
            }

            // 3. 查询项目当前总订单数量（已支付的订单）
            LambdaQueryWrapper<SdPresaleOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SdPresaleOrder::getProjectId, presaleOrder.getProjectId())
                       .eq(SdPresaleOrder::getOrderStatus, 2); // 已支付状态

            List<SdPresaleOrder> paidOrders = presaleOrderMapper.selectList(queryWrapper);
            int totalQuantity = paidOrders.stream()
                    .mapToInt(SdPresaleOrder::getQuantity)
                    .sum();

            log.info("[预售订单] 项目当前总订单数量: 项目ID={}, 总数量={}", presaleOrder.getProjectId(), totalQuantity);

            // 4. 计算当前应该的单价
            BigDecimal currentUnitPrice = calculateCurrentUnitPrice(totalQuantity, tieredPricingList);
            log.info("[预售订单] 当前应该的单价: 项目ID={}, 单价={}", presaleOrder.getProjectId(), currentUnitPrice);

            // 5. 检查每个订单是否需要调整价格
            for (SdPresaleOrder order : paidOrders) {
                adjustOrderPricing(order, currentUnitPrice, tieredPricingList);
            }

        } catch (Exception e) {
            log.error("[预售订单] 阶梯价格调整失败: 订单号={}", presaleOrder.getOrderNo(), e);
        }
    }

    /**
     * 解析阶梯价格配置
     */
    private List<TieredPricingItem> parseTieredPricing(String tieredPricingJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(tieredPricingJson,
                mapper.getTypeFactory().constructCollectionType(List.class, TieredPricingItem.class));
        } catch (Exception e) {
            log.error("[预售订单] 解析阶梯价格配置失败: {}", tieredPricingJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算当前应该的单价
     */
    private BigDecimal calculateCurrentUnitPrice(int totalQuantity, List<TieredPricingItem> tieredPricingList) {
        // 按数量节点排序
        tieredPricingList.sort((a, b) -> Integer.compare(a.getNode(), b.getNode()));

        // 找到对应的价格区间
        for (int i = tieredPricingList.size() - 1; i >= 0; i--) {
            TieredPricingItem tier = tieredPricingList.get(i);
            if (totalQuantity >= tier.getNode()) {
                return tier.getUnitPrice();
            }
        }

        // 如果数量小于第一个节点，返回第一个价格
        return tieredPricingList.get(0).getUnitPrice();
    }

    /**
     * 调整订单价格
     */
    private void adjustOrderPricing(SdPresaleOrder order, BigDecimal currentUnitPrice, List<TieredPricingItem> tieredPricingList) {
        try {
            // 如果订单的最终单价已经是最新的，跳过
            if (order.getFinalUnitPrice() != null && order.getFinalUnitPrice().compareTo(currentUnitPrice) == 0) {
                return;
            }

            // 计算新的总金额
            BigDecimal newTotalAmount = currentUnitPrice.multiply(BigDecimal.valueOf(order.getQuantity()));

            // 计算退款金额（原总金额 - 新总金额）
            BigDecimal refundAmount = order.getOriginalTotalAmount().subtract(newTotalAmount);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 需要退款
                log.info("[预售订单] 需要退款: 订单号={}, 退款金额={}, 原金额={}, 新金额={}",
                    order.getOrderNo(), refundAmount, order.getOriginalTotalAmount(), newTotalAmount);

                // 更新订单信息
                order.setFinalUnitPrice(currentUnitPrice);
                order.setFinalTotalAmount(newTotalAmount);
                order.setRefundAmount(refundAmount);


                presaleOrderMapper.updateById(order);

                // TODO: 发起支付宝退款流程
                // 这里需要调用支付宝退款接口，暂时记录日志
                log.info("[预售订单] 发起退款流程: 订单号={}, 退款金额={}", order.getOrderNo(), refundAmount);
                // processAlipayRefund(order, refundAmount);

            } else if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                // 理论上不应该出现这种情况，因为阶梯价格只会降低
                log.warn("[预售订单] 价格异常: 订单号={}, 退款金额为负数={}", order.getOrderNo(), refundAmount);
            } else {
                // 价格没有变化
                log.info("[预售订单] 价格无变化: 订单号={}", order.getOrderNo());
            }

        } catch (Exception e) {
            log.error("[预售订单] 调整订单价格失败: 订单号={}", order.getOrderNo(), e);
        }
    }

    /**
     * 阶梯价格配置项
     */
    public static class TieredPricingItem {
        private BigDecimal unitPrice;
        private Integer node;

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getNode() {
            return node;
        }

        public void setNode(Integer node) {
            this.node = node;
        }
    }

    /**
     * 处理支付宝退款（TODO: 待实现）
     */
    private void processAlipayRefund(SdPresaleOrder order, BigDecimal refundAmount) {
        // TODO: 实现支付宝退款接口
        // 1. 调用支付宝退款API
        // 2. 更新支付订单的退款状态
        // 3. 更新预售订单的退款状态
        log.info("[预售订单] 支付宝退款接口待实现: 订单号={}, 退款金额={}", order.getOrderNo(), refundAmount);
    }
}
