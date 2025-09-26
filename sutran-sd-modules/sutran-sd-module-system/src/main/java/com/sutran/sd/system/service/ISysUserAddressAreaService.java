package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;



public interface ISysUserAddressAreaService {
    /**
     * 省市区县
     * @param parentCode
     * @return
     */
    @Cacheable(value = "sys:address:area", key = "#parentCode", unless = "#result == null or #result.isEmpty()")
    List<SysAddressArea> selectAreasByParentCode(String parentCode);

    void clearAreaCache(String parentCode);
}
