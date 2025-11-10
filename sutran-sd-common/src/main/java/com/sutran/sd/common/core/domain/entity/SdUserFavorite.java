package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户收藏表实体类
 * 支持收藏模型和作品
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sd_user_favorite")
public class SdUserFavorite extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（收藏者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏类型：1=模型，2=作品
     */
    @TableField("favorite_type")
    private Integer favoriteType;

    /**
     * 收藏对象ID（模型ID或作品ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 收藏类型常量
     */
    public static final class FavoriteType {
        /** 模型 */
        public static final int MODEL = 1;
        /** 作品 */
        public static final int WORK = 2;
    }
}

