package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年10月16日 10:33
 */
@Data
@Accessors(chain=true)
public class FluxgymTrainProgressVo {
    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 任务状态（queue/running/completed/failed）
     */
    private String status;
    /**
     * 训练进度
     */
    private Integer progress;
    /**
     * 消息
     */
    private String message;
    /**
     * 详情
     */
    private String detail;
}
