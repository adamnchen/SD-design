package com.sutran.sd.draw.domain.dto.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-04-17
 */
@Data
public class SdTrainTaskDto implements Serializable {

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页数
     */
    private Integer pageNum;

    /**
     * 排序列
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc;

    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    private Integer newStatus;
}
