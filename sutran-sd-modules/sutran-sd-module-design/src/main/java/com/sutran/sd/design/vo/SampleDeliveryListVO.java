package com.sutran.sd.design.vo;

import com.sutran.sd.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 样品发货记录列表VO
 *
 * @author sutran
 * @date 2025-01-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SampleDeliveryListVO extends BaseEntity {

    /**
     * 数据ID
     */
    private Long id;

    /**
     * 众筹项目ID
     */
    private Long crowdfundingProjectId;

    /**
     * 众筹项目标题
     */
    private String projectTitle;

    /**
     * 关联邀约记录ID
     */
    private Long proofingInvitationId;

    /**
     * 样品图片地址
     */
    private String sampleImageUrl;

    /**
     * 收货人用户ID
     */
    private Long recipientUserId;

    /**
     * 收货人姓名
     */
    private String recipientName;

    /**
     * 收货人电话
     */
    private String recipientPhone;

    /**
     * 收货人地址
     */
    private String deliveryAddress;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private String deliveryCompany;

    /**
     * 状态：1=待发货，2=已发货
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 备注
     */
    private String remark;

    /**
     * 发货人用户ID
     */
    private Long senderUserId;

    /**
     * 发货人姓名
     */
    private String senderName;

    /**
     * 发货时间
     */
    private Date deliveryTime;

    /**
     * 确认收货时间
     */
    private Date confirmTime;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 发货数量
     */
    private Integer quantity;

    /**
     * 支付订单编号
     * 中奖人的支付订单编号，如果是发起者自留的，返回生成的订单编号（INITIATOR_WINNER_项目ID）
     */
    private String orderNo;
}
