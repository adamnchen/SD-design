package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹项目列表VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingProjectListVO {

    /**
     * 众筹项目ID
     */
    private Long id;

    /**
     * 项目编号
     */
    private String projectNo;

    /**
     * 项目标题
     */
    private String title;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 发起人姓名
     */
    private String creatorName;

    /**
     * 发起人头像
     */
    private String creatorAvatar;

    /**
     * 发起人用户ID
     */
    private Long creatorUserId;

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
     * 项目状态：1=众筹中，2=众筹成功，3=众筹失败
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
     * 阶梯价格配置(JSON数组: [{"unitPrice":100,"node":20}])
     * 从打样邀约中继承
     */
    private String tieredPricing;
}
