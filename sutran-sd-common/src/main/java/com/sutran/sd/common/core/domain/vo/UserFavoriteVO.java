package com.sutran.sd.common.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户收藏VO
 * 用于返回给前端的收藏信息
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Data
public class UserFavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID（收藏者）
     */
    private Long userId;

    /**
     * 收藏类型：1=模型，2=作品
     */
    private Integer favoriteType;

    /**
     * 收藏类型名称
     */
    private String favoriteTypeName;

    /**
     * 收藏对象ID（模型ID或作品ID）
     */
    private Long targetId;

    /**
     * 目标对象封面/缩略图URL
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}

