package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年10月09日 20:43
 */
@Data
@Accessors(chain = true)
public class FluxgymTainTaskVo {
    /**
     * 图片描述词
     */
    private String caption;
    /**
     * 图片url
     */
    @JsonProperty("image_url")
    private String imageUrl;
     /**
      * 任务id
      */
    private String taskId;
}
