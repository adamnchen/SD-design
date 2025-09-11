package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 训练任务API响应状态
 * @author zj
 * @date 2025-04-21
 */
@Data
@Accessors(chain = true)
public class TrainProcessTaskVo implements Serializable {
    /**
     * 响应值状态
     */
    private String status;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private TrainProcessDataVo data;
}
