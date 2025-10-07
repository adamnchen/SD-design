package com.sutran.sd.user.service;

import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;

import java.util.List;

/**
 * 用户地址管理服务
 *
 * @author SutranSD
 */
public interface IUserAddressService {

    /**
     * 新增地址
     * @param address 地址信息
     */
    void addAddress(SysAddress address);

    /**
     * 删除地址
     * @param addressId 地址ID
     */
    void deleteAddress(Long addressId);

    /**
     * 修改地址
     * @param address 地址信息
     */
    void updateAddress(SysAddress address);

    /**
     * 查询用户地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<SysAddress> selectAddressList(Long userId);

    /**
     * 获取用户地址详情
     * @param addressId 地址ID
     * @return 地址信息
     */
    SysAddress getAddress(Long addressId);

    /**
     * 设置默认地址
     * @param addressId 地址ID
     */
    void setDefaultAddress(Long addressId);

    /**
     * 获取省市区列表
     * @param parentCode 父级代码
     * @return 区域列表
     */
    List<SysAddressArea> getAreaList(String parentCode);
}
