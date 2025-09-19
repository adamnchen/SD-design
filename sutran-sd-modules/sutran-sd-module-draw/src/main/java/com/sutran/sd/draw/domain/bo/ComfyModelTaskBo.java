package com.sutran.sd.draw.domain.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * [ComfyUI]模型生图任务提交参数
 * @author zj
 * @date 2025年09月12日 23:34
 */
@Data
@Accessors(chain=true)
public class ComfyModelTaskBo {
    /**
     * 模型id[必填]
     */
    @NotBlank(message = "模型id不能为空")
    private String modelId;
    /**
     * 提示词(英文)[必填]
     */
    @NotBlank(message = "提示词(英文)不能为空")
    private String prompt;
    /**
     * 提示词中文[必填]
     */
    @NotBlank(message = "提示词中文不能为空")
    private String promptZh;
    /**
     * 模型强度[默认0.8]
     */
    private String modelStrength="0.8";
    /**
     * 生成图片数量
     */
    private String batchSize="1";
}
