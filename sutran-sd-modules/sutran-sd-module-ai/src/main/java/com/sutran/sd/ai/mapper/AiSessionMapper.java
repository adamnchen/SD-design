package com.sutran.sd.ai.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.ai.entity.AiMsgSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author zj
 * @date 2024-04-13
 */
@Mapper
public interface AiSessionMapper extends BaseMapperPlus<AiSessionMapper, AiMsgSession, AiMsgSession> {
    @Update("update ai_msg_session set name=#{name},order_num=#{orderNum} where id=#{id} AND crt_user_id=#{userId}")
    void updateSession(@Param("id") String id, @Param("name") String name, @Param("orderNum") Integer orderNum, @Param("userId") long userId);
}
