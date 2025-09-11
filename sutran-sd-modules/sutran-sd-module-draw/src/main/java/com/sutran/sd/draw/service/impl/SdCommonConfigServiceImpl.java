package com.sutran.sd.draw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.draw.domain.SdCommonConfig;
import com.sutran.sd.draw.mapper.SdCommonConfigMapper;
import com.sutran.sd.draw.service.SdCommonConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zj
 * @date 2024-04-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdCommonConfigServiceImpl implements SdCommonConfigService {

    private final SdCommonConfigMapper baseMapper;

    /**
     * 新增SD配置数据
     * @param sdCommonConfig 配置数据
     */
    @Override
    public void insert(SdCommonConfig sdCommonConfig) {
        baseMapper.insert(sdCommonConfig);
    }

    /**
     * 更新SD配置数据
     * @param sdCommonConfig 配置数据
     */
    @Override
    public void update(SdCommonConfig sdCommonConfig) {
        baseMapper.updateById(sdCommonConfig);
    }

    /**
     * 查询SD配置数据
     * @return 配置数据
     */
    @Override
    public SdCommonConfig selectOne() {
        LambdaQueryWrapper<SdCommonConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SdCommonConfig::getId).last("limit 1");
        return baseMapper.selectOne(wrapper);
    }

}
