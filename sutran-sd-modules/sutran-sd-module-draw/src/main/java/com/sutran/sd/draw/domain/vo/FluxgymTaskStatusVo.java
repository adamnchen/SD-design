package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年10月27日 20:50
 */
@Data
@Accessors(chain=true)
public class FluxgymTaskStatusVo {
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    private Integer status;
}
