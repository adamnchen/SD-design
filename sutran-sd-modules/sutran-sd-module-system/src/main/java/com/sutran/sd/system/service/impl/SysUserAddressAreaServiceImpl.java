package com.sutran.sd.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.system.mapper.SysAddressAreaMapper;
import com.sutran.sd.system.service.ISysUserAddressAreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 省市区县地址业务层处理
 * @author Administrator
 */
@Slf4j
@Service
public class SysUserAddressAreaServiceImpl extends ServiceImpl<SysAddressAreaMapper, SysAddressArea> implements ISysUserAddressAreaService {

    @Override
    public List<SysAddressArea> selectAreasByParentCode(String parentCode) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysAddressArea>().eq(SysAddressArea::getParentCode, parentCode).orderByAsc(SysAddressArea::getAreaCode));
    }
    @Override
    @CacheEvict(value = "sys:address:area", key = "#parentCode")
    public void clearAreaCache(String parentCode) {
        log.info("【缓存清除】已触发清除地址缓存：Key={}", "sys:address:area:" + parentCode);
    }
}
