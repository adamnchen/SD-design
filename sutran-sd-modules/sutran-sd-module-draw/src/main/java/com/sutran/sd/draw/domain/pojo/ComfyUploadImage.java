package com.sutran.sd.draw.domain.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ComfyUI图片对象
 * @author zj
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComfyUploadImage {
    /**
     * 图片名
     */
    @JsonProperty("name")
    private String name;

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
}
