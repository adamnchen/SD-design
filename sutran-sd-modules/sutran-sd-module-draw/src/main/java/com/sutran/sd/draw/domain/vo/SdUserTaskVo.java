package com.sutran.sd.draw.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * SD绘图 || 用户任务执行记录(SdUserTask)实体类
 *
 * @author zj
 * @since 2024-03-03
 */
@Data
@Accessors(chain = true)
public class SdUserTaskVo implements Serializable {
    /**
     * 任务ID
     */
    @Schema(name = "taskId", description = "任务ID")
    private String taskId;
    /**
     * 任务第一张图地址
     */
    @Schema(name = "firstImgUrl", description = "第一张图地址")
    private String firstImgUrl;
    /**
     * 执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]
     */
    @Schema(name = "status", description = "执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]")
    private Integer status;
    /**
     * 分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]
     */
    @Schema(name = "category", description = "分类[0-SD文生图,1-SD图生图,2-测试,3-Comfy生图]")
    private Integer category;
    /**
     * comfy内部任务ID
     */
    @Schema(name = "promptId", description = "comfy内部任务ID")
    private String promptId;
    /**
     * 英文提示词
     */
    @Schema(name = "prompt", description = "英文提示词")
    private String prompt;
    /**
     * 中文提示词
     */
    @Schema(name = "promptZh", description = "中文提示词")
    private String promptZh;
    /**
     * comfy任务执行的节点ID
     */
    @Schema(name = "nodeId", description = "comfy任务执行的节点ID")
    private Long nodeId;
    /**
     * comfy任务执行的节点URL
     */
    @Schema(name = "nodeUrl", description = "comfy任务执行的节点URL")
    private String nodeUrl;
    /**
     * 参考图片地址数组
     */
    @Schema(name = "initImgList", description = "参考图片地址数组")
    private String initImgList;
    /**
     * 任务归属人ID
     */
    @Schema(name = "belongUserId", description = "任务归属人ID")
    private String belongUserId;
    /**
     * 任务归属人
     */
    @Schema(name = "belongUserName", description = "任务归属人")
    private String belongUserName;
    /**
     * 任务创建时间
     */
    @Schema(name = "crtTime", description = "任务创建时间")
    private Date crtTime;
    /**
     * 任务开始时间
     */
    @Schema(name = "startTime", description = "任务开始时间")
    private Date startTime;
    /**
     * 任务结束时间
     */
    @Schema(name = "endTime", description = "任务结束时间")
    private Date endTime;
    /**
     * 失败原因
     */
    @Schema(name = "reason", description = "失败原因")
    private String reason;
    /**
     * 是否局部重绘
     */
    @Schema(name = "isRedraw", description = "isRedraw")
    private Integer isRedraw;
    /**
     * 任务更新时间
     */
    @Schema(name = "updTime", description = "任务更新时间")
    private Date updTime;
    /**
     * 使用的LORA信息
     */
    @Schema(name = "loraInfo", description = "使用的LORA信息")
    private Object loraInfo;

}

