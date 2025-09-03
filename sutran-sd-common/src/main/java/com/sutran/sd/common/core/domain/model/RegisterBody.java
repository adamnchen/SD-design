package com.sutran.sd.common.core.domain.model;

import com.sutran.sd.common.constant.UserConstants;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

import static com.sutran.sd.common.enums.UserType.BS_USER;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
public class RegisterBody {

    /**
     * 用户密码（必填）
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = UserConstants.PASSWORD_MIN_LENGTH, max = UserConstants.PASSWORD_MAX_LENGTH, message = "{user.password.length.valid}")
    private String password;

    /**
     * 手机号(用户名)（必填）
     */
    @NotBlank(message = "手机号不能为空")
    @Parameter(description = "用户密码",required = true)
    private String phoneNumber;

    /**
     * 短信验证码（必填）
     */
//    @NotBlank(message = "验证码不可为空")
    @Parameter(description = "短信验证码",required = true)
    private String smsCode;

    /**
     * 校验验证码
     */
    private String verifyCode;

    /**
     * 校验uuid
     */
    private String verifyUuid;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户类型
     */
    private String userType = BS_USER.getUserType();

}
