package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 预售项目详情VO
 *
 * @author sutran
 * @date 2025-01-12
 */
@Data
public class PresaleProjectDetailVO {

    /**
     * 预售项目ID
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
     * 关联的众筹项目ID
     */
    private Long crowdfundingProjectId;

    /**
     * 阶梯价格配置(JSON格式)
     */
    private String tierPricing;

    /**
     * 当前预售数量
     */
    private Integer currentQuantity;

    /**
     * 预售开始时间
     */
    private Date startTime;

    /**
     * 预售结束时间
     */
    private Date endTime;

    /**
     * 预计发货时间
     */
    private Date deliveryTime;

    /**
     * 项目状态：1=预售中，2=预售成功，3=预售失败，4=已结束
     */
    private Integer status;

    /**
     * 是否精选：0=否，1=是
     */
    private Integer isFeatured;

    /**
     * 是否热门：0=否，1=是
     */
    private Integer isHot;

    /**
     * 风险提示
     */
    private String riskTips;

    /**
     * 当前价格（根据阶梯价格计算）
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
     * 剩余天数
     */
    private Long remainingDays;

    /**
     * 阶梯价格列表（解析后的对象）
     */
    private Object tierPricingList;
}
