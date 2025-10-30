package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
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

     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        //当前消息没有真实进度 使用虚假进度;
        log.info("[任务执行节点更新]>>>>>>>>>节点: {}", dataNode);
    }
}
