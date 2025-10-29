package com.sutran.sd.system.mapper;

import com.sutran.sd.common.core.domain.entity.SysUserMember;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2025年08月24日 23:07
 */
@Mapper
public interface SysUserMemberMapper extends BaseMapperPlus<SysUserMemberMapper, SysUserMember, SysUserMember> {

    /**
     * 获取当前用户已购买且处于生效中的会员ID
     * @param userId 用户ID
     * @param now 当前时间
     * @return 会员ID
     */
    @Select("SELECT A.member_id FROM sys_user_member AS A LEFT JOIN pay_member AS B ON A.member_id = B.id " +
        "WHERE A.user_id = #{userId} and A.status = 1 AND A.end_time >= #{now} ORDER BY B.level DESC LIMIT 1")
    String selectMemberIdByUserId(@Param("userId") Long userId, @Param("now") Date now);

    /**
     * 获取当前用户已购买且处于生效中的会员信息
     * @param userIds 用户ID列表
     * @param now 当前时间
     * @return 会员信息
     */
    @MapKey("userId")
    List<SysUserMember> selectMemberInfoByUserIds(@Param("userIds") List<Long> userIds, @Param("now") Date now);

    /**
     * 获取当前用户已购买且处于生效中的会员绘图次数
     * @param userId 用户ID
     * @param now 当前时间
     * @return 会员绘图次数
     */
    @Select("SELECT (A.limit_draw_num-A.use_draw_num) AS drawNum FROM sys_user_member AS A WHERE A.user_id = #{userId} and A.status = 1 AND A.end_time >= #{now} ORDER BY A.id DESC LIMIT 1")
    Integer selectDrawNumById(@Param("userId") Long userId, @Param("now") Date now);

    /**
     * 获取当前用户已购买且处于生效中的会员训练次数
     * @param userId 用户ID
     * @param now 当前时间
     * @return 会员训练次数
     */
    @Select("SELECT (A.limit_train_times-A.use_train_times) AS trainTimes FROM sys_user_member AS A WHERE A.user_id = #{userId} and A.status = 1 AND A.end_time >= #{now} ORDER BY A.id DESC LIMIT 1")
    Integer selectTrainTimesById(@Param("userId") Long userId, @Param("now") Date now);

    /**
     * 更新会员状态
     * @param sysUserMember 会员信息
     */
    @Update("UPDATE sys_user_member SET status = #{status}, remark = #{remark} WHERE id = #{id}")
    void updateStatusById(SysUserMember sysUserMember);

    /**
     * 已会使用训练次数+1
     * @param userId 用户ID
     * @param now 当前时间
     */
    @Update("UPDATE sys_user_member SET use_train_times = use_train_times + 1 WHERE user_id = #{userId} and status = 1 AND end_time >= #{now} ORDER BY id DESC LIMIT 1")
    void deductedTrainTimes(@Param("userId") Long userId, @Param("now") Date now);

    /**
     * 已使用训练次数-1
     * @param userId 用户ID
     * @param now 当前时间
     */
    @Update("UPDATE sys_user_member SET use_train_times = use_train_times - 1 WHERE user_id = #{userId} and status = 1 AND end_time >= #{now} ORDER BY id DESC LIMIT 1")
    void returnedTrainTimes(@Param("userId") Long userId, @Param("now") Date now);

    /**
     * 已会使用绘图次数+1
     * @param userId 用户ID
     * @param num 绘图次数
     * @param now 当前时间
     */
    @Update("UPDATE sys_user_member SET use_draw_num = use_draw_num + #{num} WHERE user_id = #{userId} and status = 1 AND end_time >= #{now} ORDER BY id DESC LIMIT 1")
    void deductedDrawNum(@Param("userId") Long userId, @Param("num") int num, @Param("now") Date now);

    /**
     * 已会使用绘图次数-1
     *
     * @param userId 用户ID
     * @param num    绘图次数
     * @param now    当前时间
     */
    @Update("UPDATE sys_user_member SET use_draw_num = use_draw_num - #{num} WHERE user_id = #{userId} and status = 1 AND end_time >= #{now} ORDER BY id DESC LIMIT 1")
    void returnedDrawNum(@Param("userId") Long userId, @Param("num") int num, @Param("now") Date now);
}
