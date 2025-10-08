package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.constant.TagConstants;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.system.service.ISysUserTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 后台管理系统 - 用户标签管理
 * 管理员可以为用户添加身份标签和业务标签
 *
 * @author 陈善
 */
@Tag(name = "用户标签管理", description = "后台管理系统用户标签管理接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/tag")
public class SysUserTagController extends BaseController {

    private final ISysUserTagService sysUserTagService;

    /**
     * 为指定用户添加身份标签（厂商+设计师）
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "添加身份标签-厂商+设计师", description = "管理员为用户添加厂商+设计师身份标签")
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/identity/manufacturer-designer/{userId}")
    public R<Void> addManufacturerDesignerTag(@PathVariable Long userId, @RequestBody @Valid UserTagDTO tagDTO) {
        tagDTO.setBizType(TagConstants.IDENTITY_TAG_MANUFACTURER_DESIGNER);
        if (tagDTO.getTagLevel() == null) {
            tagDTO.setTagLevel(TagConstants.TAG_LEVEL_IMPORTANT);
        }
        sysUserTagService.addTag(userId, tagDTO);
        return R.ok();
    }

    /**
     * 为指定用户添加身份标签（设计师）
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "添加身份标签-设计师", description = "管理员为用户添加设计师身份标签")
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/identity/designer/{userId}")
    public R<Void> addDesignerTag(@PathVariable Long userId, @RequestBody @Valid UserTagDTO tagDTO) {
        tagDTO.setBizType(TagConstants.IDENTITY_TAG_DESIGNER);
        if (tagDTO.getTagLevel() == null) {
            tagDTO.setTagLevel(TagConstants.TAG_LEVEL_IMPORTANT);
        }
        sysUserTagService.addTag(userId, tagDTO);
        return R.ok();
    }

    /**
     * 为指定用户添加身份标签（普通用户）
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "添加身份标签-普通用户", description = "管理员为用户添加普通用户身份标签")
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/identity/user/{userId}")
    public R<Void> addUserTag(@PathVariable Long userId, @RequestBody @Valid UserTagDTO tagDTO) {
        tagDTO.setBizType(TagConstants.IDENTITY_TAG_USER);
        if (tagDTO.getTagLevel() == null) {
            tagDTO.setTagLevel(TagConstants.TAG_LEVEL_NORMAL);
        }
        sysUserTagService.addTag(userId, tagDTO);
        return R.ok();
    }

    /**
     * 为指定用户添加业务标签
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "添加业务标签", description = "管理员为用户添加业务标签")
    @Log(title = "用户标签管理", businessType = BusinessType.INSERT)
    @PostMapping("/business/{userId}")
    public R<Void> addBusinessTag(@PathVariable Long userId, @RequestBody @Valid UserTagDTO tagDTO) {
        // 强制设置为业务标签
        tagDTO.setBizType(TagConstants.BUSINESS_TAG);

        sysUserTagService.addTag(userId, tagDTO);
        return R.ok();
    }

    /**
     * 获取指定用户的所有标签
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取用户标签列表", description = "获取指定用户的所有标签")
    @GetMapping("/{userId}")
    public R<List<TagDetailVO>> getUserTags(@PathVariable Long userId) {
        List<TagDetailVO> tagList = sysUserTagService.selectUserTagList(userId);
        return R.ok(tagList);
    }

    /**
     * 获取指定用户的身份标签（所有身份标签）
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取用户身份标签", description = "获取指定用户的所有身份标签")
    @GetMapping("/{userId}/identity")
    public R<List<TagDetailVO>> getUserIdentityTags(@PathVariable Long userId) {
        // 获取所有身份标签（0,1,2）
        List<TagDetailVO> allTags = sysUserTagService.selectUserTagList(userId);
        List<TagDetailVO> identityTags = allTags.stream()
                .filter(tag -> TagConstants.isIdentityTag(tag.getBizType()))
                .collect(java.util.stream.Collectors.toList());
        return R.ok(identityTags);
    }

    /**
     * 获取指定用户的厂商+设计师标签
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取厂商+设计师标签", description = "获取指定用户的厂商+设计师身份标签")
    @GetMapping("/{userId}/identity/manufacturer-designer")
    public R<List<TagDetailVO>> getManufacturerDesignerTags(@PathVariable Long userId) {
        List<TagDetailVO> tagList = sysUserTagService.selectUserTagListByType(userId, TagConstants.IDENTITY_TAG_MANUFACTURER_DESIGNER);
        return R.ok(tagList);
    }

    /**
     * 获取指定用户的设计师标签
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取设计师标签", description = "获取指定用户的设计师身份标签")
    @GetMapping("/{userId}/identity/designer")
    public R<List<TagDetailVO>> getDesignerTags(@PathVariable Long userId) {
        List<TagDetailVO> tagList = sysUserTagService.selectUserTagListByType(userId, TagConstants.IDENTITY_TAG_DESIGNER);
        return R.ok(tagList);
    }

    /**
     * 获取指定用户的普通用户标签
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取普通用户标签", description = "获取指定用户的普通用户身份标签")
    @GetMapping("/{userId}/identity/user")
    public R<List<TagDetailVO>> getNormalUserTags(@PathVariable Long userId) {
        List<TagDetailVO> tagList = sysUserTagService.selectUserTagListByType(userId, TagConstants.IDENTITY_TAG_USER);
        return R.ok(tagList);
    }

    /**
     * 获取指定用户的业务标签
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取用户业务标签", description = "获取指定用户的业务标签")
    @GetMapping("/{userId}/business")
    public R<List<TagDetailVO>> getUserBusinessTags(@PathVariable Long userId) {
        List<TagDetailVO> tagList = sysUserTagService.selectUserTagListByType(userId, TagConstants.BUSINESS_TAG);
        return R.ok(tagList);
    }

    /**
     * 更新用户标签
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户标签", description = "管理员更新用户标签")
    @Log(title = "用户标签管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}")
    public R<Void> updateUserTag(@PathVariable Long userId, @RequestBody @Valid TagUpdateDTO tagDTO) {
        sysUserTagService.updateTag(userId, tagDTO);
        return R.ok();
    }

    /**
     * 删除用户标签
     */
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "删除用户标签", description = "管理员删除用户标签")
    @Log(title = "用户标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userId}/{tagId}")
    public R<Void> deleteUserTag(@PathVariable Long userId, @PathVariable Long tagId) {
        sysUserTagService.deleteTagById(userId, tagId);
        return R.ok();
    }

    /**
     * 获取标签详情
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取标签详情", description = "获取指定标签的详细信息")
    @GetMapping("/{userId}/{tagId}")
    public R<TagDetailVO> getTagDetail(@PathVariable Long userId, @PathVariable Long tagId) {
        TagDetailVO tagDetail = sysUserTagService.selectTagDetailById(userId, tagId);
        return R.ok(tagDetail);
    }
}
