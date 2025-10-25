package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Fluxgym模型列表vo
 * @author zj
 * @date 2025年10月25日 15:48
 */
@Data
@Accessors(chain=true)
public class FluxgymModelListVo {
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型id
     */
    private String modelId;
    /**
     * 模型发布状态[0-未发布,1-已发布]
     */
    private Integer publishStatus;
}
