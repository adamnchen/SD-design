package com.sutran.sd.design.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户生图文件VO
 *
 * @author sutran
 * @date 2025-10-11
 */
@Data
public class UserModelFileVO {

    /**
     * 数据ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 分类[0-文生图，1-图生图]
     */
    private Integer category;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 是否局部重绘[0-否,1-是]
     */
    private Integer isRedraw;

    /**
     * 是否局部重绘名称
     */
    private String isRedrawName;

    /**
     * 描述词(译文)
     */
    private String prompt;

    /**
     * 描述词(原文)
     */
    private String promptZh;

    /**
     * 弃用词
     */
    private String promptDesc;

    /**
     * 反向提示词(译文)
     */
    private String negativePrompt;

    /**
     * 反向提示词(原文)
     */
    private String negativePromptZh;

    /**
     * 模型强度
     */
    private String modelStrength;

    /**
     * 参考图片
     */
    private String initImg;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件信息
     */
    private String fileInfo;

    /**
     * 文件参数信息
     */
    private String fileParameters;

    /**
     * 基础大模型名称
     */
    private String modelName;

    /**
     * lora模型ID
     */
    private Long loraModelId;

    /**
     * lora模型名称
     */
    private String loraTitle;

    /**
     * lora模型名称(中文)
     */
    private String loraTitleZh;

    /**
     * lora模型信息数组
     */
    private String loraInfo;

    /**
     * 文件归属人ID
     */
    private Long belongUserId;

    /**
     * 文件归属人名称
     */
    private String belongUserName;

    /**
     * 创建时间
     */
    private Date crtTime;

    /**
     * 当前用户是否已收藏该作品[true-已收藏,false-未收藏]
     */
    private Boolean isFavorite;

    /**
     * 是否公开[0-不公开,1-公开]
     */
    private Integer isPublic;

    /**
     * 获取分类名称
     */
    public String getCategoryName() {
        if (category == null) {
            return "未知";
        }
        switch (category) {
            case 0:
                return "文生图";
            case 1:
                return "图生图";
            default:
                return "未知";
        }
    }

    /**
     * 获取是否局部重绘名称
     */
    public String getIsRedrawName() {
        if (isRedraw == null) {
            return "否";
        }
        return isRedraw == 1 ? "是" : "否";
    }
}
