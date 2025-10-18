package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 图片处理
 * @author zj
 * @date 2025年10月09日 20:43
 */
@Data
@Accessors(chain = true)
public class FluxgymImgVo {
    /**
     * lora名称
     */
    private String loraName;
    /**
     * 图片描述词
     */
    private List<String> captions;
    /**
     * 图片字节集合
     */
    private List<byte[]> imageBytes;
     /**
      * 图片名称集合
      */
    private List<String> imageNames;
}
