package com.sutran.sd.sdapi.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelLogMapper extends BaseMapperPlus<SdUserModelLogMapper, SdUserModelLog, SdUserModelLog> {
    void insertEntity(SdUserModelLog modelLog);
}
