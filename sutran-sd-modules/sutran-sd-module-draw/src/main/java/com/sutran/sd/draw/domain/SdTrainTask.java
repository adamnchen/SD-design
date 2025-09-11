package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zj
 * @since 2024-03-03
 */
@Data
@TableName("sd_train_task")
@Accessors(chain = true)
public class SdTrainTask implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 预处理参数
     */
    @Schema(name = "preParams", description = "预处理参数")
    private String preParams;
    /**
     * 预处理图片数量
     */
    @Schema(name = "imgNum", description = "预处理图片数量")
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
     * 预处理图片状态[0-未训练,1-已训练]
     */
    @Schema(name = "status", description = "预处理图片状态[0-未训练,1-已预处理,2-已训练,3-训练失败]")
    private Integer status;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-预处理完成,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    @Schema(name = "newStatus", description = "任务状态[0-预处理队列中,1-预处理中,2-预处理完成,3-训练队列中,4-训练中,5-训练完成,6-训练失败]")
    private Integer newStatus;


    /**
     * 训练任务ID
     */
    @Schema(name = "taskId", description = "训练任务ID")
    private String taskId;
    /**
     * 训练任务模型名称
     */
    @Schema(name = "modelName", description = "训练任务模型名称")
    private String modelName;
    /**
     * 训练任务共性词数组
     */
    @Schema(name = "additionTag", description = "训练任务共性词数组")
    private String additionTag;
    /**
     * 训练任务参数
     */
    @Schema(name = "trainParams", description = "训练任务参数")
    private String trainParams;
    /**
     * 训练任务提交时间
     */
    @Schema(name = "submitTime", description = "训练任务提交时间")
    private Date submitTime;
    /**
     * 训练任务开始时间
     */
    @Schema(name = "startTime", description = "训练任务开始时间")
    private Date startTime;
    /**
     * 训练任务结束时间
     */
    @Schema(name = "endTime", description = "训练任务结束时间")
    private Date endTime;
    /**
     * 训练任务失败原因
     */
    @Schema(name = "reason", description = "训练任务失败原因")
    private String reason;


    /**
     * 创建人ID
     */
    @Schema(name = "crtUserId", description = "创建人ID")
    private Long crtUserId;
    /**
     * 创建人名称
     */
    @Schema(name = "crtUserName", description = "创建人名称")
    private String crtUserName;
    /**
     * 任务创建时间
     */
    @Schema(name = "crtTime", description = "任务创建时间")
    private Date crtTime;
}

