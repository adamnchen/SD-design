package com.sutran.sd.system.api;

import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Query;

import java.util.Map;

/**
 * @author zj
 * @date 2024-04-07
 */
public interface TranslateApi {

    @Get(value = "https://openapi.youdao.com/api")
    JSONObject youdaoTranslate(@Query Map<String,String> params);

    @Get(value = "https://fanyi-api.baidu.com/api/trans/vip/translate")
    JSONObject baiduTranslate(@Query Map<String,String> params);

}
