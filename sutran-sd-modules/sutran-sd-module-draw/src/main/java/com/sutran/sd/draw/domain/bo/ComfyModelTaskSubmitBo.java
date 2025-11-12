package com.sutran.sd.draw.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年09月09日 22:09
 */
@Data
@Accessors(chain=true)
public class ComfyModelTaskSubmitBo {
    private String modelId;
    private String modelType;
    private String modelName;
    private String modelStrength;
    private String prompt;
    private String promptZh;
    private String batchSize;
    private String checkPoint;
}
