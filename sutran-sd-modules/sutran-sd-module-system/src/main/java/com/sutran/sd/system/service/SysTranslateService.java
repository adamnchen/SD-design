package com.sutran.sd.system.service;

import com.sutran.sd.common.enums.TranslateType;

import java.security.NoSuchAlgorithmException;

/**
 * @author zj
 * @date 2024-04-07
 */
public interface SysTranslateService {
    String zhToEn(String content, TranslateType type) throws NoSuchAlgorithmException;
    String enToZh(String content, TranslateType type) throws NoSuchAlgorithmException;
}
