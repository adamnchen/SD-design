package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdCommonConfig;

/**
 * SD绘图通用设置
 * @author zj
 * @date 2024-04-13
 */
public interface SdCommonConfigService {

    /**
     * 新增SD配置数据
     * @param sdCommonConfig 配置数据
     */
    void insert(SdCommonConfig sdCommonConfig);

    /**
     * 更新SD配置数据
     * @param sdCommonConfig 配置数据
     */
    void update(SdCommonConfig sdCommonConfig);

    /**
     * 查询SD配置数据
     * @return 配置数据
     */
    SdCommonConfig selectOne();
}

