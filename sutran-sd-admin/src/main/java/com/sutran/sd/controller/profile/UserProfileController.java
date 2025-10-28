package com.sutran.sd.controller.profile;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserProfileUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.common.core.domain.entity.SysUserMember;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.core.domain.vo.UserProfileVO;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.user.service.IUserAddressService;
import com.sutran.sd.user.service.IUserProfileService;
import com.sutran.sd.user.service.IUserTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端用户个人信息管理
 *
 * @author 陈善
 */
@Tag(name = "用户个人信息管理", description = "客户端用户个人信息、地址、标签管理接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/profile")
@SaCheckLogin
public class UserProfileController extends BaseController {

    private final IUserProfileService userProfileService;
    private final IUserAddressService addressService;
    private final IUserTagService tagService;
    private final UserService userService;

    /**
     * 获取个人信息
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的基本信息")
    @GetMapping
    public R<UserProfileVO> profile() {
        return R.ok(userProfileService.getClientUserProfile(getUserId()));
    }

    /**
     * 修改个人信息
     */
    @Operation(summary = "修改个人信息", description = "更新当前登录用户的基本信息")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> updateProfile(@RequestBody @Valid UserProfileUpdateDTO updateDTO) {
        if (userProfileService.updateClientUserProfile(getUserId(), updateDTO)) {
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
        if (userProfileService.changePassword(getUserId(), oldPassword, newPassword)) {
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
    @PostMapping("/avatar")
    public R<Map<String, Object>> avatar(@RequestParam("avatarfile") MultipartFile avatarfile) {
        if (!avatarfile.isEmpty()) {
            String avatar = userProfileService.uploadAvatar(getUserId(), avatarfile);
            if (avatar != null) {
                Map<String, Object> data = new HashMap<>(2);
                data.put("imgUrl", avatar);
                return R.ok(data);
            }
        }
        return R.fail("上传图片异常，请联系管理员");
    }

    /**
     * 获取用户所有地址
     */
    @Operation(summary = "获取用户地址列表", description = "获取当前用户的所有收货地址")
    @Log(title = "用户地址管理")
    @GetMapping("/address")
    public R<List<SysAddress>> getAddressList() {
        Long currentUserId = LoginHelper.getUserId();

        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }
        return R.ok(addressService.selectAddressList(currentUserId));
    }

    /**
     * 添加地址
     */
    @Operation(summary = "添加地址", description = "为用户添加新的收货地址，包含省市区街道四级选择和详细地址手动填写")
    @Log(title = "用户地址管理", businessType = BusinessType.INSERT)
    @PostMapping("/address")
    public R<Void> addAddress(@RequestBody @Valid SysAddress address) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }

        // 验证必填字段
        if (address.getName() == null || address.getName().trim().isEmpty()) {
            return R.fail("收件人姓名不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return R.fail("省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return R.fail("城市不能为空");
        }
        if (address.getCounty() == null || address.getCounty().trim().isEmpty()) {
            return R.fail("区县不能为空");
        }
        if (address.getAddress() == null || address.getAddress().trim().isEmpty()) {
            return R.fail("街道不能为空");
        }
        if (address.getHome() == null || address.getHome().trim().isEmpty()) {
            return R.fail("详细地址不能为空");
        }
        if (address.getPhonenumber() == null || address.getPhonenumber().trim().isEmpty()) {
            return R.fail("手机号不能为空");
        }

        addressService.addAddress(address);
        return R.ok("地址添加成功");
    }

    /**
     * 修改地址
     */
    @Operation(summary = "修改地址", description = "修改用户的收货地址信息，包含省市区街道四级选择和详细地址手动填写")
    @Log(title = "用户地址管理", businessType = BusinessType.UPDATE)
    @PutMapping("/address")
    public R<Void> updateAddress(@RequestBody @Valid SysAddress address) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (address.getId() == null) {
            return R.fail("地址ID不能为空");
        }

        // 验证必填字段
        if (address.getName() == null || address.getName().trim().isEmpty()) {
            return R.fail("收件人姓名不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return R.fail("省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return R.fail("城市不能为空");
        }
        if (address.getCounty() == null || address.getCounty().trim().isEmpty()) {
            return R.fail("区县不能为空");
        }
        if (address.getAddress() == null || address.getAddress().trim().isEmpty()) {
            return R.fail("街道不能为空");
        }
        if (address.getHome() == null || address.getHome().trim().isEmpty()) {
            return R.fail("详细地址不能为空");
        }
        if (address.getPhonenumber() == null || address.getPhonenumber().trim().isEmpty()) {
            return R.fail("手机号不能为空");
        }

        addressService.updateAddress(address);
        return R.ok("地址修改成功");
    }

    /**
     * 删除地址
     */
    @Operation(summary = "删除地址", description = "删除用户的收货地址")
    @Log(title = "用户地址管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/address/{addressId}")
    public R<Void> deleteAddress(@PathVariable Long addressId) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }
        addressService.deleteAddress(addressId);
        return R.ok();
    }

    /**
     * 获取地址详情
     */
    @Operation(summary = "获取地址详情", description = "根据地址ID获取地址详细信息")
    @GetMapping("/address/{addressId}")
    public R<SysAddress> getAddressDetail(@PathVariable Long addressId) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            return R.fail("用户未登录或Token无效");
        }
        SysAddress address = addressService.getAddress(addressId);
        return R.ok(address);
    }

    /**
     * 设置默认地址
     */
    @Log(title = "用户地址管理", businessType = BusinessType.UPDATE)
    @PutMapping("/address/{addressId}/default")
    public R<Void> setDefaultAddress(@PathVariable Long addressId) {
        addressService.setDefaultAddress(addressId);
        return R.ok();
    }

    /**
     * 省市区街道四级地址接口
     * @param parentCode 父级区域代码，默认值为"0"，表示查询所有省份
     * @return 区域列表
     */
    @Log(title = "用户地址管理", businessType = BusinessType.OTHER)
    @GetMapping("/address/area")
    public R<List<SysAddressArea>> getAreaList(@RequestParam(value = "parentCode", defaultValue = "0", required = false) String parentCode) {
        List<SysAddressArea> areaList = addressService.getAreaList(parentCode);
        if (areaList == null || areaList.isEmpty()) {
            return R.fail("未查询到区域信息，请检查父级代码是否正确。");
        }
        return R.ok(areaList);
    }

    // ==================== 标签管理接口 ====================

    /**
     * 添加标签
     */
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/tag")
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
    @DeleteMapping("/tag/{tagId}")
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
     */
    @Log(title = "用户标签管理", businessType = BusinessType.UPDATE)
    @PutMapping("/tag")
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
    @Operation(summary = "获取用户标签列表", description = "获取当前用户的所有标签")
    @GetMapping("/tag")
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
    @GetMapping("/tag/{tagId}")
    public R<TagDetailVO> getTagDetail(@PathVariable Long tagId) {

        Long userId = LoginHelper.getUserId();

        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }
        TagDetailVO tagDetail = tagService.selectTagDetailById(userId, tagId);

        return R.ok(tagDetail);
    }

    /**
     * 获取建议的标签名称
     */
    @Operation(summary = "获取建议标签名称", description = "获取系统建议的标签名称，避免与身份标签冲突")
    @GetMapping("/tag/suggestions")
    public R<List<String>> getTagSuggestions() {
        return R.ok(com.sutran.sd.common.utils.TagNameValidator.getSuggestedTagNames());
    }

    /**
     * 获取用户会员信息
     */
    @Operation(summary = "获取用户当前会员信息", description = "获取用户会员信息(携带会员状态0-失效,1-有效)")
    @GetMapping("/member")
    public R<SysUserMember> getUserMember() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效。");
        }
        SysUserMember member = userService.selectUserMember(userId);
        return R.ok(member);
    }

    // ==================== 支付宝账号管理 ====================

    /**
     * 绑定支付宝账号
     */
    @Operation(summary = "绑定支付宝账号", description = "用户绑定支付宝账号和实名姓名，用于接收资金转账")
    @Log(title = "支付宝账号管理", businessType = BusinessType.UPDATE)
    @PostMapping("/alipay/bind")
    public R<Void> bindAlipayAccount(@RequestBody @Valid com.sutran.sd.common.core.domain.dto.AlipayAccountBindDTO bindDTO) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (userProfileService.bindAlipayAccount(userId, bindDTO)) {
            return R.ok("支付宝账号绑定成功");
        }
        return R.fail("绑定失败，请联系管理员");
    }

    /**
     * 解绑支付宝账号
     */
    @Operation(summary = "解绑支付宝账号", description = "用户解绑已绑定的支付宝账号")
    @Log(title = "支付宝账号管理", businessType = BusinessType.UPDATE)
    @DeleteMapping("/alipay/unbind")
    public R<Void> unbindAlipayAccount() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (userProfileService.unbindAlipayAccount(userId)) {
            return R.ok("支付宝账号解绑成功");
        }
        return R.fail("解绑失败，请联系管理员");
    }

    /**
     * 获取支付宝账号信息
     */
    @Operation(summary = "获取支付宝账号信息", description = "查询当前用户已绑定的支付宝账号信息")
    @GetMapping("/alipay/info")
    public R<com.sutran.sd.common.core.domain.dto.AlipayAccountBindDTO> getAlipayAccount() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        com.sutran.sd.common.core.domain.dto.AlipayAccountBindDTO accountInfo = userProfileService.getAlipayAccount(userId);
        return R.ok(accountInfo);
    }
}
