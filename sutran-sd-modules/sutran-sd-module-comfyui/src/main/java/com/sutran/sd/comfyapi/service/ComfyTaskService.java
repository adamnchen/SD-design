package com.sutran.sd.comfyapi.service;

import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;

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

}
