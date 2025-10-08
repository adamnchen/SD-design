package com.sutran.sd.common.constant;

/**
 * 标签相关常量
 *
 * @author sutran
 */
public class TagConstants {

    /**
     * 身份标签 - 厂商+设计师（由系统分配）
     */
    public static final Integer IDENTITY_TAG_MANUFACTURER_DESIGNER = 0;

    /**
     * 身份标签 - 设计师（由系统分配）
     */
    public static final Integer IDENTITY_TAG_DESIGNER = 1;

    /**
     * 身份标签 - 普通用户（由系统分配）
     */
    public static final Integer IDENTITY_TAG_USER = 2;

    /**
     * 业务标签（用户自定义）
     */
    public static final Integer BUSINESS_TAG = 3;

    /**
     * 标签等级 - 普通
     */
    public static final Integer TAG_LEVEL_NORMAL = 1;

    /**
     * 标签等级 - 重要
     */
    public static final Integer TAG_LEVEL_IMPORTANT = 2;

    /**
     * 标签等级 - 核心
     */
    public static final Integer TAG_LEVEL_CORE = 3;

    /**
     * 默认排序值
     */
    public static final Integer DEFAULT_SORT_ORDER = 0;

    /**
     * 默认业务类型（业务标签）
     */
    public static final Integer DEFAULT_BIZ_TYPE = BUSINESS_TAG;

    /**
     * 检查是否为身份标签（0,1,2）
     */
    public static boolean isIdentityTag(Integer bizType) {
        return bizType != null && (bizType == 0 || bizType == 1 || bizType == 2);
    }

    /**
     * 检查是否为业务标签（3）
     */
    public static boolean isBusinessTag(Integer bizType) {
        return bizType != null && bizType == 3;
    }

    /**
     * 默认标签等级（普通）
     */
    public static final Integer DEFAULT_TAG_LEVEL = TAG_LEVEL_NORMAL;
}
