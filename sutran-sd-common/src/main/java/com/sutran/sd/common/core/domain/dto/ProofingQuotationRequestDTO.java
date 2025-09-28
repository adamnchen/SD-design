package com.sutran.sd.common.core.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交报价请求 DTO (由被邀约方使用)
 */
@Data
public class ProofingQuotationRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 【必填】关联的邀约单ID
     */
    @NotNull(message = "邀约单ID不能为空")
    private Long invitationId;

    /**
     * 【必填】提交的报价金额 (元)
     */
    @NotNull(message = "报价金额不能为空")
    private BigDecimal quotedPrice;

    /**
     * 【必填】预计打样周期 (天)
     */
    @NotNull(message = "预计周期不能为空")
    private Integer quotedPeriodDays;

    /**
     * 报价时是否提供了批量生产方案 (0:否, 1:是)
     */
    private Boolean isQuoteBatchPlan = false;
    // 默认可以设置为false，除非用户勾选。
}
