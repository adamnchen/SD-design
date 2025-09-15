package com.sutran.sd.comfyapi.service;

import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.pojo.ComfyTaskHistoryInfo;

/**
 * @author zj
 * @date 2025年09月09日 22:02
 */
public interface ComfyTaskService {

    /**
     * 提交模型生图任务
     * @param modelTaskBo 任务参数
     * @return 任务id
     */
    String submitModelTask(ComfyModelTaskSubmitBo modelTaskBo);

    /**
     * 提交工作流生图任务
     * @param flowId 工作流id
     * @return 任务id
     */
    String submitComfyFlowTask(String flowId);

    /**
     * 获取模型指定历史任务详情
     * @param taskId 任务id
     * @return 任务详情
     */
    ComfyTaskHistoryInfo getComfyModelHistoryTask(String taskId);

    /**
     * 获取任务进度
     * @param taskId 任务id
     * @return 任务进度
     */
    Integer getComfyTaskProgress(String taskId);
}
