package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;

import java.util.List;

public interface ISysUserAddressService {

    /**
     * 新增地址
     * @param address
     */
    void addAddress(SysAddress address);

    /**
     * 删除地址
     * @param addressId
     */
    void deleteAddress(Long addressId);

    /**
     * 修改地址
     * @param address
     */
    void updateAddress(SysAddress address);

    /**
     * 查询用户地址列表
     * @return
     */
    List<SysAddress> selectAddressList(Long userId);

    /**
     * 获取用户地址
     * @param userId
     * @return
     */
    SysAddress getAddress(Long userId);

    /**
     * 设置默认地址
     * @param addressId
     */
    void setDefaultAddress(Long addressId);


}
