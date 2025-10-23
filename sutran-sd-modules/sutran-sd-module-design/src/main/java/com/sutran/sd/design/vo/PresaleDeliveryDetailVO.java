package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 预售发货记录详情VO
 *
 * @author sutran
 * @date 2025-10-22
 */
@Data
public class PresaleDeliveryDetailVO {

    /**
     * 发货记录ID
     */
    private Long id;

    /**
     * 发货单号
     */
    private String deliveryNo;

    /**
     * 预售项目ID
     */
    private Long projectId;

    /**
     * 预售项目标题
     */
    private String projectTitle;

    /**
     * 预售项目描述
     */
    private String projectDescription;

    /**
     * 预售项目封面图片
     */
    private String projectCoverImage;

    /**
     * 预售订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 收货用户ID
     */
    private Long userId;

    /**
     * 收货用户姓名
     */
    private String userName;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 发货数量
     */
    private Integer quantity;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 收货人姓名
     */
    private String recipientName;

    /**
     * 收货人手机号
     */
    private String recipientPhone;

    /**
     * 收货地址
     */
    private String recipientAddress;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 发货状态：1=待发货，2=已发货
     */
    private Integer deliveryStatus;

    /**
     * 发货状态描述
     */
    private String deliveryStatusText;

    /**
     * 发货时间
     */
    private Date deliveryTime;

    /**
     * 发货人用户ID
     */
    private Long senderUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
