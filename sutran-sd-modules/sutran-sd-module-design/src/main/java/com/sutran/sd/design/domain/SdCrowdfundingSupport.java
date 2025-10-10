package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹支持记录对象 sd_crowdfunding_support
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
@TableName("sd_crowdfunding_support")
public class SdCrowdfundingSupport {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支持编号
     */
    @TableField("support_no")
    private String supportNo;

    /**
     * 众筹项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 支持金额
     */
    @TableField("support_amount")
    private BigDecimal supportAmount;

    /**
     * 支持留言
     */
    @TableField("message")
    private String message;

    /**
     * 是否匿名
     */
    @TableField("is_anonymous")
    private Integer isAnonymous;

    /**
     * 状态：0-正常，1-已取消，2-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付状态：0-待支付，1-已支付，2-支付失败，3-已退款
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private Date paymentTime;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private Date refundTime;

    /**
     * 退款原因
     */
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private Date cancelTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Date updateTime;
}