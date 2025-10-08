package com.sutran.sd.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
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

}
