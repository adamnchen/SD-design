package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹支持VO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingSupportVO {

    /**
     * 支持记录ID
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
     * 参与者用户ID
     */
    private Long userId;

    /**
     * 参与者姓名
     */
    private String userName;

    /**
     * 支付订单号
     */
    private String orderNo;

    /**
     * 支持金额
     */
    private BigDecimal supportAmount;

    /**
     * 抽奖状态：0=未参与，1=已参与，2=中奖，3=未中奖
     */
    private Integer drawStatus;

    /**
     * 抽奖状态描述
     */
    private String drawStatusDesc;

    /**
     * 是否中奖：0=否，1=是
     */
    private Boolean isWinner;

    /**
     * 奖品信息（JSON格式）
     */
    private String prizeInfo;

    /**
     * 快递单号（样品发货）
     */
    private String trackingNumber;

    /**
     * 发货状态：0=待发货，1=已发货，2=已收货
     */
    private Integer deliveryStatus;

    /**
     * 发货时间
     */
    private Date deliveryTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
