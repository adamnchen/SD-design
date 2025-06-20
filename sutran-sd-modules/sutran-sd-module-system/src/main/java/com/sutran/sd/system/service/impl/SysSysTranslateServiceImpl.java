package com.sutran.sd.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.sutran.sd.common.enums.TranslateType;
import com.sutran.sd.common.utils.translate.AuthV3Util;
import com.sutran.sd.common.utils.translate.BaiduAuthUtil;
import com.sutran.sd.system.api.TranslateApi;
import com.sutran.sd.system.service.SysTranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zj
 * @date 2024-04-07
 */
@Slf4j
@Service("sysTranslateService")
public class SysSysTranslateServiceImpl implements SysTranslateService {

    @Resource
    private TranslateApi translateApi;
    @Value("${translator.youdao.appKey:}")
    private String youdaoAppKey;
    @Value("${translator.youdao.appSecret:}")
    private String youdaoAppSecret;
    @Value("${translator.youdao.vocabId:}")
    private String youdaoVocabId;
    @Value("${translator.baidu.appId:}")
    private String baiduAppKey;
    @Value("${translator.baidu.appSecret:}")
    private String baiduAppSecret;

    @Override
    public String zhToEn(String content, TranslateType type) throws NoSuchAlgorithmException {
        switch (type) {
            case YOUDAO:
                Map<String, String> ydParams = new HashMap<String, String>() {{
                    put("q", content);
                    put("from", "zh-CHS");
                    put("to", "en");
                    put("vocabId", youdaoVocabId);
                }};
                // 添加鉴权相关参数
                AuthV3Util.addAuthParams(youdaoAppKey, youdaoAppSecret, ydParams);
                JSONObject ydResult = translateApi.youdaoTranslate(ydParams);
                return CollectionUtil.isNotEmpty(ydResult) && "0".equals(ydResult.getString("errorCode"))?String.valueOf(ydResult.getJSONArray("translation").get(0)):null;
            case BAIDU:
                Map<String, String> bdParams = new HashMap<String, String>() {{
                    put("q", content);
                    put("from", "zh");
                    put("to", "en");
                }};
                // 添加鉴权相关参数
                BaiduAuthUtil.buildParams(baiduAppKey, baiduAppSecret, bdParams);
                JSONObject bdResult = translateApi.baiduTranslate(bdParams);
                log.info("[百度翻译][汉译英]>>>>>>>>>返回结果：{}",bdResult);
                return CollectionUtil.isNotEmpty(bdResult) && !"54001".equals(bdResult.getString("error_code"))? JSON.parseArray(bdResult.getString("trans_result"), JSONObject.class, JSONReader.Feature.SupportAutoType).get(0).getString("dst") :null;
            default:
                return null;
        }
    }

    @Override
    public String enToZh(String content, TranslateType type) throws NoSuchAlgorithmException {
        switch (type) {
            case YOUDAO:
                Map<String, String> ydParams = new HashMap<String, String>() {{
                    put("q", content);
                    put("from", "en");
                    put("to", "zh-CHS");
                    put("vocabId", youdaoVocabId);
                }};
                // 添加鉴权相关参数
                AuthV3Util.addAuthParams(youdaoAppKey, youdaoAppSecret, ydParams);
                JSONObject ydResult = translateApi.youdaoTranslate(ydParams);
                if(CollectionUtil.isNotEmpty(ydResult) && "0".equals(ydResult.getString("errorCode"))) {
                    String translation = String.valueOf(ydResult.getJSONArray("translation").get(0));
                    return StrUtil.isEmptyIfStr(translation)?null:translation.replace("。","");
                }
                return null;
            case BAIDU:
                Map<String, String> bdParams = new HashMap<String, String>() {{
                    put("q", content);
                    put("from", "en");
                    put("to", "zh");
                }};
                // 添加鉴权相关参数
                BaiduAuthUtil.buildParams(baiduAppKey, baiduAppSecret, bdParams);
                JSONObject bdResult = translateApi.baiduTranslate(bdParams);
                log.info("[百度翻译][英译汉]>>>>>>>>>返回结果：{}",bdResult);
                return CollectionUtil.isNotEmpty(bdResult) && !"54001".equals(bdResult.getString("error_code"))? JSON.parseArray(bdResult.getString("trans_result"), JSONObject.class, JSONReader.Feature.SupportAutoType).get(0).getString("dst") :null;
            default:
                return null;
        }
    }
}
