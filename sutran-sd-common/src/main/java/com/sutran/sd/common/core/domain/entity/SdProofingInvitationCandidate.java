package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 邀约候选厂家报价表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sd_proofing_invitation_candidates")
public class SdProofingInvitationCandidate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    @TableField("invitation_id")
    private Long invitationId;

    @TableField("invitee_user_id")
    private Long inviteeUserId;

    @TableField("quoted_price")
    private BigDecimal quotedPrice;

    @TableField("quoted_period_days")
    private Integer quotedPeriodDays;

    @TableField("is_quote_batch_plan")
    private Boolean isQuoteBatchPlan;

    @TableField("tiered_pricing")
    private String tieredPricing; // JSON 字符串

    @TableField("profit_share_ratio")
    private BigDecimal profitShareRatio;

    @TableField("quote_submit_at")
    private Date quoteSubmitAt;

    @TableField("status")
    private Integer status; // 0待处理 1已接受 2已拒绝 4已关闭
}


