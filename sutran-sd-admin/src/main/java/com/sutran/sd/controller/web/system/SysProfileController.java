package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.MimeTypeUtils;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.ISysOssService;
import com.sutran.sd.system.service.ISysUserService;
import com.sutran.sd.user.service.IUserAddressService;
import com.sutran.sd.user.service.IUserTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理系统 - 管理员个人信息管理 + 用户信息管理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile")
@SaCheckLogin
public class SysProfileController extends BaseController {

    private final ISysUserService userService;
    private final ISysOssService iSysOssService;
    private final IUserAddressService addressService;
    private final IUserTagService tagService;

    /**
     * 个人信息
     */
    @GetMapping
    public R<Map<String, Object>> profile() {
        SysUser user = userService.selectUserById(getUserId());
        Map<String, Object> ajax = new HashMap<>();
        ajax.put("user", user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(user.getUserName()));
        ajax.put("postGroup", userService.selectUserPostGroup(user.getUserName()));
        return R.ok(ajax);
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> updateProfile(@RequestBody SysUser user) {
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        user.setUserId(getUserId());
        user.setUserName(null);
        user.setPassword(null);
        user.setAvatar(null);
        user.setDeptId(null);
        if (userService.updateUserProfile(user) > 0) {
            return R.ok();
        }
        return R.fail("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     *
     * @param newPassword 新密码
     * @param oldPassword 旧密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public R<Void> updatePwd(String oldPassword, String newPassword) {
        SysUser user = userService.selectUserById(LoginHelper.getUserId());
        String userName = user.getUserName();
        String password = user.getPassword();
        if (!BCrypt.checkpw(oldPassword, password)) {
            return R.fail("修改密码失败，旧密码错误");
        }
        if (BCrypt.checkpw(newPassword, password)) {
            return R.fail("新密码不能与旧密码相同");
        }

        if (userService.resetUserPwd(userName, BCrypt.hashpw(newPassword)) > 0) {
            return R.ok();
        }
        return R.fail("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     *
     * @param avatarfile 用户头像
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        Map<String, Object> ajax = new HashMap<>();
        if (!avatarfile.isEmpty()) {
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            SysOssVo oss = iSysOssService.upload(avatarfile);
            String avatar = oss.getUrl();
            if (userService.updateUserAvatar(getUsername(), avatar)) {
                ajax.put("imgUrl", avatar);
                return R.ok(ajax);
            }
        }
        return R.fail("上传图片异常，请联系管理员");
    }
}
