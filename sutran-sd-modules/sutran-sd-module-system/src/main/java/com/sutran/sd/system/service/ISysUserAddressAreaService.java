package com.sutran.sd.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;

import java.util.List;

/**
 * 行政区划 服务层接口
 * @author Administrator
 */
public interface ISysUserAddressAreaService extends IService<SysAddressArea> {

    /**
     * 根据父级编码查询省市区县列表 (带缓存)
     * @param parentCode 父级编码
     * @return 子级列表
     */
    List<SysAddressArea> selectAreasByParentCode(String parentCode);

    /**
     * 清除指定父级编码的区域缓存
     * @param parentCode 父级编码
     */
    void clearAreaCache(String parentCode);
}
