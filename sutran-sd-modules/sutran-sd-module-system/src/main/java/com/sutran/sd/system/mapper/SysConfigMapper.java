package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.system.domain.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 参数配置 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysConfigMapper extends BaseMapperPlus<SysConfigMapper, SysConfig, SysConfig> {

}
