package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 众筹项目创建DTO
 *
 * @author sutran
 * @date 2025-10-10
 */
@Data
public class CrowdfundingProjectCreateDTO {

    /**
     * 关联的打样邀约ID
     */
    @NotNull(message = "打样邀约ID不能为空")
    private Long proofingInvitationId;

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
     * 目标金额
     */
    @NotNull(message = "目标金额不能为空")
    private BigDecimal targetAmount;

    /**
     * 众筹开始时间
     */
    @NotNull(message = "众筹开始时间不能为空")
    private Date startTime;

    /**
     * 众筹结束时间
     */
    @NotNull(message = "众筹结束时间不能为空")
    private Date endTime;

    /**
     * 预计发货时间
     */
    private Date deliveryTime;

    /**
     * 风险提示
     */
    private String riskTips;

    /**
     * 抽奖名额数量
     */
    @NotNull(message = "抽奖名额数量不能为空")
    private Integer drawNumber;
}
