package com.sutran.sd.framework.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

import static com.sutran.sd.framework.mq.MqConstant.*;

/**
 * @author zj
 * @date 2024-03-06
 */
@Configuration
@Slf4j
public class RabbitConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 定制化amqp模版      可根据需要定制多个
     * <p>
     * <p>
     * 此处为模版类定义 Jackson消息转换器
     * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调   即消息发送到exchange  ack
     * ReturnCallback接口用于实现消息发送到RabbitMQ 交换器，但无相应队列与交换器绑定时的回调  即消息发送不到任何一个队列中  ack
     *
     * @return the amqp template
     */
    @Bean
    public AmqpTemplate amqpTemplate() {
        // 使用jackson 消息转换器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setEncoding("UTF-8");
        // 消息发送失败返回到队列中，yml需要配置 publisher-returns: true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned -> {
            Message message = returned.getMessage();
            String correlationId = message.getMessageProperties().getCorrelationId();
            log.warn("[MQ投递回调确认]>>>>>>>>>MQ消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
        });
        // 消息确认，yml需要配置 publisher-confirms: true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("[MQ投递回调确认]>>>>>>>>>MQ消息发送到exchange失败,原因: {}", cause);
            }
        });
        return rabbitTemplate;
    }



    /** ---------------------------------------------------------------------------- SD业务-文生图 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdTxtDrawExchange() {
        return ExchangeBuilder.directExchange(SD_TXT_TO_IMG_DRAW_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdTxtDrawQueue() {
        return QueueBuilder.durable(SD_TXT_TO_IMG_DRAW_QUEUE).build();
    }
    @Bean
    public Binding sdTxtDrawBinding() {
        return BindingBuilder.bind(sdTxtDrawQueue()).to(sdTxtDrawExchange()).with(SD_TXT_TO_IMG_DRAW_ROUTING_KEY).noargs();
    }

    /** ---------------------------------------------------------------------------- SD业务-图生图 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdImgDrawExchange() {
        return ExchangeBuilder.directExchange(SD_IMG_TO_IMG_DRAW_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdImgDrawQueue() {
        return QueueBuilder.durable(SD_IMG_TO_IMG_DRAW_QUEUE).build();
    }
    @Bean
    public Binding sdImgDrawBinding() {
        return BindingBuilder.bind(sdImgDrawQueue()).to(sdImgDrawExchange()).with(SD_IMG_TO_IMG_DRAW_ROUTING_KEY).noargs();
    }

    /** ---------------------------------------------------------------------------- SD业务-Comfy生图 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdComfyDrawExchange() {
        return ExchangeBuilder.directExchange(SD_COMFY_DRAW_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdComfyDrawQueue() {
        return QueueBuilder.durable(SD_COMFY_DRAW_QUEUE).build();
    }
    @Bean
    public Binding sdComfyDrawBinding() {
        return BindingBuilder.bind(sdComfyDrawQueue()).to(sdComfyDrawExchange()).with(SD_COMFY_DRAW_ROUTING_KEY).noargs();
    }

    /** ---------------------------------------------------------------------------- SD模型训练-图片预处理任务 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdPreImgExchange() {
        return ExchangeBuilder.directExchange(SD_PRE_IMG_TASK_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdPreImgQueue() {
        return QueueBuilder.durable(SD_PRE_IMG_TASK_QUEUE).build();
    }
    @Bean
    public Binding sdPreImgBinding() {
        return BindingBuilder.bind(sdPreImgQueue()).to(sdPreImgExchange()).with(SD_PRE_IMG_TASK_ROUTING_KEY).noargs();
    }

    /** ---------------------------------------------------------------------------- SD模型训练-训练任务 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdTrainExchange() {
        return ExchangeBuilder.directExchange(SD_TRAIN_TASK_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdTrainQueue() {
        return QueueBuilder.durable(SD_TRAIN_TASK_QUEUE).build();
    }
    @Bean
    public Binding sdTrainBinding() {
        return BindingBuilder.bind(sdTrainQueue()).to(sdTrainExchange()).with(SD_TRAIN_TASK_ROUTING_KEY).noargs();
    }


    /** ---------------------------------------------------------------------------- SD模型训练-FluxGym训练任务 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange sdFluxGymTrainExchange() {
        return ExchangeBuilder.directExchange(SD_FLUXGYM_TRAIN_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue sdFluxGymTrainQueue() {
        return QueueBuilder.durable(SD_FLUXGYM_TRAIN_QUEUE).build();
    }
    @Bean
    public Binding sdFluxGymTrainBinding() {
        return BindingBuilder.bind(sdFluxGymTrainQueue()).to(sdFluxGymTrainExchange()).with(SD_FLUXGYM_TRAIN_ROUTING_KEY).noargs();
    }


    /** ---------------------------------------------------------------------------- 微信消息通知 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange wxMsgExchange() {
        return ExchangeBuilder.directExchange(WX_MSG_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue wxMsgQueue() {
        return QueueBuilder.durable(WX_MSG_QUEUE).build();
    }
    @Bean
    public Binding wxMsgBinding() {
        return BindingBuilder.bind(wxMsgQueue()).to(wxMsgExchange()).with(WX_MSG_ROUTING_KEY).noargs();
    }


    /** ---------------------------------------------------------------------------- 第三方绘图数据推送 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange imgSendThirdExchange() {
        return ExchangeBuilder.directExchange(IMG_SEND_THIRD_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue imgSendThirdQueue() {
        return QueueBuilder.durable(IMG_SEND_THIRD_QUEUE).build();
    }
    @Bean
    public Binding imgSendThirdBinding() {
        return BindingBuilder.bind(wxMsgQueue()).to(wxMsgExchange()).with(IMG_SEND_THIRD_ROUTING_KEY).noargs();
    }


    /** ---------------------------------------------------------------------------- 支付订单超时 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange payOrderTimeoutExchange() {
        return ExchangeBuilder.directExchange(PAY_ORDER_TIMEOUT_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue payOrderTimeoutQueue() {
        return QueueBuilder.durable(PAY_ORDER_TIMEOUT_QUEUE).build();
    }
    @Bean
    public Binding payOrderTimeoutBinding() {
        return BindingBuilder.bind(wxMsgQueue()).to(wxMsgExchange()).with(PAY_ORDER_TIMEOUT_ROUTING_KEY).noargs();
    }


    /** ---------------------------------------------------------------------------- 众筹支付订单 Direct exchange --------------------------------------------------------------------------- */
    @Bean
    public Exchange crowdfundingPaymentOrderExchange() {
        return ExchangeBuilder.directExchange(CROWDFUNDING_PAYMENT_ORDER_EXCHANGE).durable(true).build();
    }
    @Bean
    public Queue crowdfundingPaymentOrderQueue() {
        return QueueBuilder.durable(CROWDFUNDING_PAYMENT_ORDER_QUEUE).build();
    }
    @Bean
    public Binding crowdfundingPaymentOrderBinding() {
        return BindingBuilder.bind(crowdfundingPaymentOrderQueue()).to(crowdfundingPaymentOrderExchange()).with(CROWDFUNDING_PAYMENT_ORDER_ROUTING_KEY).noargs();
    }

}
