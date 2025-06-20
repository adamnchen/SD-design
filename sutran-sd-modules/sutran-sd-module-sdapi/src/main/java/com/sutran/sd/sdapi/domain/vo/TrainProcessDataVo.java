package com.sutran.sd.sdapi.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 训练任务进度响应实体类
 * @author zj
 * @date 2025-04-21
 */
@Data
@Accessors(chain = true)
public class TrainProcessDataVo implements Serializable {
    /**
     * 任务ID
     */
    private String id;
    /**
     * 任务状态(CREATED-创建,RUNNING-运行,FINISHED-完成,TERMINATED-中断,FAILED-失败)
     */
    private String status;
    /**
     * 任务进度(100-完成)
     */
    private Integer process;
    /**
     * 任务原因
     */
    private String reason;
    /**
     * 剩余执行时间(秒)
     */
    private Integer remainingTime;
}
