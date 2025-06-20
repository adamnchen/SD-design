package com.sutran.sd.sdapi.modules.system.entity;

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
    private Object preParams;
    /**
     * 预处理参数
     */
    @Schema(name = "trainParams", description = "训练参数")
    private Object trainParams;
    /**
     * 训练模型名称
     */
    @Schema(name = "modelName", description = "训练模型名称")
    private String modelName;
    /**
     * 共性词数组
     */
    @Schema(name = "additionTag", description = "共性词数组")
    private Object additionTag;
    /**
     * 预处理图片状态[0-未训练,1-已训练]
     */
    @Schema(name = "status", description = "预处理图片状态[0-未训练,1-已预处理,2-已训练,3-训练失败]")
    private Integer status;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-预处理完成,3-训练队列中,4-训练中,5-训练完成或失败]
     */
    @Schema(name = "newStatus", description = "任务状态[0-预处理队列中,1-预处理中,2-预处理完成,3-训练队列中,4-训练中,5-训练完成或失败]")
    private Integer newStatus;
    /**
     * 预处理图片数量
     */
    @Schema(name = "imgNum", description = "预处理图片数量")
    private Integer imgNum;
    /**
     * 训练任务ID
     */
    @Schema(name = "taskId", description = "训练任务ID")
    private String taskId;
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

    @Schema(name = "startTime", description = "开始时间")
    private Date startTime;
    @Schema(name = "endTime", description = "结束时间")
    private Date endTime;

}

