package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户收藏DTO
 * 用于创建收藏时接收前端数据
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Data
public class UserFavoriteDTO {

    /**
     * 收藏类型：1=模型，2=作品
     */
    @NotNull(message = "收藏类型不能为空")
    private Integer favoriteType;

    /**
     * 收藏对象ID（模型ID或作品ID）
     */
    @NotNull(message = "收藏对象ID不能为空")
    private Long targetId;
}

