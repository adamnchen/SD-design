package com.sutran.sd.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付会员配置
 * @author zj
 * @date 2025年08月21日 10:50
 */
@Data
@TableName("pay_member")
public class PayMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员配置ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 会员等级
     */
    private Integer level;
    /**
     * 等级名称
     */
    @NotBlank(message = "等级名称不能为空")
    private String levelName;
    /**
     * 会员权益描述
     */
    @NotBlank(message = "会员权益描述不能为空")
    private String description;
    /**
     * 会员价格
     */
    @NotNull(message = "会员价格不能为空")
    private BigDecimal price;
    /**
     * 会员有效期(天)
     */
    @NotNull(message = "会员有效期不能为空")
    private Integer duration;
    /**
     * 会员训练次数
     */
    private Integer limitTrainTimes;
    /**
     * 会员绘图次数
     */
    private Integer limitDrawNum;
    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
    /**
     * 是否隐藏（0否 1是）
     */
    private Integer isHide;
    /**
     * 创建人
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人
     */
    private Long updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;
}
