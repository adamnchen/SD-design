package com.sutran.sd.ai.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.ai.entity.AiMsgHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface AiMapper extends BaseMapperPlus<AiMapper, AiMsgHistory, AiMsgHistory> {
}
