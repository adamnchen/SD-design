package com.sutran.sd.sdapi.modules.comfyui.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ComfyUI图片对象
 * @author zj
 * @date 2025-03-24
 */
@Data
@Accessors(chain = true)
public class ComfyTaskImage {
    /**
     * 图片名
     */
    @JsonProperty("filename")
    private String fileName;

    /**
     * 图片存放位置的文件夹
     */
    @JsonProperty("subfolder")
    private String subFolder;

    /**
     * 图片存放位置的文件夹
     */
    @JsonProperty("type")
    private String folder;

    /**
     * 图片预览格式 例：WEBG
     */
    private String preview;

    /**
     * 应该是图片颜色通道 例：rgb
     */
    private String channel;
}
