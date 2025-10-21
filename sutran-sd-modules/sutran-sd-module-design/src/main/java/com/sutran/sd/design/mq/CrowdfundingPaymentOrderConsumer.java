package com.sutran.sd.design.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.sutran.sd.framework.mq.MqConstant.CROWDFUNDING_PAYMENT_ORDER_QUEUE;

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
     * @param mqMessage MQ消息
     * @param channel RabbitMQ通道
     */
    @RabbitListener(queues = CROWDFUNDING_PAYMENT_ORDER_QUEUE)
    public void handlePaymentOrderCreate(Message mqMessage, Channel channel) {
        String orderNo = null;
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();

        try {
            // 手动反序列化消息
            String messageBody = new String(mqMessage.getBody(), "UTF-8");
            log.info("接收到众筹支付订单消息: {}", messageBody);

            // 使用Jackson反序列化
            ObjectMapper objectMapper = new ObjectMapper();
            CrowdfundingPaymentOrderMessage message = objectMapper.readValue(messageBody, CrowdfundingPaymentOrderMessage.class);

            orderNo = message.getOrderNo();
            log.info("开始处理众筹支付订单创建消息: 订单号={}, 项目ID={}, 用户ID={}",
                    orderNo, message.getProjectId(), message.getUserId());

            // 1. 调用支付模块创建支付订单
            String subject = "众筹支持-" + message.getProjectId();
            String body = "用户" + message.getUserName() + "支持众筹项目";
            String notifyUrl = "/design/crowdfunding/payment/alipay/notify"; // 众筹模块回调地址

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
            log.error("处理众筹支付订单消息失败: 订单号={}", orderNo, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("消息拒绝失败: 订单号={}", orderNo, ioException);
            }
        }
    }
}
