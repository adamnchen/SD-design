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
    private Boolean success;
    private String taskId;
    private String status;
    private Integer progress;
    private String message;
    private String detail;
}
