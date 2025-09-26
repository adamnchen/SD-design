package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;

import java.util.List;

public interface ISysUserAddressAreaService {
    /**
     * 省市区县
     * @param parentCode
     * @return
     */
    List<SysAddressArea> selectAreasByParentCode(String parentCode);
}
