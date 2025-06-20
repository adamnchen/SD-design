package com.sutran.sd.sdapi.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
* SD绘图 || 用户生图文件数据记录
* @author zj
* @TableName sd_user_model_file
*/
@Data
@Accessors(chain = true)
public class SdUserModelFileVo implements Serializable {

    /**
    * 数据ID
    */
    @Schema(description="数据ID")
    private String id;
    /**
    * 任务ID
    */
    @Schema(description="任务ID")
    private String taskId;
    /**
     * 分类[0-文生图，1-图生图]
     */
    @Schema(description="分类[0-文生图，1-图生图]")
    private Integer category;
    /**
     * 是否局部重绘
     */
    @Schema(name = "isRedraw", description = "isRedraw")
    private Integer isRedraw;

    @Schema(name = "summonWord", description = "召唤词")
    private String summonWord;
    @Schema(name = "prompt", description = "提示词(译文)")
    private String prompt;
    @Schema(name = "promptZh", description = "提示词(中文)")
    private String promptZh;
    @Schema(name = "promptDesc", description = "提示词(译文)")
    private String promptDesc;

    @Schema(name = "negativePrompt", description = "反向提示词(译文)")
    private String negativePrompt;
    @Schema(name = "negative_prompt_zh", description = "反向提示词(原文)")
    private String negativePromptZh;
    /**
     * 未翻译的提示词
     */
    @Schema(description="未翻译的提示词")
    private String modelStrength;
    /**
     * 参考图片
     */
    @Schema(description="参考图片")
    private String initImg;
    /**
    * 文件地址
    */
    @Schema(description="文件地址")
    private String fileUrl;
    /**
    * 文件信息
    */
    @Schema(description="文件信息")
    private Object fileInfo;
    /**
     * 文件参数
     */
    @Schema(description="文件参数")
    private Object fileParameters;
    /**
    * 基础大模型名称
    */
    @Schema(description="基础大模型名称")
    private String modelName;
    /**
    * lora模型ID
    */
    @Schema(description="lora模型ID")
    private String loraModelId;
    /**
     * lora模型ID
     */
    @Schema(description="lora模型图地址")
    private String loraModelUrl;
    /**
    * lora模型名称
    */
    @Schema(description="lora模型名称")
    private String loraTitle;
    /**
     * lora模型名称(中文)
     */
    @Schema(description="lora模型名称(中文)")
    private String loraTitleZh;
    /**
     * lora模型信息集合
     */
    @Schema(description="lora模型信息集合")
    private Object loraInfo;
    /**
    * 文件归属人ID
    */
    @Schema(description="文件归属人ID")
    private String belongUserId;
    /**
    * 文件归属人名称
    */
    @Schema(description="文件归属人名称")
    private String belongUserName;
    /**
    * 创建时间
    */
    @Schema(description="创建时间")
    private Date crtTime;
}
