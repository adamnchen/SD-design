package com.sutran.sd.draw.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
* SD绘图 || 用户生图文件数据记录
* @author zj
*/
@Data
@Accessors(chain = true)
public class ComfyUserWorkVo implements Serializable {

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

    @Schema(name = "prompt", description = "提示词(原文)")
    private String prompt;
    @Schema(name = "promptZh", description = "提示词(中文)")
    private String promptZh;

    @Schema(name = "negativePrompt", description = "反向提示词(译文)")
    private String negativePrompt;
    @Schema(name = "negative_prompt_zh", description = "反向提示词(原文)")
    private String negativePromptZh;
    /**
     * 模型强度
     */
    @Schema(description="模型强度")
    private String modelStrength;
    /**
     * 参考图片集合
     */
    @Schema(description="参考图片集合")
    private Object initImgList;
    /**
    * 文件地址
    */
    @Schema(description="文件地址")
    private String fileUrl;
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
