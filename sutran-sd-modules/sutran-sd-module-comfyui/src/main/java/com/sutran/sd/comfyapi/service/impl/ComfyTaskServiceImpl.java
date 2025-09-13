package com.sutran.sd.comfyapi.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.comfyapi.domain.DrawingTaskInfo;
import com.sutran.sd.comfyapi.service.ComfyApiService;
import com.sutran.sd.comfyapi.service.ComfyTaskService;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdFlow;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.pojo.ComfyTaskHistoryInfo;
import com.sutran.sd.draw.service.SdFlowService;
import com.sutran.sd.draw.service.SdUserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_TASK_PROGRESS;
import static com.sutran.sd.draw.mq.MqConstant.SD_COMFY_DRAW_EXCHANGE;
import static com.sutran.sd.draw.mq.MqConstant.SD_COMFY_DRAW_ROUTING_KEY;

/**
 * @author zj
 * @date 2025年09月09日 22:04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyTaskServiceImpl implements ComfyTaskService {

    private final SdFlowService sdFlowService;
    private final UserService userService;
    private final SdUserTaskService sdUserTaskService;
    private final RabbitTemplate rabbitTemplate;
    private final ComfyApiService comfyApiService;

    /**
     * 提交模型任务
     * @param modelTaskBo 任务参数
     * @return 任务id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitModelTask(ComfyModelTaskSubmitBo modelTaskBo) {
//        final Long userId = LoginHelper.getUserId();
        final Long userId = 1838096394063040512L;
//        final String userName = LoginHelper.getUsername();
        final String userName = "18852862861";
        // 根据模型类型获取工作流
        SdFlow sdFlow = sdFlowService.getFlow(modelTaskBo.getModelType());
        if (sdFlow == null || StringUtils.isBlank(sdFlow.getFlow())) {
            throw new TaskErrorException(String.format("未找到模型类型为[%s]的工作流", modelTaskBo.getModelType()));
        }
        if (sdFlow.getDrawNum()==null || sdFlow.getDrawNum()<=0) {
            throw new TaskErrorException(String.format("[%s]的工作流[%s]未配置生图数量",modelTaskBo.getModelType(),sdFlow.getName()));
        }
        // 校验生图数量,获取当前用户对应的会员的剩余数量
        userService.checkDrawNumOfMember(userId,sdFlow.getDrawNum());

        // 替换lora模型和强度
        String flow = sdFlow.getFlow().replace("{{lora_model}}",modelTaskBo.getModelName())
            .replace("\"{{lora_model_strength}}\"",modelTaskBo.getModelStrength());
        if (StringUtils.isNotBlank(modelTaskBo.getPrompt())) {
            flow = flow.replace("{{prompt}}",modelTaskBo.getPrompt());
        }

        // 生图任务落库
        final String taskId = IdUtil.getSnowflakeNextIdStr();
        sdUserTaskService.addComfyTask(taskId,userId,userName,flow,modelTaskBo.getPrompt(),modelTaskBo.getPromptZh());
        // 生图任务存放到MQ队列
        DrawingTaskInfo taskInfo = new DrawingTaskInfo(taskId, JSONObject.parseObject(flow),10);
        submitComfyTaskToQueue(taskInfo);
        return taskId;
    }

    @Override
    public ComfyTaskHistoryInfo getComfyModelHistoryTask(String promptId) {
        return comfyApiService.getTaskInfoById(promptId);
    }

    @Override
    public Integer getComfyTaskProgress(String taskId) {
        return RedisUtils.getCacheMapValue(DRAW_TASK_PROGRESS, taskId);
    }

    /**
     * 提交任务到队列
     * @param taskInfo 任务信息
     */
    public void submitComfyTaskToQueue(DrawingTaskInfo taskInfo) {
        try {
            rabbitTemplate.convertAndSend(SD_COMFY_DRAW_EXCHANGE,SD_COMFY_DRAW_ROUTING_KEY,taskInfo,new CorrelationData(taskInfo.getTaskId()));
        }
        catch (Exception e) {
            //重试次数
            int retryCount = 5;
            for (int i = 0; i < retryCount; i++) {
                try {
                    rabbitTemplate.convertAndSend(SD_COMFY_DRAW_EXCHANGE,SD_COMFY_DRAW_ROUTING_KEY,taskInfo,new CorrelationData(taskInfo.getTaskId()));
                }
                catch (Exception ignored) {}
            }
        }
    }
}
