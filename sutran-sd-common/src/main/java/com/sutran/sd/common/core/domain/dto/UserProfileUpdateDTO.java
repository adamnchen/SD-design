package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 客户端用户个人信息更新DTO
 * 只包含客户端用户可以修改的字段
 *
 * @author SutranSD
 */
@Data
public class UserProfileUpdateDTO {

    /**
     * 用户昵称
     */
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过{max}个字符")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Pattern(regexp = "^[0-2]$", message = "性别值不正确")
    private String sex;

    /**
     * 个人简介
     */
    @Size(min = 0, max = 255, message = "个人简介长度不能超过{max}个字符")
    private String description;

    /**
     * 备注
     */
    @Size(min = 0, max = 500, message = "备注长度不能超过{max}个字符")
    private String remark;
}
