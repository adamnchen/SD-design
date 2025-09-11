package com.sutran.sd.draw.domain.dto.txt2img;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Lora模型参数请求实体
 * @author zj
 * @date 2024-04-05
 */
@Data
@Accessors(chain = true)
public class SdApiModelParamDto implements Serializable {
    @Schema(name = "modelId", description = "Lora模型ID")
    private String modelId;
    @Schema(name = "modelStrength", description = "Lora模型强度")
    private String modelStrength;
}
