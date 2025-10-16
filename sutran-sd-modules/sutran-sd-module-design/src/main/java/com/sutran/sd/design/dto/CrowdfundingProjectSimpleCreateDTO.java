package com.sutran.sd.design.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 众筹项目简化创建DTO
 * 前端只需要传打样邀约ID和厂家ID，其他信息从打样邀约中获取
 *
 * @author sutran
 * @date 2025-10-16
 */
@Data
public class CrowdfundingProjectSimpleCreateDTO {

    /**
     * 关联的打样邀约ID
     */
    @NotNull(message = "打样邀约ID不能为空")
    private Long proofingInvitationId;

    /**
     * 厂家用户ID（被选中的厂家）
     */
    @NotNull(message = "厂家用户ID不能为空")
    private Long manufacturerUserId;
}
