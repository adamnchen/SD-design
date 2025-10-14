package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户会员关联表
 * @author zj
 * @date 2025年08月24日 23:02
 */
@Data
@TableName("sys_user_member")
@Accessors(chain = true)
public class SysUserMember  implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 会员ID
     */
    private Long memberId;
    /**
     * 会员等级名称
     */
    private String levelName;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 会员有效期(天)
     */
    private Integer duration;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 状态(0:已过期,1:有效)
     */
    private Integer status;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 原始会员训练次数(主要记录叠加购买会员前的训练次数)
     */
    private Integer oldLimitTrainTimes;
    /**
     * 原始会员绘图次数(主要记录叠加购买会员前的绘图次数)
     */
    private Integer oldLimitDrawNum;
    /**
     * 会员训练次数(主要记录购买会员的总训练次数)
     */
    private Integer limitTrainTimes;
    /**
     * 会员绘图次数(主要记录购买会员的总绘图次数)
     */
    private Integer limitDrawNum;
    /**
     * 已使用的会员训练次数
     */
    private Integer useTrainTimes;
    /**
     * 已使用的会员绘图次数
     */
    private Integer useDrawNum;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
