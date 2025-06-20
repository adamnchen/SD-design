package com.sutran.sd.ai.enums;

import lombok.Getter;

/**
 * @author zj
 * @date 2024-12-21
 */
@Getter
public enum SessionType {
    TEXT("text","文本对话"),
    IMAGE("image","图片对话"),
    ;
    private final String code;
    private final String desc;
    private SessionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
