package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.service.SdUserTaskService;
import com.sutran.sd.draw.websocket.ComfyWebsocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_NODE_TASK_MAP;
import static com.sutran.sd.common.constant.CacheConstants.DRAW_TASK_PROGRESS;

/**
 * 任务完成
 * @author zj
 */
@Slf4j
@Service("COMPLETE")
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class TaskCompleteHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    private final SdUserTaskService sdUserTaskService;
    private final ComfyWebsocketClient comfyWebsocketClient;

    /**
     * 任务完成
     *
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        // 任务完成
        log.warn("[任务完成]>>>>>>>>>节点信息: {}", dataNode);
        String promptId = dataNode.get("prompt_id").asText();
        SdUserTaskVo task = sdUserTaskService.getTaskInfoByPromptId(promptId);
        if (task == null) {
            return;
        }
        // 任务完成后，更新任务状态
        sdUserTaskService.completeComfyTask(task.getTaskId(), new Date());
        // 清除缓存中的节点任务
        if (StringUtils.isNotBlank(task.getNodeId().toString())) {
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, task.getNodeId().toString());
        }
        // 清除缓存中的任务进度
        RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, task.getTaskId());
        // 关闭节点的websocket连接
        comfyWebsocketClient.closeComfyUiWebSocket(task.getTaskId());
    }
}
