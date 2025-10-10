package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 众筹抽奖奖品领取DTO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingDrawClaimDTO {

    /**
     * 抽奖记录ID
     */
    @NotNull(message = "抽奖记录ID不能为空")
    private Long drawId;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String claimName;

    /**
     * 收货电话
     */
    @NotBlank(message = "收货电话不能为空")
    private String claimPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    private String claimAddress;
}
