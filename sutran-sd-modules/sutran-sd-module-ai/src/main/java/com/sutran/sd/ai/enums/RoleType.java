package com.sutran.sd.ai.enums;

/**
 * @author zj
 * @date 2025-01-08
 */
public enum RoleType {
    USER("USER"),
    SYSTEM("SYSTEM"),
    AI("AI"),
    ;
    private final String value;
    private RoleType(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return this.value;
    }
}
