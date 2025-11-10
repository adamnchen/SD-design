package com.sutran.sd.system.service;

import java.util.List;

/**
 * 违禁词校验服务接口
 * 
 * @author sutran
 * @date 2025-11-07
 */
public interface IForbiddenWordService {

    /**
     * 获取违禁词列表
     * 
     * @return 违禁词列表
     */
    List<String> getForbiddenWords();

    /**
     * 校验文本是否包含违禁词
     * 
     * @param text 待校验的文本
     * @return true-包含违禁词，false-不包含违禁词
     */
    boolean containsForbiddenWord(String text);

    /**
     * 校验文本是否包含违禁词，如果包含则抛出异常
     * 
     * @param text 待校验的文本
     * @param fieldName 字段名称（用于错误提示）
     * @throws com.sutran.sd.common.exception.ServiceException 如果包含违禁词
     */
    void validateForbiddenWord(String text, String fieldName);

    /**
     * 校验文本是否包含违禁词，如果包含则抛出异常
     * 
     * @param text 待校验的文本
     * @throws com.sutran.sd.common.exception.ServiceException 如果包含违禁词
     */
    void validateForbiddenWord(String text);

    /**
     * 批量校验文本列表是否包含违禁词
     * 
     * @param texts 待校验的文本列表
     * @param fieldName 字段名称（用于错误提示）
     * @throws com.sutran.sd.common.exception.ServiceException 如果包含违禁词
     */
    void validateForbiddenWords(List<String> texts, String fieldName);

    /**
     * 批量校验文本列表是否包含违禁词
     * 
     * @param texts 待校验的文本列表
     * @throws com.sutran.sd.common.exception.ServiceException 如果包含违禁词
     */
    void validateForbiddenWords(List<String> texts);
}

