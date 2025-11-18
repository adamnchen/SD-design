package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年11月16日 21:41
 */
@Data
@Accessors(chain = true)
public class ComfyuiDoingTaskVo implements Serializable {
    /**
     * 任务类型[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图,4-工具修复]
     */
    private Integer category;
    /**
     * 正在进行的任务ID
     */
    private String taskId;
    /**
     * 使用的工作流ID
     */
    private String flowId;
}
