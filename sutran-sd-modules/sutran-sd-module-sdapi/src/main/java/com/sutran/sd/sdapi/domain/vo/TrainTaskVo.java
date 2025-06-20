package com.sutran.sd.sdapi.domain.vo;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
     * 预处理任务id
     */
    private String preTaskId;
    /**
     * 训练任务id
     */
    private String taskId;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    private Integer newStatus;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 共性词组
     */
    private List<String> additionTag;
    /**
     * 训练图片数量
     */
    private Integer imgNum;
    /**
     * 训练使用的GPU信息
     */
    private JSONObject gpuPool;
    /**
     * 训练失败原因
     */
    private String reason;
    /**
     * 训练开始时间
     */
    private Date startTime;
    /**
     * 训练结束时间
     */
    private Date endTime;
    /**
     * 任务创建时间
     */
    private Date crtTime;
    /**
     * 任务创建人
     */
    private String crtUserName;
    /**
     * 任务创建人id
     */
    private String crtUserId;
    /**
     * 预处理任务提交参数
     */
    private JSONObject preTaskParams;
    /**
     * 训练任务提交参数
     */
    private JSONObject trainTaskParams;
}
