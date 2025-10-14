package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * comfyui工具修复实体类
 * @author zj
 * @date 2025年10月11日 10:47
 */
@Data
@Accessors(chain=true)
public class ComfyuiImageToolVo implements Serializable {
    /**
     * 工作流Id
     */
    private String flowId;
    /**
     * 工具名称
     */
    private String toolName;
    /**
     * 归属模型类型
     */
    private String modelType;
    /**
     * 初始化prompt
     */
    private String initPrompt;
    /**
     * 初始化prompt中文
     */
    private String initPromptZh;
    /**
     * 需要扣除的生图数量
     */
    private Integer drawNum;
}
