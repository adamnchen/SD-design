package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.constant.CacheConstants;
import com.sutran.sd.common.constant.UserConstants;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.PayMember;
import com.sutran.sd.common.core.domain.entity.SysDept;
import com.sutran.sd.common.core.domain.entity.SysRole;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.domain.entity.SysAddress;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.excel.ExcelResult;
import com.sutran.sd.common.exception.user.CaptchaException;
import com.sutran.sd.common.exception.user.CaptchaExpireException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StreamUtils;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.poi.ExcelUtil;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.pay.service.PayMemberService;
import com.sutran.sd.system.domain.SysPost;
import com.sutran.sd.system.domain.SysUserForgetPwd;
import com.sutran.sd.system.domain.bo.SysUserMemberBo;
import com.sutran.sd.system.domain.vo.SysUserExportVo;
import com.sutran.sd.system.domain.vo.SysUserImportVo;
import com.sutran.sd.system.listener.SysUserImportListener;
import com.sutran.sd.system.service.ISysDeptService;
import com.sutran.sd.system.service.ISysPostService;
import com.sutran.sd.system.service.ISysRoleService;
import com.sutran.sd.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 用户信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPostService postService;
    private final ISysDeptService deptService;
    private final PayMemberService payMemberService;

    /**
     * 获取用户列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }

    /**
     * 导出用户列表
     */
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:user:export")
    @PostMapping("/export")
    public void export(SysUser user, HttpServletResponse response) {
        List<SysUser> list = userService.selectUserList(user);
        List<SysUserExportVo> listVo = BeanUtil.copyToList(list, SysUserExportVo.class);
        for (int i = 0; i < list.size(); i++) {
            SysDept dept = list.get(i).getDept();
            SysUserExportVo vo = listVo.get(i);
            if (ObjectUtil.isNotEmpty(dept)) {
                vo.setDeptName(dept.getDeptName());
                vo.setLeader(dept.getLeader());
            }
        }
        ExcelUtil.exportExcel(listVo, "用户数据", SysUserExportVo.class, response);
    }

    /**
     * 导入数据
     *
     * @param file          导入文件
     * @param updateSupport 是否更新已存在数据
     */
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        ExcelResult<SysUserImportVo> result = ExcelUtil.importExcel(file.getInputStream(), SysUserImportVo.class, new SysUserImportListener(updateSupport));
        return R.ok(result.getAnalysis());
    }

    /**
     * 获取导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "用户数据", SysUserImportVo.class, response);
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public R<Map<String, Object>> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        Map<String, Object> ajax = new HashMap<>(8);
        SysRole role = new SysRole();
        role.setStatus(UserConstants.ROLE_NORMAL);
        SysPost post = new SysPost();
        post.setStatus(UserConstants.POST_NORMAL);
        List<SysRole> roles = roleService.selectRoleList(role);
        ajax.put("roles", LoginHelper.isAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isAdmin()));
        ajax.put("posts", postService.selectPostList(post));
        if (ObjectUtil.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put("user", sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", StreamUtils.toList(sysUser.getRoles(), SysRole::getRoleId));
        }
        return R.ok(ajax);
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysUser user) {
        deptService.checkDeptDataScope(user.getDeptId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, getUserId())) {
            return R.fail("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 忘记密码
     */
    @PutMapping("/forgetPwd")
    @SaIgnore
    public R<Void> forgetPwd(@RequestBody SysUserForgetPwd forgetPwd) {
        if (!validateSmsCode(forgetPwd.getPhone(), forgetPwd.getCode())) {
            throw new CaptchaException();
        }
        forgetPwd.setNewPwd(BCrypt.hashpw(forgetPwd.getNewPwd()));
        return toAjax(userService.forgetPwd(forgetPwd.getPhone(),forgetPwd.getNewPwd()));
    }

    /**
     * 校验短信验证码
     */
    private boolean validateSmsCode(String phoneNumber, String smsCode) {
        String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + phoneNumber);
        if (StringUtils.isBlank(code)) {
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 恢复删除用户
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/recoverDel")
    public R<Void> recoverDel(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        return toAjax(userService.recoverDel(user.getUserId(),user.getDelFlag()));
    }

    /**
     * 根据用户编号获取授权角色
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/authRole/{userId}")
    public R<Map<String, Object>> authRole(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        Map<String, Object> ajax = new HashMap<>(2);
        ajax.put("user", user);
        ajax.put("roles", LoginHelper.isAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isAdmin()));
        return R.ok(ajax);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户Id
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public R<Void> insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return R.ok();
    }

    /**
     * 获取部门树列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/deptTree")
    public R<List<Tree<Long>>> deptTree(SysDept dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }

    /**
     * [会员]授权会员
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authMember")
    public R<Void> insertAuthMember(@RequestBody SysUserMemberBo bo) {
        if (CollUtil.isEmpty(bo.getUserIds()) || ObjectUtil.isNull(bo.getMemberId())) {
            return R.fail("参数错误");
        }
        PayMember payMember = payMemberService.detailById(bo.getMemberId().toString());
        userService.insertAuthMember(bo,payMember,new Date());
        return R.ok();
    }

}
