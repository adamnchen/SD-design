package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.MimeTypeUtils;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.*;
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
 * 个人信息 业务处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {

    private final ISysUserService userService;
    private final ISysOssService iSysOssService;
    private final ISysUserAddressService addressService;
    private final ISysUserAddressAreaService addressAreaService;
    private final ISysUserTagService tagService;
    @SaCheckLogin

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
    /**
     *用户地址管理 添加地址
     */
    @Log(title = "用户地址管理", businessType = BusinessType.INSERT)
    @PutMapping("/addAddress")
    public R<Void> addAddress(@RequestBody SysAddress address) {
        addressService.addAddress(address);
        return R.ok();
    }

    /**
     * 删除用户地址
     */
    @Log(title = "用户地址管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteAddress/{addressId}")
    public R<Void> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return R.ok();
    }

    /**
     * 修改用户地址
     */
    @Log(title = "用户地址管理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateAddress")
    public R<Void> updateAddress(@RequestBody SysAddress address) {
        addressService.updateAddress(address);
        return R.ok();
    }
    /**
     * 获取用户地址
     */
    @Log(title = "用户地址管理")
    @GetMapping("/getAddress/{addressId}")
    public R<SysAddress> getAddress(@PathVariable Long addressId) {
        return R.ok(addressService.getAddress(addressId));
    }

    /**
     * 获取用户所有地址
     * @return
     */

    @Log(title = "用户地址管理")
    @GetMapping("/getAddressList")
    public R<List<SysAddress>> getAddressList() {
        Long currentUserId = LoginHelper.getUserId();

        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }
        return R.ok(addressService.selectAddressList(currentUserId));
    }
    /**
     * 设置默认地址
     */

    @Log(title = "用户地址管理", businessType = BusinessType.UPDATE)
    @PutMapping("/setDefaultAddress/{addressId}")
    public R<Void> setDefaultAddress(@PathVariable Long addressId) {
        addressService.setDefaultAddress(addressId);
        return R.ok();
    }

    /**
     * 省市区三级地址接口
     * @return
     */
    @Log(title = "用户地址管理", businessType = BusinessType.OTHER)
    @GetMapping("/getArea")
    public R<List<SysAddressArea>> getAreaList(
        @RequestParam(value = "parentCode", defaultValue = "0", required = false) String parentCode) {

        // 调用Service层方法，根据父级代码查询所有子级区域
        List<SysAddressArea> areaList =  addressAreaService.selectAreasByParentCode(parentCode);

        // 如果查询结果为空，返回一个空列表
        if (areaList == null || areaList.isEmpty()) {
            return R.fail("未查询到区域信息，请检查父级代码是否正确。");
        }
        return R.ok(areaList);
    }
    /**
     * 用户标签管理-添加标签
     */
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/addTag")
    public R<Void> addTag(@RequestBody @Valid UserTagDTO tagDTO) {
        // 1. 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            // 确保用户已登录，否则返回未登录或权限错误
            return R.fail("用户未登录，操作失败。");
        }
        tagService.addTag(userId, tagDTO);
        return R.ok();
    }
    /**
     * 删除标签
     */
    @Log(title = "用户标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagId}") // DELETE /system/user/tag/{tagId}
    public R<Void> deleteTag(@PathVariable Long tagId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }
        tagService.deleteTagById(userId, tagId);
        return R.ok();
    }


    /**
     * 更新标签
     * @param tagDTO
     * @return
     */
    @Log(title = "用户标签管理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateTag")
    public R<Void> updateTag(@RequestBody @Valid TagUpdateDTO tagDTO) {

        Long userId = LoginHelper.getUserId();

        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }

        // Service 层必须校验：1. 标签是否存在 2. 标签是否属于当前用户
        tagService.updateTag(userId, tagDTO);

        return R.ok();
    }
    /**
     * 查询当前用户的所有标签列表
     */
    @GetMapping("/list") // GET /system/user/tag/list
    public R<List<TagDetailVO>> listUserTags() {

        Long userId = LoginHelper.getUserId();

        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }

        // Service 层负责查询并转换为 VO
        List<TagDetailVO> tagList = tagService.selectUserTagList(userId);

        return R.ok(tagList);
    }
    /**
     * 根据ID获取标签详情
     */
    @GetMapping("/{tagId}") // GET /system/user/tag/{tagId}
    public R<TagDetailVO> getTagDetail(@PathVariable Long tagId) {

        Long userId = LoginHelper.getUserId();

        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }
        TagDetailVO tagDetail = tagService.selectTagDetailById(userId, tagId);

        return R.ok(tagDetail);
    }

}
