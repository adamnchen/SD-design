package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 训练任务状态
 * @author zj
 * @date 2025-05-25
 */
@Data
@Accessors(chain = true)
public class TrainTaskStatusVo implements Serializable {
    /**
     * 预处理任务ID
     */
    private String preTaskId;
    /**
     * 训练任务ID
     */
    private String taskId;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    private Integer newStatus;
}
