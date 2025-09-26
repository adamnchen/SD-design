package com.sutran.sd.common.core.domain.dto;

import lombok.Data;
// import javax.validation.constraints.NotNull;

/**
 * 【TagUpdateDTO】
 * 用户更新标签时提交的数据
 */
@Data
public class TagUpdateDTO {

    /**
     * 标签ID (必填，用于定位记录)
     */
    // @NotNull(message = "标签ID不能为空")
    private Long tagId;

    /**
     * 标签名称 (可选，如果修改)
     */
    private String tagName;

    /**
     * 标签说明/描述
     */
    private String description;

    /**
     * 标签等级/重要性
     */
    private Integer tagLevel;

    /**
     * 排序值
     */
    private Integer sortOrder;

    // 注意：updateTime/createTime/userId 等字段不应允许前端修改
}
