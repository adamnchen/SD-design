package com.sutran.sd.draw.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 训练图片描述词
 * @author zj
 * @date 2025年10月31日 21:12
 */
@Data
@Accessors(chain=true)
public class TrainCaptionBo {
    /**
     * 英文描述
     */
    private String caption;
    /**
     * 中文描述
     */
    private String captionZh;
}
