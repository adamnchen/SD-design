package com.sutran.sd.pay.mapper;

import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.common.core.domain.entity.PayMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author zj
 * @date 2025年08月21日 11:03
 */
@Mapper
public interface PayMemberMapper extends BaseMapperPlus<PayMemberMapper, PayMember, PayMember> {
    /**
     * 获取会员配置时长
     * @param id 会员配置ID
     * @return 会员配置时长
     */
    @Select("select duration from pay_member where id = #{id}")
    Integer selectDurationById(@Param("id") Long id);

    /**
     * 判断会员配置下是否有用户
     * @param id 会员配置ID
     * @return 是否有用户
     */
    @Select("select count(1) from sys_user_member where member_id = #{id}")
    boolean hasUser(@Param("id") String id);
}
