package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zj
 * @date 2025年10月16日 10:33
 */
@Data
@Accessors(chain=true)
public class FluxgymTrainResultVo {
    private Boolean success;
    private String taskId;
    @JsonProperty("output_dir")
    private String outputDir;
    @JsonProperty("dataset_dir")
    private String datasetDir;
    @JsonProperty("sh_path")
    private String shPath;
    private String message;
    private String detail;

}
