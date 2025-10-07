package com.sutran.sd.user.service;

import com.sutran.sd.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户个人信息服务
 *
 * @author SutranSD
 */
public interface IUserProfileService {

    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户信息
     */
    Map<String, Object> getUserProfile(Long userId);

    /**
     * 更新用户个人信息
     * @param userId 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUserProfile(Long userId, SysUser user);

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param avatarFile 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile avatarFile);

    /**
     * 检查手机号是否唯一
     * @param user 用户信息
     * @return 是否唯一
     */
    boolean checkPhoneUnique(SysUser user);

    /**
     * 检查邮箱是否唯一
     * @param user 用户信息
     * @return 是否唯一
     */
    boolean checkEmailUnique(SysUser user);
}
