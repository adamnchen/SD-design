package com.sutran.sd.design.domain;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 众筹样品发货记录对象 sd_crowdfunding_sample_delivery
 *
 * @author sutran
 * @date 2025-01-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sd_crowdfunding_sample_delivery")
public class SdCrowdfundingSampleDelivery extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 众筹项目ID
     */
    @TableField("crowdfunding_project_id")
    @NotNull(message = "众筹项目ID不能为空")
    private Long crowdfundingProjectId;

    /**
     * 关联邀约记录ID
     */
    @TableField("proofing_invitation_id")
    @NotNull(message = "关联邀约记录ID不能为空")
    private Long proofingInvitationId;

    /**
     * 样品图片地址
     */
    @TableField("sample_image_url")
    private String sampleImageUrl;

    /**
     * 收货人用户ID
     */
    @TableField("recipient_user_id")
    @NotNull(message = "收货人用户ID不能为空")
    private Long recipientUserId;

    /**
     * 收货人姓名
     */
    @TableField("recipient_name")
    @NotBlank(message = "收货人姓名不能为空")
    private String recipientName;

    /**
     * 收货人电话
     */
    @TableField("recipient_phone")
    private String recipientPhone;

    /**
     * 收货人地址
     */
    @TableField("delivery_address")
    @NotBlank(message = "收货人地址不能为空")
    private String deliveryAddress;

    /**
     * 快递单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 快递公司
     */
    @TableField("delivery_company")
    private String deliveryCompany;

    /**
     * 状态：1=待发货，2=已发货
     */
    @TableField("delivery_status")
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 发货人用户ID
     */
    @TableField("sender_user_id")
    @NotNull(message = "发货人用户ID不能为空")
    private Long senderUserId;

    /**
     * 发货人姓名
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 发货时间
     */
    @TableField("delivery_time")
    private Date deliveryTime;

    /**
     * 确认收货时间
     */
    @TableField("confirm_time")
    private Date confirmTime;
    /**
     * 发货数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 订单状态
     */
    private Integer orderStatus;
}
