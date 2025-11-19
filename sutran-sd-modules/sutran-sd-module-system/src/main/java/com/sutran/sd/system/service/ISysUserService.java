package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.vo.UserPublicInfoVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.system.domain.bo.SysUserMemberBo;
import com.sutran.sd.system.domain.vo.UserBaseVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户 业务层
 *
 * @author Lion Li
 */
public interface ISysUserService {

    /**
     * 分页查询用户列表
     *
     * @param user      用户信息
     * @param pageQuery 分页查询对象
     * @return 用户信息集合信息
     */
    TableDataInfo<SysUser> selectPageUserList(SysUser user, PageQuery pageQuery);

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    List<SysUser> selectUserList(SysUser user);

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user      用户信息
     * @param pageQuery 分页查询对象
     * @return 用户信息集合信息
     */
    TableDataInfo<SysUser> selectAllocatedList(SysUser user, PageQuery pageQuery);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user      用户信息
     * @param pageQuery 分页查询对象
     * @return 用户信息集合信息
     */
    TableDataInfo<SysUser> selectUnallocatedList(SysUser user, PageQuery pageQuery);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 通过手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户对象信息
     */
    SysUser selectUserByPhoneNumber(String phoneNumber);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(Long userId);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserRoleGroup(String userName);

    /**
     * 根据用户ID查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserPostGroup(String userName);

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkUserNameUnique(SysUser user);

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkPhoneUnique(SysUser user);

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkEmailUnique(SysUser user);

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    void checkUserAllowed(SysUser user);

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    void checkUserDataScope(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int insertUser(SysUser user);

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean registerUser(SysUser user);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUser(SysUser user);

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    void insertUserAuth(Long userId, Long[] roleIds);

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserStatus(SysUser user);

    /**
     * 恢复已删除的指定用户
     * @param userId    用户ID
     * @param delFlag   删除标志（0代表存在 2代表删除）
     * @return 结果
     */
    int recoverDel(Long userId, String delFlag);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserProfile(SysUser user);

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    boolean updateUserAvatar(String userName, String avatar);

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    int resetPwd(SysUser user);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(String userName, String password);

    /**
     * 忘记密码
     *
     * @param phone 手机号
     * @param password 密码
     * @return 结果
     */
    int forgetPwd(String phone, String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    int deleteUserByIds(Long[] userIds);

    /**
     * 当前用户是否关注了微信公众号
     * @param userId 用户ID
     * @return 是否关注
     */
    boolean isFollowWxMp(Long userId);

    /**
     * 关闭用户引导
     *
     * @param userId            用户ID
     * @param isCloserGuide     是否关闭引导(1-关闭 0-开启)
     */
    void closeGuide(Long userId, Integer isCloserGuide);

    /**
     * 授权用户会员
     *
     * @param bo            授权用户会员实体类
     * @param payMember     会员信息
     * @param now           当前时间
     */
    void insertAuthMember(SysUserMemberBo bo, PayMember payMember, Date now);

    /**
     * 查询可分享人员信息列表
     * @param phoneNumber   手机好
     * @param nickName      昵称
     * @return 人员列表
     */
    List<UserBaseVo> selectShareUserListByPhoneNumberOrNickName(String phoneNumber, String nickName);

    /**
     * 根据用户ID查询用户公开信息（不包含敏感信息）
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    UserPublicInfoVO getUserPublicInfo(Long userId);

     /**
     * 根据用户ID查询用户绑定的微信openId
     * @param userId    用户ID
     * @return          微信openId
     */
    String selectOpenIdByUserId(Long userId);

    /**
     * 查询所有管理员用户的openId
     * @return 管理员用户openId列表
     */
    List<Map<String, String>> selectAdminUserOpenId();

    /**
     * 根据用户ID列表查询用户信息列表
     * @param userIds   用户ID列表
     * @return          用户信息列表
     */
    List<SysUser> selectUserListByIds(List<Long> userIds);

}
