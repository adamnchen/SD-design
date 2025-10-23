package com.sutran.sd.common.core.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 打样邀约发起请求 DTO
 */
@Data
public class ProofingInvitationRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 【必填】关联的作品ID
     */
    @NotNull(message = "作品ID不能为空")
    private String workId;

    /**
     * 【必填】被邀约用户ID列表(最多3个)
     */
    @NotNull(message = "被邀约用户列表不能为空")
    private List<Long> inviteeUserIds;

    // --- 作品和设计详情 ---

    /**
     * 【必填】打样产品的标题
     */
    @NotBlank(message = "产品标题不能为空")
    private String productTitle;

    /**
     * 邀约作品的详细描述
     */
    private String productDescription;

    /**
     * 模型来源或版本号
     */
    private String modelSource;

    // --- 邀约细节和要求 ---

    /**
     * 其他合作内容详情
     */
    private String cooperationContent;

    /**
     * 发起邀约时填写的关键词/标签
     */
    private String keywords;

    /**
     * 【必填】是否需要批量生产（0:否, 1:是）
     */
    @NotNull(message = "是否需要批量生产不能为空")
    private Boolean isBatchProduction;

    /**
     * 【必填】期望的打样数量 (件)
     */
    @NotNull(message = "打样数量不能为空")
    private Integer proofingQuantity;

    // --- 时限 ---

    /**
     * 【必填】发起方要求的预计交付时限 (小时)
     */
    @NotNull(message = "交付时限不能为空")
    private Integer deliveryLimitHours;

    /**
     * 【必填】无人应答自动取消时限 (1:一天, 2:两天, 3:三天)
     */
    @NotNull(message = "自动取消时限不能为空")
    private Integer cancelTimeLimit;

    /**
     * 【必填】打样样品参与抽奖的数量分配给众筹用户,最少一个
     */
    @NotNull(message = "抽奖数量不能为空")
    private Integer drawNumber;

    // --- 收货信息 ---

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人手机号
     */
    @NotBlank(message = "收货人手机号不能为空")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    /**
     * 收货地区
     */
    private String receiverArea;

    /**
     * 状态
     */
    private Integer status;
}
