package com.sutran.sd.design.domain;

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
 * 预售发货记录对象 sd_presale_delivery
 *
 * @author sutran
 * @date 2025-10-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_presale_delivery")
public class SdPresaleDelivery extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 发货记录ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 发货单号（唯一）
     */
    @TableField("delivery_no")
    @NotBlank(message = "发货单号不能为空")
    private String deliveryNo;

    /**
     * 预售项目ID
     */
    @TableField("project_id")
    @NotNull(message = "预售项目ID不能为空")
    private Long projectId;

    /**
     * 预售订单ID
     */
    @TableField("order_id")
    @NotNull(message = "预售订单ID不能为空")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 收货用户ID
     */
    @TableField("user_id")
    @NotNull(message = "收货用户ID不能为空")
    private Long userId;

    /**
     * 收货用户姓名
     */
    @TableField("user_name")
    @NotBlank(message = "收货用户姓名不能为空")
    private String userName;

    /**
     * 商品标题
     */
    @TableField("product_title")
    @NotBlank(message = "商品标题不能为空")
    private String productTitle;

    /**
     * 发货数量
     */
    @TableField("quantity")
    @NotNull(message = "发货数量不能为空")
    private Integer quantity;

    /**
     * 总金额
     */
    @TableField("total_amount")
    @NotNull(message = "总金额不能为空")
    private BigDecimal totalAmount;

    /**
     * 收货人姓名
     */
    @TableField("recipient_name")
    @NotBlank(message = "收货人姓名不能为空")
    private String recipientName;

    /**
     * 收货人手机号
     */
    @TableField("recipient_phone")
    private String recipientPhone;

    /**
     * 收货地址
     */
    @TableField("recipient_address")
    @NotBlank(message = "收货地址不能为空")
    private String recipientAddress;

    /**
     * 快递单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 发货状态：1=待发货，2=已发货
     */
    @TableField("delivery_status")
    @NotNull(message = "发货状态不能为空")
    private Integer deliveryStatus;

    /**
     * 发货时间
     */
    @TableField("delivery_time")
    private Date deliveryTime;

    /**
     * 发货人用户ID
     */
    @TableField("sender_user_id")
    private Long senderUserId;
}
