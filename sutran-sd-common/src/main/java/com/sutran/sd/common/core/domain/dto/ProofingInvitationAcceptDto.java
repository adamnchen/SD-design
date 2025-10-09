package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家接受邀约时提交的数据
 */
@Data
public class ProofingInvitationAcceptDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 【必填】邀约单ID
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
    @NotNull(message = "是否提供批量生产方案不能为空")
    private Boolean isQuoteBatchPlan;

    /**
     * 阶梯价格配置
     * - 数量区间与对应单价
     */
    @Valid
    private List<TierPriceItem> tieredPricing;

    /**
     * 利润分成比例(%)，例如 15.5 表示 15.5%
     */
    @DecimalMin(value = "0.00", message = "利润分成比例不能小于0")
    @DecimalMax(value = "100.00", message = "利润分成比例不能大于100")
    private BigDecimal profitShareRatio;

    @Data
    public static class TierPriceItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 【必填】最小数量(含)
         */
        @NotNull(message = "阶梯最小数量不能为空")
        private Integer minQty;

        /**
         * 【必填】最大数量(含)。若为null表示无上限
         */
        private Integer maxQty;

        /**
         * 【必填】区间对应单价(元)
         */
        @NotNull(message = "阶梯单价不能为空")
        private BigDecimal unitPrice;
    }
}
