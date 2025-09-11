package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 基础大模型
 * @author zj
 * @date 2024-03-07
 */
@Data
@Accessors(chain = true)
public class CheckPointVo implements Serializable {

    /**
     * 模型全称
     */
    private String title;
    /**
     * 模型名称
     */
    @JsonProperty(value = "model_name")
    private String modelName;
    /**
     * 模型hash值
     */
    private String hash;
    /**
     * 模型加密值
     */
    private String sha256;
    /**
     * 模型存储位置
     */
    private String filename;
    /**
     * 模型配置参数
     */
    private Object config;
    /**
     * 模型当前使用状态[0-未使用,1-使用中]
     */
    private Integer useStatus;

}
