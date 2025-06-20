package com.sutran.sd.sdapi.modules.system;

import com.sutran.sd.sdapi.modules.system.entity.SdCommonConfig;

/**
 * @author zj
 * @date 2024-04-13
 */
public interface SdCommonConfigService {

    /**
     * 查询SD配置数据
     * @return 配置数据
     */
    SdCommonConfig selectOne();
}

