package com.sutran.sd.common.core.domain.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-04-11
 */
@Data
public class WxMsgDto implements Serializable {
    private String openId;
    private String templateId;
    private String url;
    private JSONObject param;
}
