package com.sutran.sd.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.system.mapper.SysUserAddressMapper;
import com.sutran.sd.system.service.ISysUserAddressAreaService;
import com.sutran.sd.system.service.ISysUserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserAddressServiceImpl  implements ISysUserAddressService {


    private final SysUserAddressMapper addressMapper;
    private final ISysUserAddressAreaService addressAreaService;



    /**
     * 添加地址
     * @param address
     */
    @Override
    public void addAddress(SysAddress address) {
        // 使用优化后的方法获取用户ID
        Long userId = LoginHelper.getUserId();

        // 自动设置用户ID，防止前端篡改
        address.setUserId(userId);

        // 如果是新增的第一个地址，自动设置为默认
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<SysAddress>().eq(SysAddress::getUserId, userId));
        if (count == null || count == 0) {
            address.setIsDefault(1);
        }

        addressMapper.insert(address);
    }

    @Override
    public void deleteAddress(Long addressId) {
        Long userId = LoginHelper.getUserId();
        LambdaQueryWrapper<SysAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAddress::getId, addressId)
            .eq(SysAddress::getUserId, userId);

        int rows = addressMapper.delete(wrapper);

        if (rows == 0) {
            // 0 行受影响，可能是地址不存在，或地址不属于当前用户
            throw new ServiceException("地址不存在或无权删除此地址。");
        }
    }

    @Override
    public void updateAddress(SysAddress address) {
        Long userId = LoginHelper.getUserId();
        // 核心安全校验：确保要更新的地址属于当前用户
        LambdaUpdateWrapper<SysAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysAddress::getId, address.getId())
            .eq(SysAddress::getUserId, userId); // 必须添加用户ID条件

        // 确保用户不能手动修改地址归属的 userId
        address.setUserId(userId);

        int rows = addressMapper.update(address, wrapper);

        if (rows == 0) {
            throw new ServiceException("地址不存在或无权修改此地址。");
        }
    }


    @Override
    public List<SysAddress> selectAddressList(Long userId) {
        // 1. 查询原始地址列表，包含省、市、区的编码
        List<SysAddress> addressList = addressMapper.selectAddressList(userId);

        if (addressList == null || addressList.isEmpty()) {
            return Collections.emptyList(); // 返回一个空列表，而不是 null
        }

        // 2. 收集所有地址中不重复的 area codes
        Set<String> areaCodes = new HashSet<>();
        for (SysAddress address : addressList) {
            if (address.getProvince() != null) {
                areaCodes.add(address.getProvince());
            }
            if (address.getCity() != null) {
                areaCodes.add(address.getCity());
            }
            if (address.getCounty() != null) {
                areaCodes.add(address.getCounty());
            }
        }

        // 如果没有任何编码需要查询，直接返回
        if (areaCodes.isEmpty()) {
            return addressList;
        }

        // 3. 一次性从数据库查询所有相关的区划信息
        List<SysAddressArea> areaList = addressAreaService.list(
            new LambdaQueryWrapper<SysAddressArea>().in(SysAddressArea::getAreaCode, areaCodes)
        );

        // 4. 将查询结果转换为 Map<AreaCode, AreaName> 以便快速查找
        Map<String, String> areaNameMap = areaList.stream()
            .collect(Collectors.toMap(SysAddressArea::getAreaCode, SysAddressArea::getAreaName, (k1, k2) -> k1));

        // 5. 遍历地址列表，使用 Map 设置省、市、区的中文名称
        for (SysAddress address : addressList) {
            if (address.getProvince() != null) {
                address.setProvinceName(areaNameMap.getOrDefault(address.getProvince(), ""));
            }
            if (address.getCity() != null) {
                address.setCityName(areaNameMap.getOrDefault(address.getCity(), ""));
            }
            if (address.getCounty() != null) {
                address.setCountyName(areaNameMap.getOrDefault(address.getCounty(), ""));
            }
        }

        return addressList;
    }

    @Override
    public SysAddress getAddress(Long addressId) {
        Long userId = LoginHelper.getUserId();

        // 核心安全校验：确保只查询属于当前用户的地址
        LambdaQueryWrapper<SysAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAddress::getId, addressId)
            .eq(SysAddress::getUserId, userId);

        SysAddress address = addressMapper.selectOne(wrapper);

        if (address == null) {
            throw new ServiceException("地址不存在");
        }
        // 获取省份名称
        addressAreaService.selectAreasByParentCode(address.getProvince())
            .stream().findFirst().ifPresent(sysAddressArea -> address.setProvinceName(sysAddressArea.getAreaName()));
        // 获取城市名称
        addressAreaService.selectAreasByParentCode(address.getCity())
            .stream().findFirst().ifPresent(sysAddressArea -> address.setCityName(sysAddressArea.getAreaName()));
        // 获取区县名称
        addressAreaService.selectAreasByParentCode(address.getCounty())
            .stream().findFirst().ifPresent(sysAddressArea -> address.setCountyName(sysAddressArea.getAreaName()));
        addressAreaService.selectAreasByParentCode(address.getAddressName())
            .stream().findFirst().ifPresent(sysAddressArea -> address.setCountyName(sysAddressArea.getAreaName()));

        return address;
    }

    /**
     * 设置默认地址 【安全优化：校验所有权】
     * @param addressId 目标地址ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId) {
        Long userId = LoginHelper.getUserId();

        // 1. 核心安全校验：先确认目标地址是否属于当前用户
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<SysAddress>()
            .eq(SysAddress::getId, addressId)
            .eq(SysAddress::getUserId, userId));

        if (count == null || count == 0) {
            throw new ServiceException("目标地址不存在或不属于当前用户。");
        }
        // 2. 将用户的所有现有默认地址设置为非默认 (is_default = 0)
        addressMapper.update(null, new LambdaUpdateWrapper<SysAddress>()
            .set(SysAddress::getIsDefault, 0)
            .eq(SysAddress::getUserId, userId)
            .eq(SysAddress::getIsDefault, 1)); // 优化：只更新当前是默认的地址

        // 3. 将目标地址设置为默认 (is_default = 1)
        addressMapper.update(null, new LambdaUpdateWrapper<SysAddress>()
            .set(SysAddress::getIsDefault, 1)
            .eq(SysAddress::getId, addressId));

    }



}



