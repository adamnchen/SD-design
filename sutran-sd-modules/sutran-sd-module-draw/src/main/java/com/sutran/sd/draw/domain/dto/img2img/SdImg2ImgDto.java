package com.sutran.sd.draw.domain.dto.img2img;

import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 文生图参数请求实体
 * @author zj
 * @date 2024-03-03
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain = true)
@Schema(name = "SdImg2ImgDto", description = "SD-API图生图")
public class SdImg2ImgDto extends SdText2ImgDto {
    @Schema(name = "initImage", description = "参考图片(base64)")
    private String initImage;
    @Schema(name = "resize_mode", description = "图片缩放")
    private Integer resize_mode;

    @Schema(name = "mask", description = "base64蒙版图数据（纯白区域为需重绘部分）")
    private String mask;
    @Schema(name = "denoising_strength", description = "降噪强度（0-1，值越高修改幅度越大）")
    private Double denoising_strength;
    @Schema(name = "inpainting_fill", description = "蒙版填充模式：0=填充；1=原图；2=潜空间噪声；3=空白潜空间")
    private Integer inpainting_fill;
    @Schema(name = "inpaint_full_res", description = "是否仅重绘蒙版区域（true时需指定`inpaint_full_res_padding`）")
    private Boolean inpaint_full_res;
    @Schema(name = "inpaint_full_res_padding", description = "蒙版边缘扩展像素（平滑过渡）")
    private Integer inpaint_full_res_padding;
}
