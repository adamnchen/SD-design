package com.sutran.sd.draw.domain.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zj
 * @date 2025年09月09日 21:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComfyTaskQueueRemaining {

    @JsonProperty("exec_info")
    private ExecInfo execInfo;

    @Data
    static class ExecInfo {
        @JsonProperty("queue_remaining")
        private int queueRemaining;
    }

}
