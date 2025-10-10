package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹支持VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingSupportVO {

    /**
     * 支持记录ID
     */
    private Long id;

    /**
     * 支持订单号
     */
    private String supportNo;

    /**
     * 众筹项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 支持用户ID
     */
    private Long userId;

    /**
     * 支持用户姓名
     */
    private String userName;

    /**
     * 支持用户头像
     */
    private String userAvatar;

    /**
     * 支持金额
     */
    private BigDecimal supportAmount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态：0=待支付，1=已支付，2=支付失败，3=已退款
     */
    private Integer paymentStatus;

    /**
     * 支付状态描述
     */
    private String paymentStatusDesc;

    /**
     * 支付时间
     */
    private Date paymentTime;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 支持留言
     */
    private String message;

    /**
     * 是否匿名支持
     */
    private Boolean isAnonymous;

    /**
     * 支持状态：0=正常，1=已取消，2=已退款
     */
    private Integer status;

    /**
     * 支持状态描述
     */
    private String statusDesc;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
