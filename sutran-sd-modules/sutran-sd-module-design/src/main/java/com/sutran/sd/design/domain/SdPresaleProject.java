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
     * 关联的众筹项目ID
     */
    @TableField("crowdfunding_project_id")
    @NotNull(message = "关联的众筹项目ID不能为空")
    private Long crowdfundingProjectId;

    /**
     * 阶梯价格配置(JSON格式)
     * 例如: [{"quantity":30,"price":30.00},{"quantity":100,"price":25.00},{"quantity":200,"price":20.00}]
     */
    @TableField("tier_pricing")
    @NotBlank(message = "阶梯价格配置不能为空")
    private String tierPricing;

    /**
     * 当前预售数量
     */
    @TableField("current_quantity")
    private Integer currentQuantity;

    /**
     * 预售开始时间
     */
    @TableField("start_time")
    @NotNull(message = "预售开始时间不能为空")
    private Date startTime;

    /**
     * 预售结束时间
     */
    @TableField("end_time")
    @NotNull(message = "预售结束时间不能为空")
    private Date endTime;

    /**
     * 预计发货时间
     */
    @TableField("delivery_time")
    private Date deliveryTime;

    /**
     * 项目状态：1=预售中，2=预售成功，3=预售失败，4=已结束
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
}
