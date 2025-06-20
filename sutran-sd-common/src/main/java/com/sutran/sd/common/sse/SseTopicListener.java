package com.sutran.sd.common.sse;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zj
 * @date 2024-11-22
 */
@Slf4j
@Component
public class SseTopicListener implements ApplicationRunner, Ordered {

    @Resource
    private SseEmitterManager sseEmitterManager;

    /**
     * 在Spring Boot应用程序启动时初始化SSE主题订阅监听器
     *
     * @param args 应用程序参数
     */
    @Override
    public void run(ApplicationArguments args) {
        sseEmitterManager.subscribeMessage((message) -> {
            log.warn("[SSE][主题订阅][消息接收]>>>>>>>>>收到消息session keys={} message={}", message.getUserIds(), message.getMessage());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(key -> {
                    sseEmitterManager.sendMessage(key, message.getMessage());
                });
            } else {
                sseEmitterManager.sendMessage(message.getMessage());
            }
        });
        log.warn("[SSE][订阅]>>>>>>>>>初始化SSE主题订阅监听器成功");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
