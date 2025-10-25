package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Fluxgym模型列表vo
 * @author zj
 * @date 2025年10月25日 15:48
 */
@Data
@Accessors(chain=true)
public class FluxgymModelPreviewVo {
    /**
     * 原图地址
     */
    private String originalImgUrl;
     /**
     * 提示词
     */
    private String prompt;
    /**
     * 预览图地址列表
     */
    private List<String> previewImgUrlList;
}
