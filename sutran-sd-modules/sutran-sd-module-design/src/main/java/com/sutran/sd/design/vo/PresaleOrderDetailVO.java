package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 预售订单详情VO
 *
 * @author sutran
 * @date 2025-10-19
 */
@Data
public class PresaleOrderDetailVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 原始单价
     */
    private BigDecimal originalUnitPrice;

    /**
     * 最终单价
     */
    private BigDecimal finalUnitPrice;

    /**
     * 原始总金额
     */
    private BigDecimal originalTotalAmount;

    /**
     * 最终总金额
     */
    private BigDecimal finalTotalAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 支付订单ID
     */
    private Long payOrderId;

    /**
     * 订单状态：1=待支付，2=已支付，3=生产中，4=已发货，5=已完成，6=已取消，7=已退款
     */
    private Integer orderStatus;

    /**
     * 订单状态描述
     */
    private String orderStatusDesc;

    /**
     * 发货状态：0=未开始，1=已发货，2=已签收
     */
    private Integer deliveryStatus;

    /**
     * 发货状态描述
     */
    private String deliveryStatusDesc;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 发货时间
     */
    private Date deliveryTime;

    /**
     * 签收时间
     */
    private Date receiveTime;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否已退款
     */
    private Boolean hasRefund;

 

    public String getOrderStatusDesc() {
        if (orderStatus == null) {
            return "未知";
        }
        switch (orderStatus) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "生产中";
            case 4: return "已发货";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "已退款";
            default: return "未知";
        }
    }

    public String getDeliveryStatusDesc() {
        if (deliveryStatus == null) {
            return "未开始";
        }
        switch (deliveryStatus) {
            case 0: return "未开始";
            case 1: return "已发货";
            case 2: return "已签收";
            default: return "未知";
        }
    }

    public Boolean getHasRefund() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getPriceAdjustmentNote() {
        if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("由于达到阶梯价格门槛，已自动退款 %.2f 元", refundAmount);
        }
        return null;
    }
}
