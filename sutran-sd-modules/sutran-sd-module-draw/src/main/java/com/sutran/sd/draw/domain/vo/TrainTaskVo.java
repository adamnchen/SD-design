package com.sutran.sd.draw.domain.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 训练任务VO
 * @author zj
 * @date 2025-05-25
 */
@Data
@Accessors(chain = true)
public class TrainTaskVo implements Serializable {
    /**
     * 训练任务id
     */
    private String id;
    /**
     * 预处理任务id
     */
    private String preTaskId;
    /**
     * 预处理任务提交参数
     */
    @Schema(name = "preTaskParams", description = "预处理任务提交参数")
    private JSONObject preTaskParams = new JSONObject();
    /**
     * 训练图片数量
     */
    @Schema(name = "imgNum", description = "训练图片数量")
    private Integer imgNum;
    /**
     * 预处理提交时间
     */
    @Schema(name = "preSubmitTime", description = "预处理提交时间")
    private Date preSubmitTime;
    /**
     * 预处理开始时间
     */
    @Schema(name = "preStartTime", description = "预处理开始时间")
    private Date preStartTime;
    /**
     * 预处理结束时间
     */
    @Schema(name = "preEndTime", description = "预处理结束时间")
    private Date preEndTime;
    /**
     * 预处理失败原因
     */
    @Schema(name = "preReason", description = "预处理失败原因")
    private String preReason;


    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    @Schema(name = "newStatus", description = "任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]")
    private Integer newStatus;


    /**
     * 训练任务id
     */
    @Schema(name = "taskId", description = "训练任务id")
    private String taskId;
    /**
     * 模型名称
     */
    @Schema(name = "modelName", description = "模型名称")
    private String modelName;
    /**
     * 共性词组
     */
    @Schema(name = "additionTag", description = "共性词组")
    private List<String> additionTag = new ArrayList<>();
    /**
     * 训练使用的GPU信息
     */
    @Schema(name = "gpuPool", description = "训练使用的GPU信息")
    private JSONObject gpuPool = new JSONObject();
    /**
     * 训练失败原因
     */
    @Schema(name = "reason", description = "训练失败原因")
    private String reason;
    /**
     * 训练开始时间
     */
    @Schema(name = "startTime", description = "训练开始时间")
    private Date startTime;
    /**
     * 训练结束时间
     */
    @Schema(name = "endTime", description = "训练结束时间")
    private Date endTime;
    /**
     * 训练任务提交参数
     */
    @Schema(name = "trainTaskParams", description = "训练任务提交参数")
    private JSONObject trainTaskParams = new JSONObject();


    /**
     * 任务创建时间
     */
    @Schema(name = "crtTime", description = "任务创建时间")
    private Date crtTime;
    /**
     * 任务创建人
     */
    @Schema(name = "crtUserName", description = "任务创建人")
    private String crtUserName;
    /**
     * 任务创建人昵称
     */
    @Schema(name = "nickName", description = "任务创建人昵称")
    private String nickName;
    /**
     * 任务创建人id
     */
    @Schema(name = "crtUserId", description = "任务创建人id")
    private String crtUserId;

    /**
     * 原图地址
     */
    @Schema(name = "originalImgUrl", description = "原图地址")
    private String originalImgUrl;
}
