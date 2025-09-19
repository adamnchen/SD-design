package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年09月19日 22:06
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain=true)
public class SdWebuiProgressVo implements Serializable {
    @JsonProperty("active")
    private Boolean active;
    @JsonProperty("completed")
    private Boolean completed;
    @JsonProperty("eta")
    private Double eta;
    @JsonProperty("id_live_preview")
    private Integer id_live_preview;
    @JsonProperty("live_preview")
    private Boolean live_preview;
    @JsonProperty("progress")
    private Double progress;
    @JsonProperty("queued")
    private Boolean queued;
    @JsonProperty("textinfo")
    private String textinfo;
}
