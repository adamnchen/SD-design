package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ProofingInvitationChooseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "邀约ID不能为空")
    private Long invitationId;

    @NotNull(message = "选择的厂家用户ID不能为空")
    private Long inviteeUserId;
}


