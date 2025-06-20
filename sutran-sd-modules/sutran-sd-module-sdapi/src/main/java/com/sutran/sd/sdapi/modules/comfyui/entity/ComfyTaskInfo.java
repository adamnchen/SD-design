package com.sutran.sd.sdapi.modules.comfyui.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025-03-29
 */
@Data
@Accessors(chain = true)
public class ComfyTaskInfo implements Serializable {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("prompt")
    private ComfyWorkFlow flow;
}
