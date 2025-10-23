package com.sutran.sd.design.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 阶梯价格配置项
 *
 * @author sutran
 * @date 2025-01-22
 */
@Data
public class TieredPricingItem {
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 数量节点
     */
    private Integer node;
}
