package com.sutran.sd.sdapi.domain.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 绘图任务进程
 * @author zj
 * @date 2024-03-08
 */
@Data
@Accessors(chain = true)
public class SdInternalProgressDto implements Serializable {

    /**
     * 任务ID
     */
    @JsonProperty("id_task")
    private String idTask;
    /**
     *
     */
    @JsonProperty("id_live_preview")
    private int idLivePreview=2;
    /**
     *
     */
    @JsonProperty("live_preview")
    private Boolean livePreview;

}
