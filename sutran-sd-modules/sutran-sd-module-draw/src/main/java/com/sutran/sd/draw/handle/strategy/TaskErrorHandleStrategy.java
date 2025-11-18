package com.sutran.sd.draw.handle.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.service.SdUserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 任务执行节点更新
 *
 * @author zj
 */
@Service("EXECUTION_ERROR")
@Slf4j
@RequiredArgsConstructor
public class TaskErrorHandleStrategy implements IComfyWebSocketTextHandleStrategy {
    private final SdUserTaskService sdUserTaskService;

    /**
     * 当前消息没有真实进度 使用虚假进度

     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        //当前消息没有真实进度 使用虚假进度;
        log.error("[ComfUI][任务执行错误]>>>>>>>>>节点: {}", dataNode);
        String promptId = dataNode.get("prompt_id").asText();
        String exceptionMessage = dataNode.get("exception_message").asText();
        String nodeId = dataNode.get("node_id").asText();
        String taskId = sdUserTaskService.getTaskIdByPromptId(promptId);
        sdUserTaskService.failComfyTask(taskId,"节点"+nodeId+"执行错误:"+exceptionMessage,new Date());
    }
}
