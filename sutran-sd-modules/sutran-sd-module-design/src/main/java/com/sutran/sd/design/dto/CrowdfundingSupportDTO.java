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
public class CrowdfundingSupportDTO {

    /**
     * 众筹项目ID
     */
    @NotNull(message = "众筹项目ID不能为空")
    private Long projectId;

    /**
     * 支持金额
     */
    @NotNull(message = "支持金额不能为空")
    private BigDecimal supportAmount;

    /**
     * 支持留言
     */
    private String message;

    /**
     * 是否匿名支持：0=否，1=是
     */
    private Boolean isAnonymous;
}
