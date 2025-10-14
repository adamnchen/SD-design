package com.sutran.sd.common.core.utils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 阶梯价格工具类
 * 处理商家输入的价格点，自动计算对应区间的价格
 * 
 * @author sutran
 * @date 2025-10-14
 */
public class TieredPricingUtils {

    /**
     * 根据数量获取对应的单价
     * 
     * @param quantity 订货数量
     * @param tieredPricing 阶梯价格配置 [20, 30, 40]
     * @return 对应的单价
     */
    public static BigDecimal getUnitPrice(int quantity, List<BigDecimal> tieredPricing) {
        if (tieredPricing == null || tieredPricing.isEmpty()) {
            throw new IllegalArgumentException("阶梯价格配置不能为空");
        }
        
        // 如果数量小于等于第一个价格点，返回第一个价格
        if (quantity <= tieredPricing.get(0).intValue()) {
            return tieredPricing.get(0);
        }
        
        // 遍历价格点，找到对应的区间
        for (int i = 0; i < tieredPricing.size() - 1; i++) {
            int currentThreshold = tieredPricing.get(i).intValue();
            int nextThreshold = tieredPricing.get(i + 1).intValue();
            
            if (quantity > currentThreshold && quantity <= nextThreshold) {
                return tieredPricing.get(i + 1);
            }
        }
        
        // 如果数量超过所有价格点，返回最后一个价格
        return tieredPricing.get(tieredPricing.size() - 1);
    }
    
    /**
     * 计算总价格
     * 
     * @param quantity 订货数量
     * @param tieredPricing 阶梯价格配置
     * @return 总价格
     */
    public static BigDecimal calculateTotalPrice(int quantity, List<BigDecimal> tieredPricing) {
        BigDecimal unitPrice = getUnitPrice(quantity, tieredPricing);
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * 获取价格区间描述
     * 
     * @param tieredPricing 阶梯价格配置
     * @return 价格区间描述
     */
    public static String getPriceRangeDescription(List<BigDecimal> tieredPricing) {
        if (tieredPricing == null || tieredPricing.isEmpty()) {
            return "无阶梯价格";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("0-").append(tieredPricing.get(0).intValue()).append("件: ").append(tieredPricing.get(0)).append("元/件");
        
        for (int i = 1; i < tieredPricing.size(); i++) {
            int prevThreshold = tieredPricing.get(i - 1).intValue();
            int currentThreshold = tieredPricing.get(i).intValue();
            sb.append(", ").append(prevThreshold).append("-").append(currentThreshold).append("件: ").append(tieredPricing.get(i)).append("元/件");
        }
        
        // 最后一个区间是无上限的
        int lastThreshold = tieredPricing.get(tieredPricing.size() - 1).intValue();
        sb.append(", ").append(lastThreshold).append("件以上: ").append(tieredPricing.get(tieredPricing.size() - 1)).append("元/件");
        
        return sb.toString();
    }
}
