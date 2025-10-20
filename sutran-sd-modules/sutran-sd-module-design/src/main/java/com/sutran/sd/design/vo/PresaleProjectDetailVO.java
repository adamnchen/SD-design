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
     * 关联的打样邀约ID
     */
    private Long proofingInvitationId;

    /**
     * 基础单价（最低阶梯价格）
     */
    private BigDecimal basePrice;

    /**
     * 阶梯价格配置(JSON数组: [{"unitPrice":100,"node":20}])
     */
    private String tieredPricing;

    /**
     * 有效期天数（从创建时间开始计算）
     */
    private Integer validityDays;

    /**
     * 项目状态：1=销售中，2=暂停销售，3=已下架，4=已取消
     */
    private Integer status;

    /**
     * 项目状态描述
     */
    private String statusDesc;

    /**
     * 生产状态：0=未开始，1=进行中，2=已完成
     */
    private Integer productionStatus;

    /**
     * 发货状态：0=未开始，1=进行中，2=已完成
     */
    private Integer deliveryStatus;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 厂家上传的实物照片（JSON格式，多张图片）
     */
    private String manufacturerPhotos;

    /**
     * 厂家上传照片时间
     */
    private Date manufacturerUploadTime;

    /**
     * 累计销售金额
     */
    private BigDecimal totalSalesAmount;

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
     * 剩余天数（如果设置了结束时间）
     */
    private Long remainingDays;

    /**
     * 阶梯价格列表（解析后的对象）
     */
    private Object tieredPricingList;

    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1: return "销售中";
            case 2: return "暂停销售";
            case 3: return "已下架";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
