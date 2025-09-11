package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 任务执行节点更新
 *
 * @author zj
 */
@Service("PROGRESS_STATE")
@Slf4j
public class TaskNodeUpdateHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 当前消息没有真实进度 使用虚假进度
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        String nodeId = dataNode.get("node").asText();
        //当前消息没有真实进度 使用虚假进度;
        log.warn("{}任务执行节点更新,任务id: {},comfyui内部任务id: {},节点id: {}", msgType.name(), taskId, promptId, nodeId);
    }
}
