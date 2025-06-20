package com.sutran.sd.sdapi.domain.dto.train;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-03-30
 */
@Data
public class SdTrainTagDto implements Serializable {
    private String tagZh;
    private String tag;
}
