package com.sutran.sd.sdapi.modules.system;

import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;

import java.util.List;

/**
 * @author zj
 * @date 2024-04-13
 */
public interface SdGpuPoolService {

    /**
     * 获取GPU卡列表
     * @param type  类型 0绘图卡池，1训练卡池
     * @return  GPU池列表
     */
    List<SdGpuPool> getList(Integer type);

    /**
     * 根据ID获取GPU卡
     * @param id  ID
     * @return  GPU池
     */
    SdGpuPool selectById(String id);

    /**
     * 从指定类型的GPU卡池中随机获取一个可用的GPU卡
     * @param type  类型 0绘图卡池，1训练卡池
     * @return  GPU池
     */
    SdGpuPool selectRandomEnableOfOneGpu(int type);

    /**
     * 新增GPU卡
     * @param sdGpuPool  GPU卡
     * @return  结果
     */
    boolean insert(SdGpuPool sdGpuPool);

}
