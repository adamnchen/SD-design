package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
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
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        log.info("[任务队列数量更新]>>>>>>>>>节点信息: {}", dataNode);
    }
}
