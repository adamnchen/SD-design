package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 用户信誉积分表
 * 
 * @author sutran
 * @date 2025-10-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sd_user_normal_point")
public class SdUserNormalPoint extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 打样积分
     */
    private Integer proofingPoint;

    /**
     * 打样荣誉等级
     */
    private Integer proofingLevel;

    /**
     * 销售积分
     */
    private String salePoint;

    /**
     * 销售荣誉等级
     */
    private String saleLevel;
}
