package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模型实体类
 * 
 * @author SutranSD
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("model")
public class Model {
    
    /**
     * 模型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 模型分类ID
     */
    @TableField("classify_id")
    private Long classifyId;
    
    /**
     * 模型标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;
    
    /**
     * 模型别名
     */
    @TableField("model_name_zh")
    private String modelNameZh;
    
    /**
     * 模型强度
     */
    @TableField("model_strength")
    private String modelStrength;
    
    /**
     * 模型共性词（JSON格式）
     */
    @TableField("addition_tag")
    private String additionTag;
    
    /**
     * 模型hash值
     */
    @TableField("hash")
    private String hash;
    
    /**
     * 模型存储位置
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 模型配置（JSON格式）
     */
    @TableField("config")
    private String config;
    
    /**
     * 模型封面地址
     */
    @TableField("url")
    private String url;
    
    /**
     * 模型描述
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 模型归属类型[0-系统,1-个人]
     */
    @TableField("type")
    private Integer type;
    
    /**
     * 模型是否公开[0-否,1-是]
     */
    @TableField("is_open")
    private Integer isOpen;
    
    /**
     * 模型归属人ID
     */
    @TableField("belong_user_id")
    private String belongUserId;
    
    /**
     * 发布状态[0-否,1-是]
     */
    @TableField("publish_status")
    private Integer publishStatus;
    
    /**
     * 模型创建时间
     */
    @TableField("crt_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crtTime;
    
    /**
     * 用户是否已删除该模型[0-否,1-是]
     */
    @TableField("is_user_del")
    private Integer isUserDel;
    
    /**
     * 模型类型[SDXL, FLUX]
     */
    @TableField("model_type")
    private String modelType;
    
    // 常量定义
    public static final class Type {
        /** 系统模型 */
        public static final int SYSTEM = 0;
        /** 个人模型 */
        public static final int PERSONAL = 1;
    }
    
    public static final class OpenStatus {
        /** 不公开 */
        public static final int CLOSED = 0;
        /** 公开 */
        public static final int OPEN = 1;
    }
    
    public static final class PublishStatus {
        /** 未发布 */
        public static final int UNPUBLISHED = 0;
        /** 已发布 */
        public static final int PUBLISHED = 1;
    }
    
    public static final class DeleteStatus {
        /** 未删除 */
        public static final int NOT_DELETED = 0;
        /** 已删除 */
        public static final int DELETED = 1;
    }
    
    public static final class ModelType {
        /** SDXL模型 */
        public static final String SDXL = "SDXL";
        /** FLUX模型 */
        public static final String FLUX = "FLUX";
    }
}
