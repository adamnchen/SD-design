package com.sutran.sd.draw.domain.dto.task;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 绘图数据分页查询
 * @author zj
 * @date 2024-03-08
 */
@Data
@Accessors(chain = true)
public class SdUserTaskPageDto implements Serializable {

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
     * 分类[0-文生图，1-图生图]
     */
    private Integer category;

    /**
     * 任务状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]
     */
    private Integer status;
}
