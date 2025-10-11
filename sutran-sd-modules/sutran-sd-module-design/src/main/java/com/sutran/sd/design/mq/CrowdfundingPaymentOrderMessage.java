package com.sutran.sd.design.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹支付订单MQ消息实体
 *
 * @author chenshan
 * @date 2025-10-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrowdfundingPaymentOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 众筹项目ID
     */
    private Long projectId;

    /**
     * 支持用户ID
     */
    private Long userId;

    /**
     * 支持用户姓名
     */
    private String userName;

    /**
     * 支持金额
     */
    private BigDecimal supportAmount;

    /**
     * 消息创建时间
     */
    private Date createTime;

    /**
     * 消息类型：PAYMENT_ORDER_CREATE
     */
    private String messageType;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
}
