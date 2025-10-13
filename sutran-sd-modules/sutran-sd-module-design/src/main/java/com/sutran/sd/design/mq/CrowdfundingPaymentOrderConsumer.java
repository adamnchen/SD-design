package com.sutran.sd.design.mq;

import com.rabbitmq.client.Channel;
import com.sutran.sd.design.service.CrowdfundingMqService;
import com.sutran.sd.pay.service.AliPayService;
import com.sutran.sd.pay.config.AliPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.sutran.sd.draw.mq.MqConstant.CROWDFUNDING_PAYMENT_ORDER_QUEUE;

/**
 * 众筹支付订单MQ消费者
 *
 * @author sutran
 * @date 2025-10-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrowdfundingPaymentOrderConsumer {

    private final AliPayService aliPayService;
    private final AliPayConfig aliPayConfig;
    private final CrowdfundingMqService crowdfundingMqService;

    /**
     * 消费众筹支付订单创建消息
     *
     * @param message MQ消息
     * @param channel RabbitMQ通道
     */
    @RabbitListener(queues = CROWDFUNDING_PAYMENT_ORDER_QUEUE)
    public void handlePaymentOrderCreate(CrowdfundingPaymentOrderMessage message, Channel channel, Message mqMessage) {
        String orderNo = message.getOrderNo();
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("开始处理众筹支付订单创建消息: 订单号={}, 项目ID={}, 用户ID={}", 
                    orderNo, message.getProjectId(), message.getUserId());

            // 1. 调用支付模块创建支付订单
            String subject = "众筹支持-" + message.getProjectId();
            String body = "用户" + message.getUserName() + "支持众筹项目";
            String notifyUrl = aliPayConfig.getDomain() + "/design/crowdfunding/payment/alipay/notify"; // 众筹模块回调地址
            
            // 创建支付订单并获取二维码
            aliPayService.createPayOrder(
                    message.getUserId(),
                    message.getUserName(),
                    orderNo,
                    subject,
                    body,
                    message.getSupportAmount(),
                    notifyUrl
            );
            
            log.info("支付订单创建成功: 订单号={}", orderNo);

            // 2. 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("众筹支付订单消息处理完成: 订单号={}", orderNo);

        } catch (Exception e) {
            log.error("处理众筹支付订单消息失败: 订单号={}, 重试次数={}", orderNo, message.getRetryCount(), e);

            try {
                // 检查是否需要重试
                if (message.getRetryCount() < message.getMaxRetryCount()) {
                    // 发送重试消息
                    crowdfundingMqService.sendPaymentOrderRetryMessage(message);
                    log.info("众筹支付订单消息重试投递: 订单号={}, 重试次数={}", orderNo, message.getRetryCount() + 1);
                    
                    // 确认消息（避免重复处理）
                    channel.basicAck(deliveryTag, false);
                } else {
                    // 超过最大重试次数，记录错误日志并确认消息
                    log.error("众筹支付订单消息处理失败，超过最大重试次数: 订单号={}, 最大重试次数={}", 
                            orderNo, message.getMaxRetryCount());
                    channel.basicAck(deliveryTag, false);
                }
            } catch (IOException ioException) {
                log.error("确认MQ消息失败: 订单号={}", orderNo, ioException);
            }
        }
    }
}
