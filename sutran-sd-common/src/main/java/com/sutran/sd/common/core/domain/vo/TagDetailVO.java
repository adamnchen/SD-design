package com.sutran.sd.common.core.domain.vo;

import lombok.Data;
import java.util.Date;

/**
 * 【TagDetailVO】
 * 标签详情展示数据
 */
@Data
public class TagDetailVO {

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 用户ID (用于审计或权限展示)
     */
    private Long userId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签说明/描述
     */
    private String description;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 标签等级/重要性
     */
    private Integer tagLevel;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
