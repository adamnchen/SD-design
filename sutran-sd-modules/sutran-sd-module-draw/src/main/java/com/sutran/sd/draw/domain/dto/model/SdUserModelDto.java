package com.sutran.sd.draw.domain.dto.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-04-17
 */
@Data
public class SdUserModelDto implements Serializable {

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
     * 发布状态
     */
    private Integer publishStatus;
    private String modelNameZh;
    private String belongUserName;
    private String startDate;
    private String endDate;

}
