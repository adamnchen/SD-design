package com.sutran.sd.common.enums;

import lombok.Getter;

/**
 * 用户身份类型枚举
 *
 * @author SutranSD
 * @date 2025-10-24
 */
@Getter
public enum UserBizType {

    /**
     * 厂商和设计师（既是厂商又是设计师）
     */
    MANUFACTURER_AND_DESIGNER(0, "厂商和设计师"),

    /**
     * 设计师
     */
    DESIGNER(1, "设计师"),

    /**
     * 普通用户
     */
    NORMAL_USER(2, "普通用户");

    /**
     * 类型值
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    UserBizType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取类型
     */
    public static UserBizType getByCode(Integer code) {
        if (code == null) {
            return NORMAL_USER;
        }
        for (UserBizType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return NORMAL_USER;
    }

    /**
     * 是否是厂商
     */
    public boolean isManufacturer() {
        return this == MANUFACTURER_AND_DESIGNER;
    }

    /**
     * 是否是设计师
     */
    public boolean isDesigner() {
        return this == DESIGNER || this == MANUFACTURER_AND_DESIGNER;
    }

    /**
     * 是否是普通用户
     */
    public boolean isNormalUser() {
        return this == NORMAL_USER;
    }
}

