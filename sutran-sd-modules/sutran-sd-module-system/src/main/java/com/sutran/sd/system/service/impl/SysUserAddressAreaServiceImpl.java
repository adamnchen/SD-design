package com.sutran.sd.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.system.mapper.SysAddressAreaMapper;
import com.sutran.sd.system.service.ISysUserAddressAreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 省市区县地址业务层处理
 */
@Slf4j
@Service
public class SysUserAddressAreaServiceImpl

        extends ServiceImpl<SysAddressAreaMapper, SysAddressArea>
        implements ISysUserAddressAreaService {

    @Override
    public List<SysAddressArea> selectAreasByParentCode(String parentCode) {

        QueryWrapper<SysAddressArea> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("parent_code", parentCode);

        queryWrapper.orderByAsc("area_code");


        List<SysAddressArea> areaList = this.list(queryWrapper);

        return areaList;
    }
}
