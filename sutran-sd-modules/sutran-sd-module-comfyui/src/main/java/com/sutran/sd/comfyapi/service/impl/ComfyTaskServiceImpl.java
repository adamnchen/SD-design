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
     * 提交模型生图任务
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
        if (StringUtils.isBlank(modelTaskBo.getBatchSize()) || Integer.parseInt(modelTaskBo.getBatchSize())<=0) {
            throw new TaskErrorException("生图数量至少1张");
        }
        final int batchSize = Integer.parseInt(modelTaskBo.getBatchSize());
        // 根据模型类型获取工作流
        SdFlow sdFlow = sdFlowService.getNoFixedFlow(modelTaskBo.getModelType());
        if (sdFlow == null || StringUtils.isBlank(sdFlow.getFlow())) {
            throw new TaskErrorException(String.format("未找到模型类型为[%s]的工作流", modelTaskBo.getModelType()));
        }
        // 校验生图数量,获取当前用户对应的会员的剩余数量并扣除本次绘图数量
        userService.checkDrawNumOfMember(userId,batchSize);

        // 替换lora模型和强度
        String flow = sdFlow.getFlow().replace("{{lora_model}}",modelTaskBo.getModelName())
            .replace("\"{{lora_model_strength}}\"",modelTaskBo.getModelStrength())
            .replace("\"{{batch_size}}\"",modelTaskBo.getBatchSize());
        if (StringUtils.isNotBlank(modelTaskBo.getPrompt())) {
            flow = flow.replace("{{prompt}}",modelTaskBo.getPrompt());
        }

        // 生图任务落库
        final String taskId = IdUtil.getSnowflakeNextIdStr();
        sdUserTaskService.addComfyTask(taskId,userId,userName,flow,modelTaskBo.getPrompt(),modelTaskBo.getPromptZh());
        // 生图任务存放到MQ队列
        DrawingTaskInfo taskInfo = new DrawingTaskInfo(taskId, JSONObject.parseObject(flow),10,userId,batchSize);
        submitComfyTaskToQueue(taskInfo);
        return taskId;
    }

    /**
     * 提交工作流生图任务
     * @param flowId 工作流id
     * @return 任务id
     */
    @Override
    public String submitComfyFlowTask(String flowId) {
//        final Long userId = LoginHelper.getUserId();
        final Long userId = 1838096394063040512L;
//        final String userName = LoginHelper.getUsername();
        final String userName = "18852862861";
        // 根据模型类型获取工作流
        SdFlow sdFlow = sdFlowService.getFixedFlowById(flowId);
        if (sdFlow == null || StringUtils.isBlank(sdFlow.getFlow())) {
            throw new TaskErrorException("未找到工作流");
        }
        if (sdFlow.getDrawNum() == null || sdFlow.getDrawNum() <= 0) {
            throw new TaskErrorException(String.format("工作流[%s]未配置生图数量", sdFlow.getName()));
        }
        // 校验生图数量,获取当前用户对应的会员的剩余数量并扣除本次绘图数量
        userService.checkDrawNumOfMember(userId, sdFlow.getDrawNum());

        // 生图任务落库
        final String taskId = IdUtil.getSnowflakeNextIdStr();
        sdUserTaskService.addComfyTask(taskId, userId, userName, sdFlow.getFlow(), null, null);
        // 生图任务存放到MQ队列
        DrawingTaskInfo taskInfo = new DrawingTaskInfo(taskId, JSONObject.parseObject(sdFlow.getFlow()), 10, userId, sdFlow.getDrawNum());
        submitComfyTaskToQueue(taskInfo);
        return taskId;
    }

    /**
     * 获取模型指定历史任务详情
     * @param taskId 任务id
     * @return 任务详情
     */
    @Override
    public ComfyTaskHistoryInfo getComfyModelHistoryTask(String taskId) {
        String promptId = sdUserTaskService.getPromptIdByTaskId(taskId);
        if (StringUtils.isBlank(promptId)) {
            throw new TaskErrorException("未找到任务");
        }
        return comfyApiService.getTaskInfoById(promptId);
    }

    /**
     * 获取任务进度
     * @param taskId 任务id
     * @return 任务进度
     */
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
