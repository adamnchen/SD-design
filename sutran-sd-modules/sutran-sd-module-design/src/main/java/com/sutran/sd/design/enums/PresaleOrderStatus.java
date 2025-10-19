package com.sutran.sd.design.enums;

/**
 * 预售订单状态枚举
 *
 * @author sutran
 * @date 2025-10-19
 */
public enum PresaleOrderStatus {

    /**
     * 待支付
     */
    PENDING_PAYMENT(1, "待支付"),

    /**
     * 已支付
     */
    PAID(2, "已支付"),

    /**
     * 生产中
     */
    IN_PRODUCTION(3, "生产中"),

    /**
     * 已发货
     */
    SHIPPED(4, "已发货"),

    /**
     * 已完成
     */
    COMPLETED(5, "已完成"),

    /**
     * 已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 已退款
     */
    REFUNDED(7, "已退款");

    private final Integer code;
    private final String desc;

    PresaleOrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PresaleOrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PresaleOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
