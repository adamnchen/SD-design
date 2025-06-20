package com.sutran.sd.sdapi.domain.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * SD的Lora模型
 * @author zj
 * @date 2024-03-01
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain = true)
@Schema(name = "SdUserModelModifyDto", description = "Lora模型")
public class SdUserModelModifyDto implements Serializable {

    @Schema(name = "id", description = "模型ID")
    private String id;
    /**
     * 模型分类ID
     */
    @Schema(name = "classifyId", description = "模型分类ID")
    private String classifyId;
    /**
     * 模型封面地址
     */
    @Schema(name = "url", description = "模型封面地址")
    private String url;
    /**
     * 模型描述
     */
    @Schema(name = "remark", description = "模型描述")
    private String remark;
    /**
     * 模型是否公开[0-否,1-是]
     */
    @Schema(name = "isOpen", description = "模型是否公开[0-否,1-是]")
    private Integer isOpen;

}
