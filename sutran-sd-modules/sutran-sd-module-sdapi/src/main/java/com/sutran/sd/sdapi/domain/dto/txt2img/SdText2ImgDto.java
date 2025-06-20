package com.sutran.sd.sdapi.domain.dto.txt2img;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文生图参数请求实体
 * @author zj
 * @date 2024-03-03
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain = true)
@Schema(name = "SdText2ImgDto", description = "SD-API文生图")
public class SdText2ImgDto implements Serializable {

    @Schema(name = "modelId", description = "Lora模型ID(废弃)")
    private String modelId;
    @Schema(name = "modelStrength", description = "Lora模型强度(废弃)")
    private String modelStrength;

    @Schema(name = "modelInfos", description = "Lora多模型集合")
    private List<SdApiModelParamDto> modelInfos;

    @Schema(name = "summonWord", description = "召唤词")
    private String summonWord;
    @Schema(name = "prompt", description = "提示词(译文)")
    private String prompt;
    @Schema(name = "promptZh", description = "提示词(中文)")
    private String promptZh;
    @Schema(name = "negative_prompt", description = "反向提示词(译文)")
    private String negative_prompt;
    @Schema(name = "negativePromptZh", description = "反向提示词(原文)")
    private String negativePromptZh;

    @Schema(name = "steps", description = "采样迭代步数(默认20)")
    private Integer steps;
    @Schema(name = "sampler_name", description = "取样器(默认[DPM++ 2M Karras])")
    private String sampler_name;
    @Schema(name = "width", description = "图片宽度(默认512)")
    private Integer width;
    @Schema(name = "height", description = "图片高度(默认512)")
    private Integer height;
    @Schema(name = "batch_size", description = "每次生成的张数(默认1)")
    private Integer batch_size;
    @Schema(name = "n_iter", description = "生成批次(默认1)")
    private Integer n_iter;
    @Schema(name = "seed", description = "随机数种子(默认-1)")
    private Long seed;
    @Schema(name = "restore_faces", description = "面部修复(默认false)")
    private Boolean restore_faces;
    @Schema(name = "CLIP_stop_at_last_layers", description = "CLIP跳过层数(默认1)")
    private Integer CLIP_stop_at_last_layers;
    @Schema(name = "cfg_scale", description = "提示词相关性 越大越接近提示词")
    private Integer cfg_scale;
    @Schema(name = "sd_vae", description = "vae模型名称")
    private String sd_vae;
    /**
     * "enabled": True,  # 启用
     * "control_mode": 0,  # 对应webui 的 Control Mode 可以直接填字符串 推荐使用下标 0 1 2
     * "model": "t2i-adapter_diffusers_xl_lineart [bae0efef]",  # 对应webui 的 Model
     * "module": "lineart_standard (from white bg & black line)",  # 对应webui 的 Preprocessor
     * "weight": 0.45,  # 对应webui 的Control Weight
     * "resize_mode": "Crop and Resize",
     * "threshold_a": 200,  # 阈值a 部分control module会用上
     * "threshold_b": 245,  # 阈值b
     * "guidance_start": 0,  # 什么时候介入 对应webui 的 Starting Control Step
     * "guidance_end": 0.7,  # 什么时候退出 对应webui 的 Ending Control Step
     * "pixel_perfect": True,  # 像素完美
     * "processor_res": 512,  # 预处理器分辨率
     * "save_detected_map": False,  # 因为使用了 controlnet API会返回生成controlnet的效果图，默认是True，如何不需要，改成False
     * "input_image": "",  # 图片 格式为base64(如果是蒙版图，则使用蒙版图)
     * "mask": ""  # 蒙版 格式为base64(部分模型需要)
     */
    @Schema(name = "controlNetArgs", description = "ControlNet参数")
    private List<Map<String, Object>> controlNetArgs;
    /**
     * True,  # 是否开启
     * "sd_xl_refiner_1.0",  # 大模型昵称
     * 0.75,  # 介入时机
     */
    @Schema(name = "refinerArgs", description = "Refiner参数")
    private List<Map<String, Object>> refinerArgs;

    @Schema(name = "scriptArgs", description = "脚本参数")
    private List<Object> script_args=new ArrayList<>();
    @Schema(name = "script_name", description = "脚本名称")
    private String script_name;
}
