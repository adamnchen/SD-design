package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.SysUser;

import com.sutran.sd.common.core.domain.vo.UserPublicInfoVO;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.MimeTypeUtils;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.ISysOssService;
import com.sutran.sd.design.service.ISdProofingInvitationService;
import com.sutran.sd.system.service.ISysUserService;
import com.sutran.sd.system.service.ISysUserTagService;
import com.sutran.sd.user.service.IUserAddressService;
import com.sutran.sd.user.service.IUserTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;

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
    private final ISdProofingInvitationService invitationService;
    private final ISysUserTagService sysUserTagService;

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
        // 防止修改敏感字段
        user.setUserName(null);
        user.setPassword(null);
        user.setDeptId(null);
        // 注意：bizType 字段保留，允许修改
        
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

    /**
     * 根据用户ID查询用户公开信息（不包含敏感信息）
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    @GetMapping("/public/{userId}")
    @Operation(summary = "查询用户公开信息", description = "根据用户ID查询用户公开信息，不包含敏感信息（密码、手机号、邮箱、支付宝账号等）")
    public R<UserPublicInfoVO> getUserPublicInfo(
            @Parameter(description = "用户ID", required = true)
            @NotNull(message = "用户ID不能为空")
            @PathVariable Long userId) {
        UserPublicInfoVO userPublicInfo = userService.getUserPublicInfo(userId);
        // 追加成功打样邀约信息（来源 design 模块，避免 system 与 design 循环依赖）
        userPublicInfo.setSuccessfulProofingInvitations(invitationService.getSuccessfulInvitationsByUserId(userId));
        // 追加身份标签（仅身份类：0/1/2）
        try {
            userPublicInfo.setIdentityTags(sysUserTagService.selectUserTagListByType(userId, 0));
        } catch (Exception e) {
            userPublicInfo.setIdentityTags(java.util.Collections.emptyList());
        }
        return R.ok(userPublicInfo);
    }
}
