package com.sutran.sd.draw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.draw.domain.SdFlow;
import com.sutran.sd.draw.domain.pojo.ComfyWorkFlow;
import com.sutran.sd.draw.mapper.SdFlowMapper;
import com.sutran.sd.draw.service.SdFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SD绘图 || 工作流(SdFlow)表服务实现类
 *
 * @author makejava
 * @since 2025-09-07 23:17:31
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SdFlowServiceImpl implements SdFlowService {
    private final SdFlowMapper sdFlowMapper;

    @Override
    public void inert(SdFlow sdFlow) {
        sdFlowMapper.insert(sdFlow);
    }

    @Override
    public void updateById(SdFlow sdFlow) {
        sdFlowMapper.updateById(sdFlow);
    }

    @Override
    public void deleteById(Long id) {
        sdFlowMapper.deleteById(id);
    }

    @Override
    public SdFlow findById(Long id) {
        return sdFlowMapper.selectById(id);
    }

    /**
     * 获取工作流
     * @param modelType 模型类型
     * @return 工作流
     */
    @Override
    public SdFlow getFlow(String modelType) {
        return sdFlowMapper.selectOne(new LambdaQueryWrapper<SdFlow>().eq(SdFlow::getModelType, modelType).eq(SdFlow::getIsOpen, 1).last("limit 1"));
    }
}

