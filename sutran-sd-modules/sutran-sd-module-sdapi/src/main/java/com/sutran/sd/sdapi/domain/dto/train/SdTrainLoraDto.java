package com.sutran.sd.sdapi.domain.dto.train;

import lombok.Data;

import java.util.Map;

/**
 * @author zj
 * @date 2024-03-27
 */
@Data
public class SdTrainLoraDto {

    private String modelName;
    private String preTaskId;
    private Map<String,Object> extParam;

}
