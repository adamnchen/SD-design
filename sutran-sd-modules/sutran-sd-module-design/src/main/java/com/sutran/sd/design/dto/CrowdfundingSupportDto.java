package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 众筹支持DTO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingSupportDto {

    /**
     * 众筹项目ID
     */
    @NotNull(message = "众筹项目ID不能为空")
    private Long projectId;

    /**
     * 参与者用户ID
     */
    @NotNull(message = "参与者用户ID不能为空")
    private Long userId;

    /**
     * 参与者姓名
     */
    @NotBlank(message = "参与者姓名不能为空")
    private String userName;

    /**
     * 支持金额
     */
    @NotNull(message = "支持金额不能为空")
    private BigDecimal supportAmount;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人手机号
     */
    @NotBlank(message = "收货人手机号不能为空")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    /**
     * 收货地区
     */
    private String receiverArea;
}
