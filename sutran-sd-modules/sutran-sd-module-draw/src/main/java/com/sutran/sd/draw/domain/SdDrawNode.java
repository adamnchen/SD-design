package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.draw.enums.NodeStatus;
import com.sutran.sd.draw.enums.NodeType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 节点表
 * @author zj
 * @date 2025-06-16
 */
@Data
@TableName("sd_draw_node")
@Accessors(chain = true)
public class SdDrawNode implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    @TableId
    private Long id;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点编码
     */
    private String code;
    /**
     * 节点类型[0-绘图,1-训练]
     */
    private Integer type;
    /**
     * 节点基础URL
     */
    private String baseUrl;
    /**
     * 是否可用
     */
    private Integer isActive;
    /**
     * 节点地区
     */
    private String region;
    /**
     * 权重
     */
    private Integer weight;
    /**
     * 最大任务并发数
     */
    private Integer maxConcurrentTasks;
    /**
     * 当前任务数
     */
    @TableField(exist = false)
    private Integer currentTasks;
    /**
     * 节点状态[ONLINE-在线,OFFLINE-离线]
     */
    @TableField(exist = false)
    private String status;
    /**
     * 最后健康检查时间
     */
    @TableField(exist = false)
    private Date lastHealthCheck;
    /**
     * 队列长度
     */
    @TableField(exist = false)
    private Integer queueSize;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建人
     */
    private Long createBy;
    /**
     * 更新人
     */
    private Long updateBy;

    public boolean isAvailable() {
        return Objects.equals(status, NodeStatus.ONLINE.name()) &&
            currentTasks < maxConcurrentTasks;
    }

    public double getLoadScore() {
        double taskLoad = (double) currentTasks / maxConcurrentTasks;
        double queueLoad = queueSize / 10.0;
        return (taskLoad * 0.6 + queueLoad * 0.4) / weight;
    }

    public NodeType getNodeType() {
        if (type==null){
            return NodeType.DRAW;
        }
        return NodeType.values()[type];
    }
}
