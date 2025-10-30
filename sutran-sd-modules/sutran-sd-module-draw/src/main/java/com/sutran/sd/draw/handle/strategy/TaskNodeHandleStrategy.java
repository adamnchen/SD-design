package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 绘图任务工作流节点进度更新
 *
 * @author zj
 */
@Slf4j
@Service("EXECUTING")
@RequiredArgsConstructor
public class TaskNodeHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 获取任务进度
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        log.warn("[ComfUI][任务节点更新]>>>>>>>>>当前节点：{}",dataNode);
    }
}
