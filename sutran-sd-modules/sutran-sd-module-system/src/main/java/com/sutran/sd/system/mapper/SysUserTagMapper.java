package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.domain.dto.UserTagDTO; // 列表返回的DTO
import com.sutran.sd.common.core.domain.entity.SysUserTag; // 核心实体
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户标签数据层
 */
@Mapper

public interface SysUserTagMapper
        extends BaseMapperPlus<SysUserTagMapper, SysUserTag, UserTagDTO> {

}
