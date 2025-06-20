package com.sutran.sd.sdapi.domain.dto;

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
public class SdUserModelFilePageDto implements Serializable {

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
     * 任务ID
     */
    private String taskId;

    /**
     * 分类[0-文生图，1-图生图]
     */
    private Integer category;

    /**
     * 描述词 模糊匹配中文
     */
    private String keyword;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
