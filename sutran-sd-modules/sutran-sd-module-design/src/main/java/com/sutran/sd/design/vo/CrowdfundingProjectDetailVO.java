package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹项目详情VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingProjectDetailVO {

    /**
     * 众筹项目ID
     */
    private Long id;

    /**
     * 项目编号
     */
    private String projectNo;

    /**
     * 关联的打样邀约ID
     */
    private Long proofingInvitationId;

    /**
     * 项目标题
     */
    private String title;

    /**
     * 项目详细描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 项目图片列表(JSON格式)
     */
    private String images;

    /**
     * 项目视频URL
     */
    private String videoUrl;

    /**
     * 项目标签，逗号分隔
     */
    private String tags;

    /**
     * 发起人用户ID
     */
    private Long creatorUserId;

    /**
     * 发起人姓名
     */
    private String creatorName;

    /**
     * 发起人头像
     */
    private String creatorAvatar;

    /**
     * 厂家用户ID
     */
    private Long manufacturerUserId;

    /**
     * 厂家姓名
     */
    private String manufacturerName;

    /**
     * 厂家头像
     */
    private String manufacturerAvatar;

    /**
     * 目标金额
     */
    private BigDecimal targetAmount;

    /**
     * 当前已筹金额
     */
    private BigDecimal currentAmount;

    /**
     * 支持人数
     */
    private Integer supportCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 众筹开始时间
     */
    private Date startTime;

    /**
     * 众筹结束时间
     */
    private Date endTime;

    /**
     * 预计发货时间
     */
    private Date deliveryTime;

    /**
     * 项目状态：1=众筹中，2=众筹成功，3=众筹失败，4=已发布
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 是否精选
     */
    private Boolean isFeatured;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 风险提示
     */
    private String riskTips;

    /**
     * 抽奖名额数量
     */
    private Integer drawNumber;

    /**
     * 抽奖状态：0=未开始，1=进行中，2=已结束
     */
    private Integer drawStatus;

    /**
     * 抽奖状态描述
     */
    private String drawStatusDesc;

    /**
     * 抽奖时间
     */
    private Date drawTime;

    /**
     * 众筹进度百分比
     */
    private BigDecimal progressPercentage;

    /**
     * 剩余天数
     */
    private Long remainingDays;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 阶梯价格配置（JSON格式）
     */
    private String tieredPricing;

    /**
     * 阶梯价格列表
     */
    private java.util.List<TieredPricingItem> tieredPricingList;

    /**
     * 当前价格（基于当前支持数量计算的阶梯价格）
     */
    private BigDecimal currentPrice;

    /**
     * 下一个价格阈值
     */
    private Integer nextThreshold;

    /**
     * 下一个价格
     */
    private BigDecimal nextPrice;

    /**
     * 利润分成比例
     */
    private String profitShareRatio;
}
