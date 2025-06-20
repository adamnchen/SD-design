package com.sutran.sd.common.core.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 第三方加密登录
 * @author zj
 * @date 2024-09-23
 */
@Data
@Accessors(chain = true)
public class ThirdLogin implements Serializable {
    /**
     * 加密登录参数
     */
    private String data;
}
