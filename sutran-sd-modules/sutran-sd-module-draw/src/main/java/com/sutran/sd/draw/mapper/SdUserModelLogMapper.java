package com.sutran.sd.draw.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdUserModelLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelLogMapper extends BaseMapperPlus<SdUserModelLogMapper, SdUserModelLog, SdUserModelLog> {
    /**
     * 插入模型日志
     * @param modelLog 模型日志
     */
    void insertEntity(SdUserModelLog modelLog);
}
