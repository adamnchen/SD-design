package com.sutran.sd.system.domain.vo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zj
 * @date 2025年10月16日 22:13
 */
@Data
@Accessors(chain=true)
public class BaiduTranslationResultVo {
    @JsonProperty("trans_result")
    private List<TransResult> transResult;
    @JsonProperty("from")
    private String from;
    @JsonProperty("to")
    private String to;
    @JsonProperty("error_code")
    private String errorCode;
    @JsonProperty("error_msg")
    private String errorMsg;
    private JSONObject data;

    @Data
    @Accessors(chain=true)
    public static class TransResult {
        @JsonProperty("src")
        private String src;
        @JsonProperty("dst")
        private String dst;
    }
}
