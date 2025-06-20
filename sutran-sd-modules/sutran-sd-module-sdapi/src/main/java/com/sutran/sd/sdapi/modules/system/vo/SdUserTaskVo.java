package com.sutran.sd.sdapi.modules.system.vo;

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
     * 数据ID
     */
    @Schema(name = "id", description = "数据ID")
    private String id;
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
     * 任务分类[0-文生图,1-图生图]
     */
    @Schema(name = "category", description = "任务分类[0-文生图,1-图生图]")
    private Integer category;
    /**
     * 是否局部重绘
     */
    @Schema(name = "isRedraw", description = "isRedraw")
    private Integer isRedraw;
    /**
     * 执行时长(ms)
     */
    @Schema(name = "consumeTime", description = "执行时长(ms)")
    private Long consumeTime;
    /**
     * 排队时长(ms)
     */
    @Schema(name = "queueTime", description = "排队时长(ms)")
    private Long queueTime;
    /**
     * 失败原因
     */
    @Schema(name = "reason", description = "失败原因")
    private String reason;
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
     * 任务更新时间
     */
    @Schema(name = "updTime", description = "任务更新时间")
    private Date updTime;

    private Object loraInfo;

}

