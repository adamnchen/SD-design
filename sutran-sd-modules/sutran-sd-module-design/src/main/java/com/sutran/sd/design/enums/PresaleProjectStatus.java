package com.sutran.sd.design.enums;

/**
 * 预售项目状态枚举
 *
 * @author sutran
 * @date 2025-10-19
 */
public enum PresaleProjectStatus {

    /**
     * 销售中
     */
    ON_SALE(1, "销售中"),

    /**
     * 暂停销售
     */
    PAUSED(2, "暂停销售"),

    /**
     * 已下架
     */
    OFFLINE(3, "已下架"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String desc;

    PresaleProjectStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PresaleProjectStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PresaleProjectStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
