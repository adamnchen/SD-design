package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 设计模块用户生图文件数据记录对象 sd_user_model_file
 *
 * @author sutran
 * @date 2025-10-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_user_model_file")
public class DesignSdUserModelFile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 分类[0-文生图，1-图生图]
     */
    @TableField("category")
    private Integer category;

    /**
     * 是否局部重绘[0-否,1-是]
     */
    @TableField("is_redraw")
    private Integer isRedraw;

    /**
     * 描述词(译文)
     */
    @TableField("prompt")
    private String prompt;

    /**
     * 描述词(原文)
     */
    @TableField("prompt_zh")
    private String promptZh;

    /**
     * 弃用词
     */
    @TableField("prompt_desc")
    private String promptDesc;

    /**
     * 反向提示词(译文)
     */
    @TableField("negative_prompt")
    private String negativePrompt;

    /**
     * 反向提示词(原文)
     */
    @TableField("negative_prompt_zh")
    private String negativePromptZh;

    /**
     * 模型强度
     */
    @TableField("model_strength")
    private String modelStrength;

    /**
     * 参考图片
     */
    @TableField("init_img")
    private String initImg;

    /**
     * 文件地址
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件信息
     */
    @TableField("file_info")
    private String fileInfo;

    /**
     * 文件参数信息
     */
    @TableField("file_parameters")
    private String fileParameters;

    /**
     * 基础大模型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 是否公开[0-否,1-是]
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * lora模型ID
     */
    @TableField("lora_model_id")
    private Long loraModelId;

    /**
     * lora模型名称
     */
    @TableField("lora_title")
    private String loraTitle;

    /**
     * lora模型名称(中文)
     */
    @TableField("lora_title_zh")
    private String loraTitleZh;

    /**
     * lora模型信息数组
     */
    @TableField("lora_info")
    private String loraInfo;

    /**
     * 文件归属人ID
     */
    @TableField("belong_user_id")
    private Long belongUserId;

    /**
     * 文件归属人名称
     */
    @TableField("belong_user_name")
    private String belongUserName;

    /**
     * 创建时间
     */
    @TableField("crt_time")
    private Date crtTime;

    // 重写BaseEntity的字段，设置为不存在于数据库表中
    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private Date createTime;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private Date updateTime;
}
