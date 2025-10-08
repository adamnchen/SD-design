package com.sutran.sd.common.core.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 厂商搜索结果VO
 * 用于返回模糊匹配的厂商信息
 *
 * @author SutranSD
 */
@Data
public class ManufacturerSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 厂商用户ID
     */
    private Long userId;

    /**
     * 厂商昵称/名称
     */
    private String nickName;

    /**
     * 厂商头像
     */
    private String avatar;

    /**
     * 厂商标签列表
     */
    private List<String> tags;

    /**
     * 匹配度分数（0-100）
     */
    private Integer matchScore;

    /**
     * 匹配的标签
     */
    private List<String> matchedTags;

    /**
     * 厂商简介
     */
    private String description;

    /**
     * 厂商联系方式
     */
    private String contactInfo;
}
