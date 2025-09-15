package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * SD绘图 || 工作流(SdFlow)表实体类
 *
 * @author makejava
 * @since 2025-09-07 23:17:28
 */
@Data
@Accessors(chain = true)
public class SdFlow {
    /**
     * 工作流ID
     */
    @TableId
    private Long id;
    /**
     * 工作流名称
     */
    private String name;
    /**
     * 工作流生图数量
     */
    private Integer drawNum;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 工作流
     */
    private String flow;
    /**
     * 归属人ID
     */
    private Long belongUserId;
    /**
     * 归属人名称
     */
    private String belongUserName;
    /**
     * 是否开放[0-否,1-是]
     */
    private Integer isOpen;
    /**
     * 模型类型[SDXL,FLUX]
     */
    private String modelType;
    /**
     * 是否固定[0-否,1-是]
     */
    private Integer isFixed;

}

