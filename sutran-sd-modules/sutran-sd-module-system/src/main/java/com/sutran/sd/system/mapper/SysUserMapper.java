package com.sutran.sd.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.annotation.DataColumn;
import com.sutran.sd.common.annotation.DataPermission;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户表 数据层
 *
 * @author Lion Li
 */
@Mapper
public interface SysUserMapper extends BaseMapperPlus<SysUserMapper, SysUser, SysUser> {

    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUser> selectPageUserList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    List<SysUser> selectUserList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUser> selectAllocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUser> selectUnallocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(@Param("userName") String userName);

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    SysUser selectUserByPhonenumber(String phonenumber);

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    SysUser selectUserByEmail(String email);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(Long userId);

    /**
     * 通过wxOpenId查询用户
     *
     * @param wxOpenId 微信openId
     * @return 用户对象信息
     */
    SysUser selectUserByWxOpenId(String wxOpenId);

    /**
     * 判断手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    @Select("SELECT COUNT(user_id) FROM sys_user WHERE phonenumber=#{phone}")
    boolean isExistPhone(@Param("phone") String phone);

    /**
     * 扣除培训次数
     * @param userId 用户ID
     */
    @Update("UPDATE sys_user SET limit_train_times=limit_train_times-1 WHERE user_id=#{userId} AND limit_train_times IS NOT NULL")
    void deductedTrainTimes(@Param("userId") Long userId);

    /**
     * 扣除绘图图片数量
     * @param userId 用户ID
     */
    @Update("UPDATE sys_user SET limit_draw_num=limit_draw_num-#{num} WHERE user_id=#{userId} AND limit_draw_num IS NOT NULL")
    void deductedDrawNum(@Param("userId") Long userId, @Param("num") int num);

    /**
     * 关闭用户引导
     *
     * @param userId            用户ID
     * @param isCloserGuide     是否关闭引导(1-关闭 0-开启)
     */
    @Update("UPDATE sys_user SET is_close_guide=#{isCloserGuide} WHERE user_id=#{userId}")
    void closeGuide(@Param("userId") Long userId, @Param("isCloserGuide") Integer isCloserGuide);

    @Select("SELECT user_id FROM sys_user WHERE phonenumber=#{phone}")
    String selectUserIdByPhone(@Param("phone") String phone);

    @Update("UPDATE sys_user SET del_flag=#{delFlag} WHERE user_id=#{userId} AND del_flag='2'")
    int recoverDel(@Param("userId") Long userId, @Param("delFlag") String delFlag);
}
