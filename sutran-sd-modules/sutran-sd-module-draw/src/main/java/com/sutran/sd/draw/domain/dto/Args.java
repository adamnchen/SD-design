package com.sutran.sd.draw.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-02-27
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class Args implements Serializable {

    private String batch_images;
    private String control_mode;
    private Boolean enabled;
    private Integer guidance_start;
    private Integer guidance_end;
    private Integer input_mode;
    /**
     * PreProcessor 例如："module": "lineart_coarse"
     */
    private String module;
    private String model;

    /**
     * defaults to 1
     */
    private Integer weight;
    private String input_image;
    private String mask;

    private int resize_mode = 0;

    /**
     * enable pixel-perfect preprocessor. defaults to false
     */
    private Boolean pixel_perfect;

    /**
     * whether to compensate low GPU memory with processing time. defaults to false
     */
    private boolean low_vram;
    private int processor_res;
    private int threshold_a;
    private int threshold_b;

}
