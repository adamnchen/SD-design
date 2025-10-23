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
 * 预售项目对象 sd_presale_project
 *
 * @author sutran
 * @date 2025-01-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_presale_project")
public class SdPresaleProject extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 预售项目ID
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
     * 发起人用户ID
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
     * 发起人头像
     */
    @TableField("creator_avatar")
    private String creatorAvatar;

    /**
     * 厂家用户ID
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
     * 关联的打样邀约ID
     */
    @TableField("proofing_invitation_id")
    private Long proofingInvitationId;

    /**
     * 基础单价（最低阶梯价格）
     */
    @TableField("base_price")
    @NotNull(message = "基础单价不能为空")
    private BigDecimal basePrice;

    /**
     * 阶梯价格配置(JSON数组: [{"unitPrice":100,"node":20}])
     */
    @TableField("tiered_pricing")
    private String tieredPricing;

    /**
     * 有效期天数（从创建时间开始计算）
     */
    @TableField("validity_days")
    @NotNull(message = "有效期天数不能为空")
    private Integer validityDays;

    /**
     * 项目状态：1=销售中，2=暂停销售，3=已下架，4=已取消
     */
    @TableField("status")
    @NotNull(message = "项目状态不能为空")
    private Integer status;

    /**
     * 生产状态：0=未开始，1=进行中，2=已完成
     */
    @TableField("production_status")
    private Integer productionStatus;

    /**
     * 发货状态：0=未开始，1=进行中，2=已完成
     */
    @TableField("delivery_status")
    private Integer deliveryStatus;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 收藏次数
     */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /**
     * 分享次数
     */
    @TableField("share_count")
    private Integer shareCount;

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
     * 销售数量
     */
    @TableField("total_quantities")
    private Integer totalQuantities;

    /**
     * 累计销售金额
     */
    @TableField("total_sales_amount")
    private BigDecimal totalSalesAmount;
}
