package com.sutran.sd.sdapi.domain.dto.train;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2024-03-27
 */
@Data
@Accessors(chain = true)
public class SdTrainTagDelDto {

    private String preTaskId;
    private String imgUrl;
    private String tag;

}
