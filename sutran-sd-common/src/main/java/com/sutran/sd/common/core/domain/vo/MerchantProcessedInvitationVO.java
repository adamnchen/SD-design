package com.sutran.sd.common.core.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商家已处理邀约详情VO
 * 商家只能查看自己的报价信息，包括阶梯价格、利润分成等
 * 
 * @author SutranSD
 */
@Data
public class MerchantProcessedInvitationVO {
    
    /**
     * 邀约ID
     */
    private Long id;
    
    /**
     * 作品ID
     */
    private String workId;
    
    /**
     * 产品标题
     */
    private String productTitle;
    
    /**
     * 产品描述
     */
    private String productDescription;
    
    /**
     * 模型来源
     */
    private String modelSource;
    
    /**
     * 邀约人信息
     */
    private InviterInfo inviterInfo;
    
    /**
     * 合作内容
     */
    private String cooperationContent;
    
    /**
     * 关键词/标签
     */
    private String keywords;
    
    /**
     * 是否需要批量生产
     */
    private Boolean isBatchProduction;
    
    /**
     * 打样数量
     */
    private Integer proofingQuantity;
    
    /**
     * 交付时限（小时）
     */
    private Integer deliveryLimitHours;
    
    /**
     * 抽奖数量
     */
    private Integer drawNumber;
    
    /**
     * 邀约创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    // === 商家自己的报价信息 ===
    
    /**
     * 商家提交的报价金额
     */
    private BigDecimal quotedPrice;
    
    /**
     * 商家提交的预计打样周期（天）
     */
    private Integer quotedPeriodDays;
    
    /**
     * 是否提供了批量生产方案
     */
    private Boolean isQuoteBatchPlan;
    
    /**
     * 报价提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date quoteSubmitAt;
    
    /**
     * 阶梯价格配置
     */
    private List<TieredPricing> tieredPricing;
    
    /**
     * 利润分成比例（%）
     */
    private BigDecimal profitShareRatio;
    
    /**
     * 邀约状态
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 是否被选中
     */
    private Boolean isSelected;
    
    /**
     * 最终选择时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date selectedAt;
    
    /**
     * 邀约人信息内部类
     */
    @Data
    public static class InviterInfo {
        /**
         * 邀约人用户ID
         */
        private Long userId;
        
        /**
         * 邀约人昵称
         */
        private String nickName;
        
        /**
         * 邀约人头像
         */
        private String avatar;
        
        /**
         * 邀约人描述
         */
        private String description;
    }
    
    /**
     * 阶梯价格内部类
     */
    @Data
    public static class TieredPricing {
        /**
         * 最小数量
         */
        private Integer minQty;
        
        /**
         * 最大数量（null表示无上限）
         */
        private Integer maxQty;
        
        /**
         * 单价
         */
        private BigDecimal unitPrice;
    }
    
    // 状态常量
    public static final class Status {
        /** 待处理 */
        public static final int PENDING = 0;
        /** 已接受 */
        public static final int ACCEPTED = 1;
        /** 已拒绝 */
        public static final int REJECTED = 2;
        /** 已取消 */
        public static final int CANCELLED = 3;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case Status.PENDING:
                return "待处理";
            case Status.ACCEPTED:
                return "已接受";
            case Status.REJECTED:
                return "已拒绝";
            case Status.CANCELLED:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
