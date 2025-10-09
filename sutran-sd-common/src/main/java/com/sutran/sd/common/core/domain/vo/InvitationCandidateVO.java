package com.sutran.sd.common.core.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvitationCandidateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long inviteeUserId;
    private BigDecimal quotedPrice;
    private Integer quotedPeriodDays;
    private Boolean isQuoteBatchPlan;
    private String tieredPricing; // JSON
    private BigDecimal profitShareRatio;
    private Date quoteSubmitAt;
    private Integer status; // 0待处理 1已接受 2已拒绝 4已关闭

    // 用户展示信息
    private String inviteeNickName;
    private String inviteeAvatar;
}


