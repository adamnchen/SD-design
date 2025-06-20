package com.sutran.sd.ai.enums;

import lombok.Getter;

/**
 * Qwen模型类型
 * @author zj
 * @date 2024-12-20
 */
@Getter
public enum QwenModelType {
    QWEN_TURBO("qwen-turbo","千问Turbo"),
    QWEN_PLUS("qwen-plus","千问Plus"),
    QWEN_MAX("qwen-max","千问Max"),
    QWEN_LONG("qwen-long","千问Long"),
    QWEN_VL_PLUS("qwen-vl-plus","千问VL-PLUS"),
    QWEN_VL_MAX("qwen-vl-max","千问VL-PLUS"),
    ;
    private final String name;
    private final String desc;
    QwenModelType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
}
