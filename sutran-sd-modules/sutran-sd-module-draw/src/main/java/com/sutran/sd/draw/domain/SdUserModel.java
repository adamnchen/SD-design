package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 参数配置表 sys_config
 *
 * @author Lion Li
 */

@Data
@TableName("sd_user_model")
@Accessors(chain = true)
public class SdUserModel implements Serializable {

    @TableId(value = "id")
    @Schema(name = "id", description = "模型ID")
    private Long id;
    /**
     * 模型标题
     */
    @Schema(name = "title", description = "模型标题")
    private String title;
    /**
     * 模型分类ID
     */
    @Schema(name = "classifyId", description = "模型分类ID")
    private Long classifyId;
    /**
     * 模型名称
     */
    @Schema(name = "modelName", description = "模型名称")
    private String modelName;
    /**
     * 模型名称
     */
    @Schema(name = "modelNameZh", description = "模型别名")
    private String modelNameZh;
    /**
     * 模型名称
     */
    @Schema(name = "modelStrength", description = "模型强度")
    private String modelStrength;
    /**
     * 模型共性词
     */
    @Schema(name = "additionTag", description = "模型共性词")
    private String additionTag;
    /**
     * 模型hash值
     */
    @Schema(name = "hash", description = "模型hash值")
    private String hash;
    /**
     * 模型存储位置
     */
    @Schema(name = "fileName", description = "模型存储位置")
    private String fileName;
    /**
     * 模型配置
     */
    @Schema(name = "config", description = "模型配置")
    private String config;
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
     * 模型归属类型[0-系统,1-个人]
     */
    @Schema(name = "type", description = "模型归属类型[0-系统,1-个人]")
    private Integer type;
    /**
     * 模型是否公开[0-否,1-是]
     */
    @Schema(name = "isOpen", description = "模型是否公开[0-否,1-是]")
    private Integer isOpen;
    /**
     * 模型归属人ID
     */
    @Schema(name = "belongUserId", description = "模型归属人ID")
    private Long belongUserId;
    /**
     * 模型发布状态[0-未发布,1-已发布]
     */
    @Schema(name = "publishStatus", description = "模型发布状态[0-未发布,1-已发布]")
    private Integer publishStatus;
    /**
     * 模型创建时间
     */
    @Schema(name = "crtTime", description = "模型创建时间")
    private Date crtTime;
    /**
     * 模型类型[SDXL,FLUX]
     */
    @Schema(name = "modelType", description = "模型类型[SDXL,FLUX]")
    private String modelType;
}
