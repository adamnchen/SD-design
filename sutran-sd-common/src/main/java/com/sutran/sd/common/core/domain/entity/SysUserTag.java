package com.sutran.sd.common.core.domain.entity; // 假设你的实体类包路径


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; // 自动生成 Getter, Setter, toString, equals, hashCode

import java.io.Serializable;
import java.util.Date;

/**
 * 【BizUserTag】
 * 用户自定义业务标签实体类
 * 对应数据库表：user_tag
 */
@Data
@TableName("sys_user_tag") // 明确映射到数据库表名
public class SysUserTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID，主键
     */
    @TableId // 标记为主键
    private Long tagId;

    /**
     * 用户ID，关联用户
     */
    private Long userId;

    /**
     * 标签名称，如：厂家、饰品专长
     */
    private String tagName;

    /**
     * 标签说明/描述 (对应数据库的 description)
     */
    private String description;

    /**
     * 标签类型：0=厂商+设计师，1=设计师，2=普通用户，3=客户自定义
     */
    private Integer bizType;

    /**
     * 标签等级/重要性：1=普通，2=重要，3=核心
     */
    private Integer tagLevel;

    /**
     * 排序值，用于前端展示顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间 (对应数据库的 create_time)
     */
    private Date createTime;

    /**
     * 修改时间 (对应数据库的 update_time)
     */
    private Date updateTime;

    /**
     * 逻辑删除标识：0=未删除，1=已删除
     * 注意：当前使用物理删除，不使用逻辑删除
     * 此字段保留但不使用 @TableLogic 注解
     */
    private Integer deleted;
}
