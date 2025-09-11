package com.sutran.sd.draw.domain.dto.train;

import lombok.Data;

import java.util.List;

/**
 * @author zj
 * @date 2024-03-27
 */
@Data
public class SdTrainPreImgDto {

    private String preTaskId;
    private String imgUrl;
    private List<SdTrainTagDto> tags;

}
