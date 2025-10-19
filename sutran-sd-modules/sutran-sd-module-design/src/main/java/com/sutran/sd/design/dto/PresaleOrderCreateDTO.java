package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建预售订单DTO
 *
 * @author sutran
 * @date 2025-10-19
 */
@Data
public class PresaleOrderCreateDTO {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

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
     * 买家备注
     */
    private String buyerNotes;
}
