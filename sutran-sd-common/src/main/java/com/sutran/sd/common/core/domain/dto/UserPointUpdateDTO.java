package com.sutran.sd.common.core.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 用户积分更新DTO
 * 
 * @author sutran
 * @date 2025-10-14
 */
@Data
public class UserPointUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 积分类型：PROOFING-打样积分，SALE-销售积分
     */
    @NotNull(message = "积分类型不能为空")
    private String pointType;

    /**
     * 积分变化量（正数为增加，负数为减少）
     */
    @NotNull(message = "积分变化量不能为空")
    private Integer pointChange;

    /**
     * 变化原因
     */
    private String reason;

    /**
     * 关联订单ID（可选）
     */
    private Long orderId;

    /**
     * 关联订单类型：PROOFING-打样订单，PRESALE-预售订单
     */
    private String orderType;
}
