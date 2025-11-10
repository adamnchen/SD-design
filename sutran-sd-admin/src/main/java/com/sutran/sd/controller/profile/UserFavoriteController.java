package com.sutran.sd.controller.profile;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.annotation.Log;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.UserFavoriteDTO;
import com.sutran.sd.common.core.domain.vo.UserFavoriteVO;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.enums.BusinessType;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.user.service.IUserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户收藏管理控制器
 * 
 * @author sutran
 * @date 2025-11-07
 */
@Tag(name = "用户收藏管理", description = "用户收藏模型和作品的接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/profile/favorite")
@SaCheckLogin
public class UserFavoriteController extends BaseController {

    private final IUserFavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏", description = "用户收藏模型或作品")
    @Log(title = "用户收藏管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> addFavorite(@RequestBody @Valid UserFavoriteDTO favoriteDTO) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (favoriteService.addFavorite(userId, favoriteDTO)) {
            return R.ok("收藏成功");
        }
        return R.fail("收藏失败");
    }

    /**
     * 删除收藏（根据收藏ID）
     */
    @Operation(summary = "删除收藏", description = "根据收藏ID删除收藏记录")
    @Log(title = "用户收藏管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{favoriteId}")
    public R<Void> deleteFavorite(@PathVariable Long favoriteId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (favoriteService.deleteFavorite(userId, favoriteId)) {
            return R.ok("取消收藏成功");
        }
        return R.fail("取消收藏失败");
    }

    /**
     * 删除收藏（根据收藏对象）
     */
    @Operation(summary = "根据对象删除收藏", description = "根据收藏类型和目标ID删除收藏记录")
    @Log(title = "用户收藏管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/target")
    public R<Void> deleteFavoriteByTarget(@RequestParam Integer favoriteType, 
                                          @RequestParam Long targetId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        if (favoriteService.deleteFavoriteByTarget(userId, favoriteType, targetId)) {
            return R.ok("取消收藏成功");
        }
        return R.fail("取消收藏失败");
    }

    /**
     * 分页查询收藏列表
     */
    @Operation(summary = "分页查询收藏列表", description = "分页查询当前用户的收藏列表，支持按收藏类型筛选")
    @GetMapping
    public R<TableDataInfo<UserFavoriteVO>> list(@RequestParam(required = false) Integer favoriteType,
                                                  PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        Page<UserFavoriteVO> page = favoriteService.selectFavoritePage(userId, favoriteType, pageQuery);
        return R.ok(TableDataInfo.build(page));
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查是否已收藏", description = "检查用户是否已收藏指定对象")
    @GetMapping("/check")
    public R<Boolean> checkFavorite(@RequestParam Integer favoriteType, 
                                    @RequestParam Long targetId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("用户未登录或Token无效");
        }

        boolean isFavorite = favoriteService.isFavorite(userId, favoriteType, targetId);
        return R.ok(isFavorite);
    }
}

