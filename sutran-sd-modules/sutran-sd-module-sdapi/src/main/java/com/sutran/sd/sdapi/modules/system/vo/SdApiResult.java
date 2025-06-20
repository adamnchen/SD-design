package com.sutran.sd.sdapi.modules.system.vo;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SD的restful api接口响应参数
 * @author zj
 * @date 2024-02-27
 */
@Data
public class SdApiResult implements Serializable {

    @Getter
    private final List<String> images = new ArrayList<>();
    public final Map<String, Object> parameters = new HashMap<>();
    public final Map<String, Object> info = new HashMap<>();

    public void addImage(String base64Img) {
        images.add(base64Img);
    }

}
