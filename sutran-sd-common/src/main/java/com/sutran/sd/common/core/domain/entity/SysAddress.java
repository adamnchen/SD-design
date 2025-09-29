package com.sutran.sd.common.core.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sutran.sd.common.core.domain.BaseEntity;
import com.sutran.sd.common.xss.Xss;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 用户地址表 sys_address
 *
 * @author chen shan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_address")
public class SysAddress extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 用户名
     */
    @Xss(message = "收件人姓名不能包含脚本字符")
    @NotBlank(message = "收件人姓名不能为空")
    @Size(min = 0, max = 255, message = "收件人姓名长度不能超过{max}个字符")
    @TableField(value = "name")
    private String name;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    @Size(min = 0, max = 64, message = "省份长度不能超过{max}个字符")
    @TableField(value = "province")
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    @Size(min = 0, max = 64, message = "城市长度不能超过{max}个字符")
    @TableField(value = "city")
    private String city;

    /**
     * 区 县
     */
    @NotBlank(message = "区县不能为空")
    @Size(min = 0, max = 64, message = "区县长度不能超过{max}个字符")
    @TableField(value = "county")
    private String county;

    @TableField(exist = false)
    private String provinceName;

    @TableField(exist = false)
    private String cityName;

    @TableField(exist = false)
    private String countyName;

    /**
     * 用户具体地址
     */
    @Xss(message = "详细地址不能包含脚本字符")
    @NotBlank(message = "详细地址不能为空")
    @Size(min = 0, max = 500, message = "详细地址长度不能超过{max}个字符")
    @TableField(value = "address")
    private String address;
    /**
     * 手机号码
     */
    @Xss(message = "手机号不能包含脚本字符")
    @NotBlank(message = "手机号不能为空")
    @Size(min = 0, max = 20, message = "手机号不能超过11个数字")
    @TableField(value = "phonenumber")
    private String phonenumber;

    /**
     * 是否默认地址
     */
    @TableField(value = "is_default")
    private Integer isDefault;

    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private Date updateTime;


}
