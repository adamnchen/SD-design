package com.sutran.sd.common.core.domain.dto;
import lombok.Data;

/**
 * 【TagCreateDTO】
 * 用户创建标签时提交的数据
 */
@Data
public class UserTagDTO {

    /**
     * 标签名称 (必填)
     */
    // @NotBlank(message = "标签名称不能为空")
    // @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;

    /**
     * 标签说明/描述
     */
    // @Size(max = 255, message = "标签描述不能超过255个字符")
    private String description;

    /**
     * 标签类型：0=厂商+设计师，1=设计师，2=普通用户，3=客户自定义
     */
    private Integer bizType;

    /**
     * 标签等级/重要性：1=普通，2=重要
     */
    private Integer tagLevel;

    /**
     * 排序值
     */
    private Integer sortOrder;
}
