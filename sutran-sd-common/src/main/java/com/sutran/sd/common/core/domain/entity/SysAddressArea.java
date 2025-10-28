package com.sutran.sd.common.core.domain.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 【SysAddressArea】
 * 系统用户地址行政区划实体类
 * 对应数据库表：sys_user_address_area
 * @author Administrator
 */
@Data
@TableName("sys_address")
public class SysAddressArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID (对应表的 id BIGINT)
     */
    private Long id;

    /**
     * 行政区划代码 (对应表的 area_code VARCHAR)
     * 核心字段，用于关联父级和查询子级
     */
    private String areaCode;

    /**
     * 区域名称 (对应表的 area_name VARCHAR)
     */
    private String areaName;

    /**
     * 父级区域代码 (对应表的 parent_code VARCHAR)
     * 用于级联查询
     */
    private String parentCode;

    /**
     * 区域级别 (对应表的 area_level TINYINT)
     * 1=省, 2=市, 3=区/县, 4=街道/乡镇
     */
    private Integer areaLevel;

    /**
     * 邮政编码 (对应表的 zip_code VARCHAR)
     */
    private String zipCode;

    /**
     * 子级区域列表 (用于返回 JSON 嵌套数据时使用)
     */
    @TableField(exist = false)
    private java.util.List<SysAddressArea> children;

}
