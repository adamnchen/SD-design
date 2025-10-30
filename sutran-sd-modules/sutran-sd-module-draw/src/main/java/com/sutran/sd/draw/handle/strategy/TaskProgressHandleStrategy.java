package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.service.SdUserTaskService;
import lombok.RequiredArgsConstructor;
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
@Slf4j
@Service("PROGRESS")
@RequiredArgsConstructor
public class TaskProgressHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    private final SdUserTaskService sdUserTaskService;

    /**
     * 获取任务进度
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        // {"value":1,"max":20,"prompt_id":"d7b64511-54db-4947-a2fa-0080fa379758","node":"42"}
        log.info("[任务进度更新]>>>>>>>>>节点: {}", dataNode);
        String promptId = dataNode.get("prompt_id").asText();
        String taskId = sdUserTaskService.getTaskIdByPromptId(promptId);
        //当消息类型为progress时有真实进度
        int current = dataNode.get("value").asInt();
        int max = dataNode.path("max").asInt();
        //计算进度百分比
        int percent = 99;
        if (max!= 0) {
            BigDecimal bd1 = new BigDecimal(current);
            BigDecimal bd2 = new BigDecimal(max);
            percent = bd1.divide(bd2, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        }
        // 添加任务进度缓存
        if (StringUtils.isNotBlank(taskId)) {
            RedisUtils.setCacheMapValue(DRAW_TASK_PROGRESS, taskId, percent>=100?99:percent);
        }
    }
}
