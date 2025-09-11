package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 队列任务数量更新
 * @author zj
 */
@Slf4j
@Service("TASK_NUMBER")
public class TaskNumberUpdateHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 处理消息
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        int taskNumber = dataNode.get("status").get("exec_info").get("queue_remaining").asInt();
        log.warn("{}任务队列数量更新,任务id: {},comfyui内部任务id: {},队列数量: {}", msgType.name(), taskId, promptId, taskNumber);
    }
}
