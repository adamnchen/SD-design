package com.sutran.sd.user.service;

import com.sutran.sd.common.core.domain.dto.AlipayAccountBindDTO;
import com.sutran.sd.common.core.domain.dto.UserProfileUpdateDTO;
import com.sutran.sd.common.core.domain.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人信息服务
 *
 * @author SutranSD
 */
public interface IUserProfileService {

    /**
     * 获取客户端用户个人信息
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserProfileVO getClientUserProfile(Long userId);

    /**
     * 更新客户端用户个人信息
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    boolean updateClientUserProfile(Long userId, UserProfileUpdateDTO updateDTO);

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
     * 绑定支付宝账号
     * @param userId 用户ID
     * @param bindDTO 绑定信息
     * @return 绑定结果
     */
    boolean bindAlipayAccount(Long userId, AlipayAccountBindDTO bindDTO);

    /**
     * 解绑支付宝账号
     * @param userId 用户ID
     * @return 解绑结果
     */
    boolean unbindAlipayAccount(Long userId);

    /**
     * 获取支付宝账号信息
     * @param userId 用户ID
     * @return 支付宝账号信息
     */
    AlipayAccountBindDTO getAlipayAccount(Long userId);

}
