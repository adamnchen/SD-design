package com.sutran.sd.common.core.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 支付宝账号绑定DTO
 *
 * @author SutranSD
 * @date 2025-10-24
 */
@Data
public class AlipayAccountBindDTO {

    /**
     * 支付宝账号（手机号或邮箱）
     */
    @NotBlank(message = "支付宝账号不能为空")
    @Size(min = 6, max = 100, message = "支付宝账号长度在6到100个字符之间")
    private String alipayAccount;

    /**
     * 支付宝实名姓名
     */
    @NotBlank(message = "实名姓名不能为空")
    @Size(min = 2, max = 50, message = "实名姓名长度在2到50个字符之间")
    private String alipayRealName;
}

