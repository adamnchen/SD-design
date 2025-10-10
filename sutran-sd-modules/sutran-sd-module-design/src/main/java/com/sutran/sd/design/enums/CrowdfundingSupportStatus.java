package com.sutran.sd.design.enums;

/**
 * 众筹支持状态枚举
 *
 * @author sutran
 * @date 2025-10-10
 */
public enum CrowdfundingSupportStatus {
    
    NORMAL(0, "正常"),
    CANCELLED(1, "已取消"),
    REFUNDED(2, "已退款");

    private final Integer code;
    private final String desc;

    CrowdfundingSupportStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CrowdfundingSupportStatus getByCode(Integer code) {
        for (CrowdfundingSupportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
