package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹项目对象 sd_crowdfunding_project
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_crowdfunding_project")
public class SdCrowdfundingProject extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 众筹项目ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 项目编号
     */
    @TableField("project_no")
    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    /**
     * 项目标题
     */
    @TableField("title")
    @NotBlank(message = "项目标题不能为空")
    private String title;

    /**
     * 项目详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面图片URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 项目图片列表(JSON格式)
     */
    @TableField("images")
    private String images;

    /**
     * 项目视频URL
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 项目标签，逗号分隔
     */
    @TableField("tags")
    private String tags;

    /**
     * 发起人用户ID（邀约人）
     */
    @TableField("creator_user_id")
    @NotNull(message = "发起人用户ID不能为空")
    private Long creatorUserId;

    /**
     * 发起人姓名
     */
    @TableField("creator_name")
    @NotBlank(message = "发起人姓名不能为空")
    private String creatorName;

    /**
     * 关联的打样邀约ID
     */
    @TableField("proofing_invitation_id")
    private Long proofingInvitationId;

    /**
     * 发起人头像
     */
    @TableField("creator_avatar")
    private String creatorAvatar;

    /**
     * 厂家用户ID（被邀约人）
     */
    @TableField("manufacturer_user_id")
    @NotNull(message = "厂家用户ID不能为空")
    private Long manufacturerUserId;

    /**
     * 厂家姓名
     */
    @TableField("manufacturer_name")
    @NotBlank(message = "厂家姓名不能为空")
    private String manufacturerName;

    /**
     * 厂家头像
     */
    @TableField("manufacturer_avatar")
    private String manufacturerAvatar;

    /**
     * 目标金额
     */
    @TableField("target_amount")
    @NotNull(message = "目标金额不能为空")
    private BigDecimal targetAmount;

    /**
     * 当前已筹金额
     */
    @TableField("current_amount")
    private BigDecimal currentAmount;

    /**
     * 支持人数
     */
    @TableField("support_count")
    private Integer supportCount;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 众筹开始时间
     */
    @TableField("start_time")
    @NotNull(message = "众筹开始时间不能为空")
    private Date startTime;

    /**
     * 众筹结束时间
     */
    @TableField("end_time")
    @NotNull(message = "众筹结束时间不能为空")
    private Date endTime;

    /**
     * 预计发货时间
     */
    @TableField("delivery_time")
    private Date deliveryTime;

    /**
     * 项目状态：1=众筹中，2=众筹成功，3=众筹失败
     */
    @TableField("status")
    @NotNull(message = "项目状态不能为空")
    private Integer status;

    /**
     * 是否精选：0=否，1=是
     */
    @TableField("is_featured")
    private Integer isFeatured;

    /**
     * 是否热门：0=否，1=是
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 风险提示
     */
    @TableField("risk_tips")
    private String riskTips;

    /**
     * 抽奖名额数量（从打样邀约继承）
     */
    @TableField("draw_number")
    private Integer drawNumber;

    /**
     * 样品总数量
     */
    @TableField("total_samples")
    private Integer totalSamples;

    /**
     * 抽奖状态：0=未开始，1=进行中，2=已结束
     */
    @TableField("draw_status")
    private Integer drawStatus;

    /**
     * 抽奖时间
     */
    @TableField("draw_time")
    private Date drawTime;

    /**
     * 厂家上传的实物照片（JSON格式，多张图片）
     */
    @TableField("manufacturer_photos")
    private String manufacturerPhotos;

    /**
     * 厂家上传照片时间
     */
    @TableField("manufacturer_upload_time")
    private Date manufacturerUploadTime;

    /**
     * 资金托管状态：0=托管中，1=已释放给厂家，2=已退款
     */
    @TableField("escrow_status")
    private Integer escrowStatus;

    /**
     * 资金释放时间
     */
    @TableField("fund_release_time")
    private Date fundReleaseTime;

    /**
     * 阶梯价格配置(JSON数组: [{"unitPrice":100,"node":20}])
     * 从打样邀约中继承
     */
    @TableField(exist = false)
    private String tieredPricing;

    @TableField(exist = false)
    private String profitShareRatio;
}
