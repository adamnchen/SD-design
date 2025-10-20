package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布预售项目DTO
 *
 * @author sutran
 * @date 2025-10-20
 */
@Data
public class PresaleProjectPublishDTO {

    /**
     * 项目标题（可选，不填写则继承打样邀约的产品标题）
     */
    private String title;

    /**
     * 项目详细描述（可选，不填写则继承打样邀约的产品描述）
     */
    private String description;

    /**
     * 关联的打样邀约ID（用于获取AI设计图）
     */
    @NotNull(message = "打样邀约ID不能为空")
    private Long proofingInvitationId;

    /**
     * 基础单价（可选，不填写则继承打样邀约的报价）
     */
    private BigDecimal basePrice;

    /**
     * 阶梯价格配置（可选，不填写则继承打样邀约的阶梯价格）
     */
    private List<TieredPricingItem> tieredPricing;

    /**
     * 有效期天数（从创建时间开始计算）
     */
    @NotNull(message = "有效期天数不能为空")
    private Integer validityDays;

    /**
     * 厂家上传的实物照片（JSON格式，多张图片URL）
     */
    private String manufacturerPhotos;

    /**
     * 阶梯价格配置项
     */
    @Data
    public static class TieredPricingItem {
        /**
         * 单价
         */
        private BigDecimal unitPrice;

        /**
         * 数量节点
         */
        private Integer node;
    }
}
