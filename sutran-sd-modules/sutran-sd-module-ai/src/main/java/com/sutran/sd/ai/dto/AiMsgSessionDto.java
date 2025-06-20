package com.sutran.sd.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI会话实体类
 * @author zj
 * @since 2024-03-03
 */
@Data
@Accessors(chain = true)
public class AiMsgSessionDto implements Serializable {
    /**
     * 数据ID[新增自动生成]
     */
    @Schema(name = "id", description = "数据ID[新增自动生成]")
    private String id;
    /**
     * 会话名称
     */
    @Schema(name = "name", description = "会话名称")
    private String name;
    /**
     * 会话类型[text-文本,image-图片]
     */
    @Schema(name = "type", description = "会话类型[text-文本,image-图片]")
    private String type;
    /**
     * 角色描述
     */
    @Schema(name = "roleDesc", description = "角色描述")
    private String roleDesc;
    /**
     * 排序值
     */
    @Schema(name = "orderNum", description = "排序值")
    private Integer orderNum;

}

