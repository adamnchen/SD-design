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
 * 批量订单对象 sd_presale_order
 *
 * @author sutran
 * @date 2025-10-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_presale_order")
public class SdPresaleOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 订单号（唯一）
     */
    @TableField("order_no")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 项目ID
     */
    @TableField("project_id")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 购买用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 购买用户姓名
     */
    @TableField("user_name")
    @NotBlank(message = "用户姓名不能为空")
    private String userName;

    /**
     * 商品标题
     */
    @TableField("product_title")
    @NotBlank(message = "商品标题不能为空")
    private String productTitle;

    /**
     * 商品图片
     */
    @TableField("product_image")
    private String productImage;

    /**
     * 购买数量
     */
    @TableField("quantity")
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

    /**
     * 下单时的单价
     */
    @TableField("original_unit_price")
    @NotNull(message = "原始单价不能为空")
    private BigDecimal originalUnitPrice;

    /**
     * 最终确认单价（阶梯价格调整后）
     */
    @TableField("final_unit_price")
    private BigDecimal finalUnitPrice;

    /**
     * 下单时的总金额
     */
    @TableField("original_total_amount")
    @NotNull(message = "原始总金额不能为空")
    private BigDecimal originalTotalAmount;

    /**
     * 最终确认总金额
     */
    @TableField("final_total_amount")
    private BigDecimal finalTotalAmount;

    /**
     * 退款金额（阶梯价格调整后的退款）
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

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
     * 支付订单ID（关联pay_order表）
     */
    @TableField("pay_order_id")
    private Long payOrderId;

    /**
     * 订单状态：1=待支付，2=已支付，3=生产中，4=已发货，5=已完成，6=已取消，7=已退款
     */
    @TableField("order_status")
    @NotNull(message = "订单状态不能为空")
    private Integer orderStatus;

    /**
     * 发货状态：0=未开始，1=已发货，2=已签收
     */
    @TableField("delivery_status")
    private Integer deliveryStatus;

    /**
     * 快递单号
     */
    @TableField("express_no")
    private String expressNo;

    /**
     * 发货时间
     */
    @TableField("delivery_time")
    private Date deliveryTime;

    /**
     * 签收时间
     */
    @TableField("receive_time")
    private Date receiveTime;

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
}
