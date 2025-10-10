package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 众筹抽奖记录对象 sd_crowdfunding_draw
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_crowdfunding_draw")
public class SdCrowdfundingDraw extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 抽奖记录ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 众筹项目ID
     */
    @TableField("project_id")
    @NotNull(message = "众筹项目ID不能为空")
    private Long projectId;

    /**
     * 参与抽奖用户ID
     */
    @TableField("user_id")
    @NotNull(message = "参与抽奖用户ID不能为空")
    private Long userId;

    /**
     * 参与抽奖用户姓名
     */
    @TableField("user_name")
    @NotBlank(message = "参与抽奖用户姓名不能为空")
    private String userName;

    /**
     * 参与抽奖用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 抽奖编号
     */
    @TableField("draw_no")
    @NotBlank(message = "抽奖编号不能为空")
    private String drawNo;

    /**
     * 是否中奖：0=否，1=是
     */
    @TableField("is_winner")
    private Integer isWinner;

    /**
     * 奖品名称
     */
    @TableField("prize_name")
    private String prizeName;

    /**
     * 奖品描述
     */
    @TableField("prize_description")
    private String prizeDescription;

    /**
     * 奖品图片
     */
    @TableField("prize_image")
    private String prizeImage;

    /**
     * 中奖时间
     */
    @TableField("win_time")
    private Date winTime;

    /**
     * 中奖顺序（第几个中奖）
     */
    @TableField("win_order")
    private Integer winOrder;

    /**
     * 是否已领取：0=否，1=是
     */
    @TableField("is_claimed")
    private Integer isClaimed;

    /**
     * 领取时间
     */
    @TableField("claim_time")
    private Date claimTime;

    /**
     * 收货地址
     */
    @TableField("claim_address")
    private String claimAddress;

    /**
     * 收货电话
     */
    @TableField("claim_phone")
    private String claimPhone;

    /**
     * 收货人姓名
     */
    @TableField("claim_name")
    private String claimName;

    /**
     * 发货状态：0=未发货，1=已发货，2=已收货
     */
    @TableField("shipping_status")
    private Integer shippingStatus;

    /**
     * 发货时间
     */
    @TableField("shipping_time")
    private Date shippingTime;

    /**
     * 物流公司
     */
    @TableField("shipping_company")
    private String shippingCompany;

    /**
     * 物流单号
     */
    @TableField("shipping_no")
    private String shippingNo;

    /**
     * 收货时间
     */
    @TableField("receive_time")
    private Date receiveTime;
}
