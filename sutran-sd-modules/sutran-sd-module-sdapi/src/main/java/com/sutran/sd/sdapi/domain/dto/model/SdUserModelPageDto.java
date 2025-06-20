package com.sutran.sd.sdapi.domain.dto.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * lora模型分页查询
 * @author zj
 * @date 2024-03-08
 */
@Data
@Accessors(chain = true)
public class SdUserModelPageDto implements Serializable {

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
     * 搜索值
     */
    private String searchValue;

    /**
     * 是否分享模型
     */
    private Boolean shareModel;

    /**
     * 模型分类ID
     */
    private String classifyId;
}
