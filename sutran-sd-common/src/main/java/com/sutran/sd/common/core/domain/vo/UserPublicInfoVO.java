package com.sutran.sd.common.core.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户公开信息VO
 * 不包含敏感信息（密码、手机号、邮箱、支付宝账号等）
 *
 * @author SutranSD
 */
@Data
@Schema(description = "用户公开信息")
public class UserPublicInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户类型（sys_user系统用户,bs_user-业务用户）
     */
    @Schema(description = "用户类型")
    private String userType;

    /**
     * 用户身份类型（0=厂商和设计师，1=设计师，2=普通用户）
     */
    @Schema(description = "用户身份类型（0=厂商和设计师，1=设计师，2=普通用户）")
    private Integer bizType;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private Date loginDate;
}

