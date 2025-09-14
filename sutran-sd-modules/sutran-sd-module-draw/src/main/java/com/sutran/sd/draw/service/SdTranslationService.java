package com.sutran.sd.draw.service;

/**
 * @author zj
 * @date 2025年09月13日 23:19
 */
public interface SdTranslationService {
    /**
     * 从redis同步翻译字典到数据库
     * @param type 类型[0-提示词英译中,1-tag标签中译英]
     */
    void syncTranslationFromRedisToDb(Integer type);

    /**
     * 从数据库同步翻译字典到redis
     * @param type 类型[0-提示词英译中,1-tag标签中译英]
     */
    void syncTranslationFromDbToRedis(Integer type);
}
