package com.sutran.sd.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 可分享用户基础信息VO
 * @author zj
 * @date 2025年11月03日 17:45
 */
@Data
@Accessors(chain=true)
public class UserBaseVo {
    /**
     * 用户ID
     */
    @Schema(name = "userId", description = "用户ID")
    private Long userId;
    /**
     * 用户昵称
     */
    @Schema(name = "nickName", description = "用户昵称")
    private String nickName;
}
