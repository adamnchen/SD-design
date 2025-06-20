package com.sutran.sd.system.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zj
 * @date 2024-04-02
 */
@Data
public class SysUserForgetPwd implements Serializable {
    private String phone;
    private String code;
    private String newPwd;
}
