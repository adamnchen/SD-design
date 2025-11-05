package com.sutran.sd.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.core.domain.dto.AlipayAccountBindDTO;
import com.sutran.sd.common.core.domain.dto.UserProfileUpdateDTO;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.MimeTypeUtils;
import com.sutran.sd.system.service.ISysOssService;
import com.sutran.sd.system.service.ISysUserService;
import com.sutran.sd.user.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 用户个人信息服务实现
 *
 * @author SutranSD
 */
@Slf4j
@RequiredArgsConstructor
@Service("userProfileService")
public class UserProfileServiceImpl implements IUserProfileService {

    private final ISysUserService userService;
    private final ISysOssService ossService;

    @Override
    public UserProfileVO getClientUserProfile(Long userId) {
        SysUser user = userService.selectUserById(userId);
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public boolean updateClientUserProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        // 验证手机号唯一性
        if (StringUtils.isNotEmpty(updateDTO.getPhonenumber())) {
            SysUser checkUser = new SysUser();
            checkUser.setUserId(userId);
            checkUser.setPhonenumber(updateDTO.getPhonenumber());
            if (!userService.checkPhoneUnique(checkUser)) {
                throw new ServiceException("修改用户失败，手机号码已存在");
            }
        }
        
        // 验证邮箱唯一性
        if (StringUtils.isNotEmpty(updateDTO.getEmail())) {
            SysUser checkUser = new SysUser();
            checkUser.setUserId(userId);
            checkUser.setEmail(updateDTO.getEmail());
            if (!userService.checkEmailUnique(checkUser)) {
                throw new ServiceException("修改用户失败，邮箱账号已存在");
            }
        }

        // 创建更新对象
        SysUser updateUser = new SysUser();
        updateUser.setUserId(userId);
        updateUser.setNickName(updateDTO.getNickName());
        updateUser.setEmail(updateDTO.getEmail());
        updateUser.setPhonenumber(updateDTO.getPhonenumber());
        updateUser.setSex(updateDTO.getSex());
        updateUser.setDescription(updateDTO.getDescription());
        updateUser.setRemark(updateDTO.getRemark());

        return userService.updateUserProfile(updateUser) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userService.selectUserById(userId);
        String userName = user.getUserName();
        String password = user.getPassword();

        if (!BCrypt.checkpw(oldPassword, password)) {
            throw new ServiceException("修改密码失败，旧密码错误");
        }
        if (BCrypt.checkpw(newPassword, password)) {
            throw new ServiceException("新密码不能与旧密码相同");
        }

        return userService.resetUserPwd(userName, BCrypt.hashpw(newPassword)) > 0;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile avatarFile) {
        if (avatarFile.isEmpty()) {
            throw new ServiceException("头像文件不能为空");
        }

        String extension = FileUtil.extName(avatarFile.getOriginalFilename());
        if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
            throw new ServiceException("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
        }

        SysOssVo oss = ossService.upload(avatarFile);
        String avatar = oss.getUrl();

        if (userService.updateUserAvatar(userService.selectUserById(userId).getUserName(), avatar)) {
            return avatar;
        }

        throw new ServiceException("上传图片异常，请联系管理员");
    }

    @Override
    public boolean bindAlipayAccount(Long userId, AlipayAccountBindDTO bindDTO) {
        log.info("[绑定支付宝账号] 用户ID: {}, 账号: {}", userId, bindDTO.getAlipayAccount());

        // 0. 若已绑定，需先解绑再绑定
        SysUser existUser = userService.selectUserById(userId);
        if (existUser == null) {
            throw new ServiceException("用户不存在");
        }
        if (StrUtil.isNotBlank(existUser.getAlipayAccount()) || "1".equals(existUser.getAlipayBindStatus())) {
            throw new ServiceException("已绑定支付宝账号，请先解绑后再绑定");
        }

        // 1. 验证支付宝账号格式
        if (!isValidAlipayAccount(bindDTO.getAlipayAccount())) {
            throw new ServiceException("支付宝账号格式不正确，请输入手机号或邮箱");
        }

        // 2. 验证实名姓名格式
        if (StrUtil.isBlank(bindDTO.getAlipayRealName()) || bindDTO.getAlipayRealName().length() < 2) {
            throw new ServiceException("实名姓名格式不正确，请输入2-50个字符");
        }

        // 3. 更新用户表
        SysUser updateUser = new SysUser();
        updateUser.setUserId(userId);
        updateUser.setAlipayAccount(bindDTO.getAlipayAccount());
        updateUser.setAlipayRealName(bindDTO.getAlipayRealName());
        updateUser.setAlipayBindStatus("1"); // 已绑定

        int result = userService.updateUserProfile(updateUser);
        
        if (result > 0) {
            log.info("[绑定支付宝账号] 成功: 用户ID={}", userId);
            return true;
        }
        
        log.error("[绑定支付宝账号] 失败: 用户ID={}", userId);
        return false;
    }

    @Override
    public boolean unbindAlipayAccount(Long userId) {
        log.info("[解绑支付宝账号] 用户ID: {}", userId);

        // 清空支付宝信息
        SysUser updateUser = new SysUser();
        updateUser.setUserId(userId);
        updateUser.setAlipayAccount("");
        updateUser.setAlipayRealName("");
        updateUser.setAlipayBindStatus("0"); // 未绑定

        int result = userService.updateUserProfile(updateUser);
        
        if (result > 0) {
            log.info("[解绑支付宝账号] 成功: 用户ID={}", userId);
            return true;
        }
        
        log.error("[解绑支付宝账号] 失败: 用户ID={}", userId);
        return false;
    }

    @Override
    public AlipayAccountBindDTO getAlipayAccount(Long userId) {
        SysUser user = userService.selectUserById(userId);
        
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        AlipayAccountBindDTO dto = new AlipayAccountBindDTO();
        
        // 脱敏处理
        if (StrUtil.isNotBlank(user.getAlipayAccount())) {
            dto.setAlipayAccount(maskAlipayAccount(user.getAlipayAccount()));
        }
        
        if (StrUtil.isNotBlank(user.getAlipayRealName())) {
            dto.setAlipayRealName(maskRealName(user.getAlipayRealName()));
        }

        return dto;
    }

    /**
     * 验证支付宝账号格式
     */
    private boolean isValidAlipayAccount(String account) {
        if (StrUtil.isBlank(account)) {
            return false;
        }

        // 手机号格式：11位数字
        String phonePattern = "^1[3-9]\\d{9}$";
        // 邮箱格式
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        return Pattern.matches(phonePattern, account) || Pattern.matches(emailPattern, account);
    }

    /**
     * 脱敏支付宝账号
     */
    private String maskAlipayAccount(String account) {
        if (StrUtil.isBlank(account)) {
            return "";
        }

        // 手机号脱敏：138****8800
        if (account.matches("^1[3-9]\\d{9}$")) {
            return account.substring(0, 3) + "****" + account.substring(7);
        }

        // 邮箱脱敏：u***@example.com
        if (account.contains("@")) {
            int atIndex = account.indexOf("@");
            return account.substring(0, 1) + "***" + account.substring(atIndex);
        }

        return account;
    }

    /**
     * 脱敏真实姓名（只显示姓氏，名字用*替代）
     */
    private String maskRealName(String realName) {
        if (StrUtil.isBlank(realName)) {
            return "";
        }

        if (realName.length() <= 2) {
            return realName.substring(0, 1) + "*";
        }

        return realName.substring(0, 1) + "*" + realName.substring(realName.length() - 1);
    }

}
