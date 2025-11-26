package com.sutran.sd.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.system.service.IForbiddenWordService;
import com.sutran.sd.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 违禁词校验服务实现类
 *
 * @author sutran
 * @date 2025-11-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForbiddenWordServiceImpl implements IForbiddenWordService {

    /**
     * 违禁词字典类型
     */
    private static final String FORBIDDEN_WORD_DICT_TYPE = "sys_weijin_code";

    private final ISysDictDataService sysDictDataService;

    @Override
    public List<String> getForbiddenWords() {
        try {
            List<String> words = sysDictDataService.selectDictValueListByDictType(FORBIDDEN_WORD_DICT_TYPE);
            return CollectionUtil.isEmpty(words) ? Collections.emptyList() : words;
        } catch (Exception e) {
            log.error("获取违禁词列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean containsForbiddenWord(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        List<String> forbiddenWords = getForbiddenWords();
        if (CollectionUtil.isEmpty(forbiddenWords)) {
            return false;
        }

        String lowerText = text.toLowerCase().trim();
        for (String word : forbiddenWords) {
            if (StringUtils.isNotBlank(word) && lowerText.contains(word.toLowerCase().trim())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String containsForbiddenWordAndReturn(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        List<String> forbiddenWords = getForbiddenWords();
        if (CollectionUtil.isEmpty(forbiddenWords)) {
            return null;
        }

        String lowerText = text.toLowerCase().trim();
        List<String> returnWords = new ArrayList<>();
        for (String word : forbiddenWords) {
            if (StringUtils.isNotBlank(word) && lowerText.contains(word.toLowerCase().trim())) {
                returnWords.add(word);
            }
        }
        return CollectionUtil.isEmpty(returnWords) ? null : String.join(",", returnWords);
    }

    @Override
    public void validateForbiddenWord(String text, String fieldName) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        if (containsForbiddenWord(text)) {
            String errorMsg = StringUtils.isNotBlank(fieldName)
                ? String.format("%s包含违禁词，请修改后重试", fieldName)
                : "输入内容包含违禁词，请修改后重试";
            throw new ServiceException(errorMsg);
        }
    }

    @Override
    public void validateForbiddenWordAndReturn(String text, String fieldName) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        String s = containsForbiddenWordAndReturn(text);
        if (StringUtils.isNotBlank(s)) {
            String errorMsg = StringUtils.isNotBlank(fieldName) ? String.format("%s 包含违禁词[%s]", fieldName, s) : "输入内容包含违禁词，请修改后重试";
            throw new ServiceException(errorMsg);
        }
    }

    @Override
    public void validateForbiddenWord(String text) {
        validateForbiddenWord(text, null);
    }

    @Override
    public void validateForbiddenWords(List<String> texts, String fieldName) {
        if (CollectionUtil.isEmpty(texts)) {
            return;
        }

        for (String text : texts) {
            if (StringUtils.isNotBlank(text)) {
                validateForbiddenWord(text, fieldName);
            }
        }
    }

    @Override
    public void validateForbiddenWords(List<String> texts) {
        validateForbiddenWords(texts, null);
    }
}

