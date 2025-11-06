package com.sutran.sd.draw.domain.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * SD的Lora模型分享
 * @author zj
 * @date 2024-03-01
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain = true)
@Schema(name = "SdUserModelShareDto", description = "Lora模型分享")
public class SdUserModelShareDto implements Serializable {
    /**
     * 模型ID集合
     */
    @Schema(name = "modelId", description = "模型ID集合")
    private String modelId;
    /**
     * 被分享人userId集合
     */
    @Schema(name = "toShareUserIds", description = "被分享人userId(和toSharePhone二选一)")
    private List<String> toShareUserIds;
    /**
     * 模型分享时长(不填，默认为永久)
     */
    @Schema(name = "shareTime", description = "模型分享时长(不填，默认为永久)",hidden = true)
    private Integer shareTime;
    /**
     * 模型分享时长单位(0-分钟，1-小时，2-天，3-月，4-年(默认为1))
     */
    @Schema(name = "shareTime", description = "模型分享时长(不填，默认为永久)",hidden = true)
    private int shareTimeUnit=1;
}
