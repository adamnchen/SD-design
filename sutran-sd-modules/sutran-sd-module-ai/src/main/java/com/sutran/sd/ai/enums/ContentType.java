package com.sutran.sd.ai.enums;

/**
 * @author zj
 * @date 2025-01-08
 */
public enum ContentType {
    TEXT("TEXT"),
    TEXT_FILE("TEXT_FILE"),
    IMAGE("IMAGE"),
    AUDIO("AUDIO"),
    VIDEO("VIDEO"),
    PDF("PDF"),
    ;
    private final String value;
    private ContentType(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return this.value;
    }
}
