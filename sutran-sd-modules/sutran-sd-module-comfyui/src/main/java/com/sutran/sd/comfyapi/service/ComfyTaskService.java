package com.sutran.sd.comfyapi.service;

import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.pojo.ComfyTaskHistoryInfo;

/**
 * @author zj
 * @date 2025年09月09日 22:02
 */
public interface ComfyTaskService {

    /**
     * 提交模型任务
     * @param modelTaskBo 任务参数
     * @return 任务id
     */
    String submitModelTask(ComfyModelTaskSubmitBo modelTaskBo);

    /**
     * 获取模型任务详情
     * @param promptId 任务id
     * @return 任务详情
     */
    ComfyTaskHistoryInfo getComfyModelHistoryTask(String promptId);

    /**
     * 获取任务进度
     * @param taskId 任务id
     * @return 任务进度
     */
    Integer getComfyTaskProgress(String taskId);
}
