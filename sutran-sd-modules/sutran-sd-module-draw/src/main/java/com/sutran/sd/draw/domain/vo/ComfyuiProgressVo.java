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
public class ComfyuiProgressVo implements Serializable {
    /**
     * 执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]
     */
    private Integer status;
    /**
     * 任务进度
     */
    private Integer progress;
}
