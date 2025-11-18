package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
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
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        // 任务取消
        log.warn("[ComfUI][任务取消]>>>>>>>>>节点: {}", dataNode);
    }
}
