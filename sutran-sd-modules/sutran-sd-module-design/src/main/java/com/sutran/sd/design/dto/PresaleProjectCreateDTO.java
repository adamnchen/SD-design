package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建预售项目DTO
 *
 * @author sutran
 * @date 2025-10-19
 */
@Data
public class PresaleProjectCreateDTO {

    /**
     * 项目标题
     */
    @NotBlank(message = "项目标题不能为空")
    private String title;

    /**
     * 项目详细描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 项目图片列表(JSON格式)
     */
    private String images;

    /**
     * 项目视频URL
     */
    private String videoUrl;

    /**
     * 项目标签，逗号分隔
     */
    private String tags;

    /**
     * 厂家用户ID
     */
    @NotNull(message = "厂家用户ID不能为空")
    private Long manufacturerUserId;

    /**
     * 关联的打样邀约ID
     */
    private Long proofingInvitationId;

    /**
     * 基础单价（最低阶梯价格）
     */
    @NotNull(message = "基础单价不能为空")
    private BigDecimal basePrice;

    /**
     * 阶梯价格配置
     */
    private List<TieredPricingItem> tieredPricing;

    /**
     * 有效期天数（从创建时间开始计算）
     */
    @NotNull(message = "有效期天数不能为空")
    private Integer validityDays;

    /**
     * 厂家上传的实物照片（JSON格式，多张图片）
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
