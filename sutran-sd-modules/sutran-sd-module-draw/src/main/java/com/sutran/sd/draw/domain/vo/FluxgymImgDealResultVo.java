package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 图片处理
 * @author zj
 * @date 2025年10月09日 20:43
 */
@Data
@Accessors(chain = true)
public class FluxgymImgDealResultVo {

    private Boolean success;
    private List<ImageInfoVo> results;
    private String taskId;

    @Data
    @Accessors(chain = true)
    public static class ImageInfoVo {
        /**
         * 图片描述词
         */
        @JsonProperty("caption")
        private String caption;
        /**
         * 图片描述词（中文）
         */
        private String captionZh;
        /**
         * 图片名称
         */
        @JsonProperty("image_name")
        private String imageName;
    }
}
