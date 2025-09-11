package com.sutran.sd.draw.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdChannelData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 渠道消息推送
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface SdChannelDataMapper extends BaseMapperPlus<SdChannelDataMapper, SdChannelData, SdChannelData> {
}
