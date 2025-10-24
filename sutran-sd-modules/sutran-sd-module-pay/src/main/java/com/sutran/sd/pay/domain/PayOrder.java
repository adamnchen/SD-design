package com.sutran.sd.pay.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付订单记录
 * @author zj
 * @date 2025年08月21日 10:50
 */
@Data
@TableName("pay_order")
@Accessors(chain = true)
public class PayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 系统订单号(唯一)
     */
    private String outTradeNo;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 支付宝应用编号
     */
    private String appId;
    /**
     * 支付宝交易流水号
     */
    private String tradeNo;
    /**
     * 支付时间
     */
    private Date gmtPayment;
    /**
     * 订单标题
     */
    private String subject;
    /**
     * 订单描述
     */
    private String body;
    /**
     * 订单金额
     */
    private BigDecimal totalAmount;
    /**
     * 支付状态[0-待支付,1-已支付,2-支付失败,3-已关闭]
     */
    private Integer status;
    /**
     * 支付渠道[ALI_PAY-支付宝支付]
     */
    private String channelType;
    /**
     * 业务类型[SD_MEMBER-SD会员]
     */
    private String businessType;
    /**
     * 业务ID
     */
    private Long businessId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 失效时间(截至时间)
     */
    private Date expireTime;
    /**
     * 支付宝异步通知时间
     */
    private Date notifyTime;
    /**
     * 支付宝异步通知结果
     */
    private String notifyResult;
    /**
     * 二维码
     */
    private String qrCode;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 退款原因
     */
    private String refundReason;

    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
