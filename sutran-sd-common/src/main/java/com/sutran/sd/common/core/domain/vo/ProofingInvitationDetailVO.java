package com.sutran.sd.common.core.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 打样邀约详情 VO
 */
@Data
public class ProofingInvitationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邀请单ID
     */
    private Long id;

    /**
     * 关联的作品ID
     */
    private String workId;

    // --- 作品和设计详情 ---
    private String productTitle;
    private String productDescription;
    private String modelSource;

    // --- 邀约人/被邀约人信息 ---
    private Long inviterUserId;
    private Long inviteeUserId;
    // 实际业务中，这里通常还会包含 userNickname 或 factoryProfile 等字段，
    // 以便前端展示用户/厂商名称，但这里只映射数据库字段。

    // --- 邀约细节和要求 ---
    private String cooperationContent;
    private String keywords;
    private Boolean isBatchProduction;
    private Integer proofingQuantity;

    // --- 时限 ---
    private Integer deliveryLimitHours;
    private Integer cancelTimeLimit;
    
    /**
     * 取消时限描述 (例如: "2天内无人应答自动取消")
     */
    private String cancelTimeLimitDescription;

    // --- 报价信息 (被邀约方填写) ---
    /**
     * 报价金额 (元)
     */
    private BigDecimal quotedPrice;

    /**
     * 预计打样周期 (天)
     */
    private Integer quotedPeriodDays;

    /**
     * 报价时是否提供了批量生产方案
     */
    private Boolean isQuoteBatchPlan;

    /**
     * 报价提交时间
     */
    private Date quoteSubmitAt;

    // --- 状态和时间 ---

    /**
     * 邀约状态 (0: 待处理, 1: 已接受, 2: 已拒绝, 3: 已取消)
     */
    private Integer status;

    /**
     * 创建时间 (邀约时间)
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;

    /**
     * 【新增】发起人的用户昵称 (从 sys_user 表关联查询)
     */
    private String inviterNickName;

    /**
     * 【新增】发起人的用户头像 (从 sys_user 表关联查询)
     */
    private String inviterAvatar;

    /**
     * 【新增】被邀约人的用户昵称 (从 sys_user 表关联查询)
     */
    private String inviteeNickName;

    /**
     * 【新增】被邀约人的用户头像 (从 sys_user 表关联查询)
     */
    private String inviteeAvatar;

    /**
     * 打样样品参与抽奖的数量分配给众筹用户,最少一个
     */
    private Integer drawNumber;
}
