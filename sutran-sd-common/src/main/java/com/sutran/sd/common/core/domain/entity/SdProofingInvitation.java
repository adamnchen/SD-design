package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 作品打样合作邀请记录表
 * @author
 * @date
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sd_proofing_invitations")
public class SdProofingInvitation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 邀请单的唯一ID，主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 发起邀约的作品ID，关联作品表
     */
    @TableField("work_id")
    @NotNull(message = "作品ID不能为空")
    private String workId;

    // --- 作品和设计详情 ---

    /**
     * 打样产品的标题
     */
    @TableField("product_title")
    @NotBlank(message = "产品标题不能为空")
    private String productTitle;

    /**
     * 邀约作品的详细描述（如材质、尺寸、风格等）
     */
    @TableField("product_description")
    private String productDescription;

    /**
     * 模型来源或版本号（例如：v2.3）
     */
    @TableField("model_source")
    private String modelSource;

    // --- 邀约人/被邀约人 ---

    /**
     * 邀约人用户ID (创建人)
     */
    @TableField("inviter_user_id")
    @NotNull(message = "邀约人ID不能为空")
    private Long inviterUserId;

    /**
     * 被邀约用户ID
     */
    @TableField("invitee_user_id")
    @NotNull(message = "被邀约人ID不能为空")
    private Long inviteeUserId;

    // --- 邀约细节和要求 ---

    /**
     * 其他合作内容详情
     */
    @TableField("cooperation_content")
    private String cooperationContent;

    /**
     * 发起邀约时填写的关键词/标签
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 是否需要批量生产（0:否, 1:是）
     */
    @TableField("is_batch_production")
    @NotNull(message = "是否需要批量生产不能为空")
    private Boolean isBatchProduction;

    /**
     * 期望的打样数量 (件)
     */
    @TableField("proofing_quantity")
    @NotNull(message = "打样数量不能为空")
    private Integer proofingQuantity;

    // --- 时限 ---

    /**
     * 发起方要求的预计交付时限 (小时)
     */
    @TableField("delivery_limit_hours")
    @NotNull(message = "交付时限不能为空")
    private Integer deliveryLimitHours;

    /**
     * 无人应答自动取消时限 (1:一天, 2:两天, 3:三天)
     */
    @TableField("cancel_time_limit")
    private Integer cancelTimeLimit;

    // --- 报价信息 (由被邀约方填写) ---

    /**
     * 被邀约方提交的报价金额 (元)
     */
    @TableField("quoted_price")
    private BigDecimal quotedPrice;

    /**
     * 被邀约方提交的预计打样周期 (天)
     */
    @TableField("quoted_period_days")
    private Integer quotedPeriodDays;

    /**
     * 报价时是否提供了批量生产方案 (0:否, 1:是)
     */
    @TableField("is_quote_batch_plan")
    private Boolean isQuoteBatchPlan;

    /**
     * 报价提交时间
     */
    @TableField("quote_submit_at")
    private Date quoteSubmitAt;

    /** 最终选中的厂家用户ID */
    @TableField("selected_invitee_user_id")
    private Long selectedInviteeUserId;

    /** 最终选择时间 */
    @TableField("selected_at")
    private Date selectedAt;
    /**
     * 阶梯价格配置(JSON字符串)
     */
    @TableField("tiered_pricing")
    private String tieredPricing;

    /**
     * 利润分成比例(%)，如 15.50
     */
    @TableField("profit_share_ratio")
    private BigDecimal profitShareRatio;

    // --- 状态和时间 ---

    /**
     * 邀约状态 (0: 待处理, 1: 已接受, 2: 已拒绝, 3: 已取消，4.待回应，5已处理，6待确认，7已发布)
     */
    @NotNull(message = "邀约状态不能为空")
    private Integer status; // 使用Integer对应TINYINT

    // 创建时间和更新时间由 BaseEntity 提供
    // 重写字段映射以匹配数据库字段名
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    /**
     * 打样样品参与抽奖的数量分配给众筹用户,最少2个
     */
    @TableField("draw_number")
    @NotNull(message = "抽奖数量不能为空")
    private Integer drawNumber = 2; // 默认2个

    // --- 收货信息 ---

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人手机号
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 收货地区
     */
    @TableField("receiver_area")
    private String receiverArea;


}
