package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdFlow;
import com.sutran.sd.draw.domain.pojo.ComfyWorkFlow;

/**
 * SD绘图 || 工作流(SdFlow)表服务接口
 *
 * @author makejava
 * @since 2025-09-07 23:17:31
 */
public interface SdFlowService {
    /**
     * 新增数据
     * @param sdFlow 实例对象
     */
    void inert(SdFlow sdFlow);

    /**
     * 根据ID更新数据
     * @param sdFlow 实例对象
     */
    void updateById(SdFlow sdFlow);

    /**
     * 根据ID删除数据
     * @param id 主键
     */
    void deleteById(Long id);

    /**
     * 根据ID查询单条数据
     * @param id 主键
     * @return 实例对象
     */
    SdFlow findById(Long id);

    /**
     * 根据模型类型获取工作流
     * @param modelType 模型类型
     * @return 工作流
     */
    SdFlow getFlow(String modelType);
}
