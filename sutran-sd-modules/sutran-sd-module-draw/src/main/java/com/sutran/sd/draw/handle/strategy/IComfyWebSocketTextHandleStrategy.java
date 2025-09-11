package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;

/**
 * 处理ComfyUI ws的文本消息
 * @author zj
 */
public interface IComfyWebSocketTextHandleStrategy {

    /**
     * 处理消息
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId);
}
