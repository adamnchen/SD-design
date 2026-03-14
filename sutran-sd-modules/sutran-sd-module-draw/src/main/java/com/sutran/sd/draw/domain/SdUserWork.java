package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
* SD绘图 || 用户生图作品
* @author zj
*/
@Data
@TableName("sd_user_work")
@Accessors(chain = true)
public class SdUserWork implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @NotNull(message="[数据ID]不能为空")
    @Schema(description="数据ID")
    private Long id;
    /**
    * 任务ID
    */
    @Schema(description="任务ID")
    private Long taskId;
    /**
     * 分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]
     */
    @Schema(description="分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]")
    private Integer category;
    /**
     * 是否局部重绘
     */
    @Schema(name = "isRedraw", description = "isRedraw")
    private Integer isRedraw;

    @Schema(name = "prompt", description = "提示词(译文)")
    private String prompt;
    @Schema(name = "promptZh", description = "提示词(中文)")
    private String promptZh;
    @Schema(name = "promptDesc", description = "提示词(中文)")
    private String promptDesc;

    @Schema(name = "negativePrompt", description = "反向提示词(译文)")
    private String negativePrompt;
    @Schema(name = "negative_prompt_zh", description = "反向提示词(原文)")
    private String negativePromptZh;

    /**
     * 未翻译的提示词
     */
    @Schema(description="模型强度")
    private String modelStrength;
    /**
     * 参考图片
     */
    @Schema(description="参考图片")
    private String initImg;
    /**
    * 文件地址
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description="文件地址")
    @Length(max= 255,message="编码长度不能超过255")
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
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description="基础大模型名称")
    @Length(max= 64,message="编码长度不能超过64")
    private String modelName;
    /**
    * lora模型ID
    */
    @Schema(description="lora模型ID")
    private Long loraModelId;
    /**
    * lora模型名称
    */
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description="lora模型名称")
    @Length(max= 64,message="编码长度不能超过64")
    private String loraTitle;
    /**
     * lora模型名称
     */
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description="lora模型名称")
    @Length(max= 64,message="编码长度不能超过64")
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
    private Long belongUserId;
    /**
    * 文件归属人名称
    */
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description="文件归属人名称")
    @Length(max= 64,message="编码长度不能超过64")
    private String belongUserName;
    /**
    * 创建时间
    */
    @Schema(description="创建时间")
    private Date crtTime;

    /**
     * 是否公开[0-否,1-是]
     */
    private Integer isPublic;
}
