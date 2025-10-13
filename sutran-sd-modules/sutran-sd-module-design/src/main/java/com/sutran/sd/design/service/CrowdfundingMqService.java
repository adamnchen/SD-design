package com.sutran.sd.design.service;

import com.sutran.sd.design.domain.SdCrowdfundingSupport;
import com.sutran.sd.design.mq.CrowdfundingPaymentOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.sutran.sd.draw.mq.MqConstant.*;

/**
 * 众筹MQ服务
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrowdfundingMqService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送众筹支付订单创建消息
     *
     * @param orderNo 订单号
     * @param support 支持记录
     */
    public void sendPaymentOrderMessage(String orderNo, SdCrowdfundingSupport support) {
        try {
            // 构建MQ消息
            CrowdfundingPaymentOrderMessage message = CrowdfundingPaymentOrderMessage.builder()
                    .orderNo(orderNo)
                    .projectId(support.getProjectId())
                    .userId(support.getUserId())
                    .userName(support.getUserName())
                    .supportAmount(support.getSupportAmount())
                    .createTime(new Date())
                    .messageType("PAYMENT_ORDER_CREATE")
                    .retryCount(0)
                    .maxRetryCount(3)
                    .build();

            // 投递到MQ
            rabbitTemplate.convertAndSend(
                    CROWDFUNDING_PAYMENT_ORDER_EXCHANGE,
                    CROWDFUNDING_PAYMENT_ORDER_ROUTING_KEY,
                    message
            );

            log.info("众筹支付订单MQ消息投递成功: 订单号={}, 项目ID={}, 用户ID={}", 
                    orderNo, support.getProjectId(), support.getUserId());

        } catch (Exception e) {
            log.error("众筹支付订单MQ消息投递失败: 订单号={}, 项目ID={}, 用户ID={}", 
                    orderNo, support.getProjectId(), support.getUserId(), e);
            throw new RuntimeException("MQ消息投递失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送众筹支付订单重试消息
     *
     * @param message 原始消息
     */
    public void sendPaymentOrderRetryMessage(CrowdfundingPaymentOrderMessage message) {
        try {
            // 增加重试次数
            message.setRetryCount(message.getRetryCount() + 1);
            message.setCreateTime(new Date());

            // 投递到MQ
            rabbitTemplate.convertAndSend(
                    CROWDFUNDING_PAYMENT_ORDER_EXCHANGE,
                    CROWDFUNDING_PAYMENT_ORDER_ROUTING_KEY,
                    message
            );

            log.info("众筹支付订单MQ重试消息投递成功: 订单号={}, 重试次数={}", 
                    message.getOrderNo(), message.getRetryCount());

        } catch (Exception e) {
            log.error("众筹支付订单MQ重试消息投递失败: 订单号={}, 重试次数={}", 
                    message.getOrderNo(), message.getRetryCount(), e);
            throw new RuntimeException("MQ重试消息投递失败: " + e.getMessage(), e);
        }
    }
}
