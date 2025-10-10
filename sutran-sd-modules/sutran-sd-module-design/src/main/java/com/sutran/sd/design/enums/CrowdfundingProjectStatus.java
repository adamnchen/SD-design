package com.sutran.sd.design.enums;

/**
 * 众筹项目状态枚举
 *
 * @author sutran
 * @date 2025-10-10
 */
public enum CrowdfundingProjectStatus {
    
    FUNDING(1, "众筹中"),
    SUCCESS(2, "众筹成功"),
    FAILED(3, "众筹失败");

    private final Integer code;
    private final String desc;

    CrowdfundingProjectStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CrowdfundingProjectStatus getByCode(Integer code) {
        for (CrowdfundingProjectStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
