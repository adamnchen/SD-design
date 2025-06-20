package com.sutran.sd.sdapi.modules.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.sdapi.modules.system.entity.SdCommonConfig;
import com.sutran.sd.sdapi.mapper.SdCommonConfigMapper;
import com.sutran.sd.sdapi.modules.system.SdCommonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zj
 * @date 2024-04-13
 */
@Slf4j
@Service("SdCommonConfigService")
public class SdCommonConfigServiceImpl implements SdCommonConfigService {

    @Resource
    private SdCommonConfigMapper baseMapper;


    @Override
    public SdCommonConfig selectOne() {
        return baseMapper.selectOne(new LambdaQueryWrapper<>());
    }

}
