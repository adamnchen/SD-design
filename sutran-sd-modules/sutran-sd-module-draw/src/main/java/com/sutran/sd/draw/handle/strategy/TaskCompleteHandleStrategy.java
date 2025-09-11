package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import com.sutran.sd.draw.service.SdUserTaskService;
import com.sutran.sd.draw.websocket.ComfyWebsocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class TaskCompleteHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    private final SdUserTaskService sdUserTaskService;
    private final ComfyWebsocketClient comfyWebsocketClient;

    /**
     * 任务完成
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        // 任务完成
        log.warn("[任务完成]>>>>>>>>>任务id: {},comfyui内部任务id: {}", taskId, promptId);
        // 获取任务关联的节点ID
        String nodeId = sdUserTaskService.getNodeIdByTaskId(taskId);
        // 任务完成后，更新任务状态
        sdUserTaskService.completeComfyTask(taskId, new Date());
        // 清除缓存中的节点任务
        if (StringUtils.isNotBlank(nodeId)) {
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, nodeId);
        }
        // 清除缓存中的任务进度
        RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, taskId);
        // 关闭节点的websocket连接
        comfyWebsocketClient.closeComfyUiWebSocket(taskId);
    }
}
