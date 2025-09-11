package com.sutran.sd.draw.handle;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import com.sutran.sd.draw.handle.strategy.IComfyWebSocketTextHandleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于映射进度消息类型与对应的处理策略类
 *
 * @author zj
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComfyWebSocketMessageHandler {

    /**
     * 任务进度信息处理策略
     * key为 消息类型type value对应任务处理策略实现类
     */
    private final Map<ComfyWebSocketMessageType, IComfyWebSocketTextHandleStrategy> msgStrategyMapper;

    /**
     * 处理消息
     *
     * @param msgType  消息类型
     * @param dataNode 消息内容
     * @param taskId     任务id
     * @param promptId   comfyui内部任务ID
     */
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        //获取对应的策略实现类
        IComfyWebSocketTextHandleStrategy strategy = msgStrategyMapper.get(msgType);
        if (strategy == null) {
            log.error("Message processing strategy with message type: {} not found", msgType);
            return;
        }
        strategy.handleMessage(msgType, dataNode, taskId, promptId);
    }
}
