package com.sutran.sd.common.utils.translate;

import cn.hutool.crypto.digest.MD5;

import java.util.Map;

/**
 * @author zj
 * @date 2024-07-16
 */
public class BaiduAuthUtil {

    public static void buildParams(String appKey, String appSecret, Map<String, String> params) {
        params.put("appid", appKey);
        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        // 签名
        String src = appKey + params.get("q") + salt + appSecret;
        // 加密前的原文
        MD5 md5 = new MD5();
        params.put("sign",md5.digestHex(src));
    }

}
