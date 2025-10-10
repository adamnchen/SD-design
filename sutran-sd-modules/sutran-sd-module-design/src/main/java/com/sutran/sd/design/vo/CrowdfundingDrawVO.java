package com.sutran.sd.design.vo;

import lombok.Data;

import java.util.Date;

/**
 * 众筹抽奖VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingDrawVO {

    /**
     * 抽奖记录ID
     */
    private Long id;

    /**
     * 众筹项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 参与抽奖用户ID
     */
    private Long userId;

    /**
     * 参与抽奖用户姓名
     */
    private String userName;

    /**
     * 参与抽奖用户头像
     */
    private String userAvatar;

    /**
     * 抽奖编号
     */
    private String drawNo;

    /**
     * 是否中奖：0=否，1=是
     */
    private Boolean isWinner;

    /**
     * 奖品名称
     */
    private String prizeName;

    /**
     * 奖品描述
     */
    private String prizeDescription;

    /**
     * 奖品图片
     */
    private String prizeImage;

    /**
     * 中奖时间
     */
    private Date winTime;

    /**
     * 中奖顺序（第几个中奖）
     */
    private Integer winOrder;

    /**
     * 是否已领取：0=否，1=是
     */
    private Boolean isClaimed;

    /**
     * 领取时间
     */
    private Date claimTime;

    /**
     * 收货地址
     */
    private String claimAddress;

    /**
     * 收货电话
     */
    private String claimPhone;

    /**
     * 收货人姓名
     */
    private String claimName;

    /**
     * 发货状态：0=未发货，1=已发货，2=已收货
     */
    private Integer shippingStatus;

    /**
     * 发货状态描述
     */
    private String shippingStatusDesc;

    /**
     * 发货时间
     */
    private Date shippingTime;

    /**
     * 物流公司
     */
    private String shippingCompany;

    /**
     * 物流单号
     */
    private String shippingNo;

    /**
     * 收货时间
     */
    private Date receiveTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
