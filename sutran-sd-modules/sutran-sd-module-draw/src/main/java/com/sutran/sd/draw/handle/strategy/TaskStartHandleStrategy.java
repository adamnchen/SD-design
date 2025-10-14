package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 任务开始
 * @author zj
 */
@Slf4j
@Service("EXECUTION_START")
public class TaskStartHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 处理消息
     *
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        log.info("[任务开始]>>>>>>>>>节点: {}", dataNode);
    }
}
