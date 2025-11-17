package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.redis.RedisUtils;
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
public class TaskNodeExecutingHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 获取任务进度
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        log.warn("[ComfUI][任务节点执行中]>>>>>>>>>当前节点：{}",dataNode);
        RedisUtils.setCacheListValue("COMFYUI_EXECUTING_NODE_LIST:"+dataNode.get("prompt_id").asText(),dataNode.get("node").asText());
    }
}
