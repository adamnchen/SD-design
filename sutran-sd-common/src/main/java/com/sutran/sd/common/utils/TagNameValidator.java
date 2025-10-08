package com.sutran.sd.common.utils;

import com.sutran.sd.common.exception.ServiceException;

import java.util.Arrays;
import java.util.List;

/**
 * 标签名称验证工具类
 * 防止用户输入与身份标签冲突的词汇
 *
 * @author SutranSD
 */
public class TagNameValidator {

    /**
     * 禁止的身份标签关键词列表
     */
    private static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
        // 中文关键词
        "厂商", "设计师", "普通用户", "用户",
        "身份", "角色", "类型", "分类",
        "系统", "管理", "admin", "administrator",
        
        // 英文关键词
        "manufacturer", "designer", "user", "normal",
        "identity", "role", "type", "category",
        "system", "admin", "administrator",
        
        // 其他可能冲突的词汇
        "官方", "official", "认证", "verified",
        "特殊", "special", "高级", "premium"
    );

    /**
     * 系统身份标签的精确匹配列表
     */
    private static final List<String> SYSTEM_IDENTITY_TAGS = Arrays.asList(
        "厂商", "设计师", "普通用户", "用户",
        "manufacturer", "designer", "normal user", "user"
    );

    /**
     * 验证标签名称
     * @param tagName 标签名称
     * @throws ServiceException 如果标签名称包含禁止词汇
     */
    public static void validate(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return;
        }
        
        String lowerTagName = tagName.toLowerCase().trim();
        
        // 检查是否与系统身份标签完全匹配
        for (String systemTag : SYSTEM_IDENTITY_TAGS) {
            if (lowerTagName.equals(systemTag.toLowerCase())) {
                throw new ServiceException("该标签名称与系统身份标签冲突，请使用其他名称。");
            }
        }
        
        // 检查是否包含禁止的关键词
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lowerTagName.contains(keyword.toLowerCase())) {
                throw new ServiceException(
                    String.format("标签名称不能包含身份相关词汇，如：%s等。请使用其他描述性词汇。", 
                    String.join("、", FORBIDDEN_KEYWORDS.subList(0, Math.min(5, FORBIDDEN_KEYWORDS.size()))))
                );
            }
        }
        
        // 检查标签名称长度
        if (tagName.length() > 50) {
            throw new ServiceException("标签名称不能超过50个字符。");
        }
        
        if (tagName.length() < 2) {
            throw new ServiceException("标签名称至少需要2个字符。");
        }
    }

    /**
     * 检查标签名称是否包含禁止词汇（不抛异常）
     * @param tagName 标签名称
     * @return true 如果包含禁止词汇
     */
    public static boolean containsForbiddenKeywords(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        
        String lowerTagName = tagName.toLowerCase().trim();
        
        // 检查系统身份标签
        for (String systemTag : SYSTEM_IDENTITY_TAGS) {
            if (lowerTagName.equals(systemTag.toLowerCase())) {
                return true;
            }
        }
        
        // 检查禁止关键词
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lowerTagName.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取建议的替代词汇
     * @return 建议的标签名称示例
     */
    public static List<String> getSuggestedTagNames() {
        return Arrays.asList(
            "专业技能", "兴趣爱好", "工作领域", "擅长方向",
            "个人特长", "专业背景", "技能标签", "能力标签",
            "专业领域", "工作技能", "个人技能", "专业特长"
        );
    }
}
