package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_TASK_PROGRESS;

/**
 * 绘图任务工作流节点进度更新
 *
 * @author zj
 */
@Service("PROGRESS")
@Slf4j
public class TaskProgressHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    /**
     * 获取任务进度
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        log.warn("{}任务进度更新,任务id: {},comfyui内部任务id: {},节点id: {}", msgType.name(), taskId, promptId, dataNode.get("node").asText());
        //当消息类型为progress时有真实进度
        int current = dataNode.get("value").asInt();
        int max = dataNode.path("max").asInt();
        //计算进度百分比
        int percent = 100;
        if (max!= 0) {
            BigDecimal bd1 = new BigDecimal(current);
            BigDecimal bd2 = new BigDecimal(max);
            percent = bd1.divide(bd2, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        }
        // 添加任务进度缓存
        RedisUtils.setCacheMapValue(DRAW_TASK_PROGRESS, taskId, percent);
    }
}
