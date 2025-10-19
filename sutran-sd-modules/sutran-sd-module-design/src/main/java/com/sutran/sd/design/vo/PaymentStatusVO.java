package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付状态查询VO
 *
 * @author sutran
 */
@Data
public class PaymentStatusVO {
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 支付订单状态[0-待支付,1-已支付,2-支付失败,3-已关闭]
     */
    private Integer payStatus;
    
    /**
     * 支付状态描述
     */
    @SuppressWarnings("unused")
    private String payStatusDesc;
    
    /**
     * 支持记录是否存在
     */
    private Boolean supportExists;
    
    /**
     * 支持记录是否已处理（支付成功）
     */
    private Boolean processed;
    
    /**
     * 支持金额
     */
    private BigDecimal supportAmount;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 项目标题
     */
    private String projectTitle;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 支付时间
     */
    private Date payTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 支付宝交易流水号
     */
    private String tradeNo;
    
    /**
     * 是否支付成功
     */
    public boolean isPaymentSuccess() {
        return payStatus != null && payStatus == 1;
    }
    
    /**
     * 是否支付失败
     */
    public boolean isPaymentFailed() {
        return payStatus != null && (payStatus == 2 || payStatus == 3);
    }
    
    /**
     * 是否待支付
     */
    public boolean isPaymentPending() {
        return payStatus != null && payStatus == 0;
    }
    
    /**
     * 获取支付状态描述
     */
    public String getPayStatusDesc() {
        if (payStatus == null) {
            return "未知状态";
        }
        switch (payStatus) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "支付失败";
            case 3:
                return "已关闭";
            default:
                return "未知状态";
        }
    }
}
