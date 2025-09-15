package com.sutran.sd.comfyapi.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年09月12日 23:34
 */
@Data
@Accessors(chain=true)
public class ComfyModelTaskBo {
    private String modelId;
    private String prompt;
    private String promptZh;
    private String modelStrength="0.8";
    private String batchSize="1";
}
