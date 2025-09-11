package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.mapper.SdGpuPoolMapper;
import com.sutran.sd.draw.service.SdGpuPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * GPU卡池管理
 * @author zj
 * @date 2024-04-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdGpuPoolServiceImpl implements SdGpuPoolService {

    private final SdGpuPoolMapper baseMapper;

    /**
     * 获取GPU卡列表
     * @param type  类型 0绘图卡池，1训练卡池
     * @return  GPU卡列表
     */
    @Override
    public List<SdGpuPool> getList(Integer type) {
        return baseMapper.selectList(new LambdaQueryWrapper<SdGpuPool>().eq(type!=null,SdGpuPool::getType, type));
    }

    /**
     * 根据ID获取GPU卡
     * @param id  ID
     * @return  GPU卡
     */
    @Override
    public SdGpuPool selectById(String id) {
        return baseMapper.selectById(id);
    }

    /**
     * 从指定类型的GPU卡池中随机获取一个可用的GPU卡
     * @param type  类型 0绘图卡池，1训练卡池
     * @return  GPU卡
     */
    @Override
    public SdGpuPool selectRandomEnableOfOneGpu(int type) {
        LambdaQueryWrapper<SdGpuPool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SdGpuPool::getIsEnable,1).eq(SdGpuPool::getType,type);
        List<SdGpuPool> pools = baseMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(pools)) {
            return null;
        }
        return pools.get(new Random().nextInt(pools.size()));
    }

    /**
     * 新增GPU卡
     * @param sdGpuPool  GPU卡
     * @return  结果
     */
    @Override
    public boolean insert(SdGpuPool sdGpuPool) {
        return baseMapper.insert(sdGpuPool)>0;
    }
}
