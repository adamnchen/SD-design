package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 任务中断或取消
 * @author zj
 */
@Service("INTERRUPT")
@Slf4j
@RequiredArgsConstructor
public class TaskInterruptedHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 任务取消
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        // 任务取消
        log.warn("{}任务取消,任务id: {},comfyui内部任务id: {}", msgType.name(), taskId, promptId);
    }
}
