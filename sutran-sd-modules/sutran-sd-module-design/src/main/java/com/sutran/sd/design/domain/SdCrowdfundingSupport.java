package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 众筹支持记录对象 sd_crowdfunding_support
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_crowdfunding_support")
public class SdCrowdfundingSupport extends BaseEntity {

    /**
     * 支持记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 众筹项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 参与者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 参与者姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 支付订单号（唯一）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 支持金额
     */
    @TableField("support_amount")
    private BigDecimal supportAmount;

    /**
     * 抽奖状态：0=未参与，1=已参与，2=中奖，3=未中奖
     */
    @TableField("draw_status")
    private Integer drawStatus;

    /**
     * 是否中奖：0=否，1=是
     */
    @TableField("is_winner")
    private Integer isWinner;

    /**
     * 奖品信息（JSON格式）
     */
    @TableField("prize_info")
    private String prizeInfo;
}
