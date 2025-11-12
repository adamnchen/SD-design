package com.sutran.sd.controller.design;

import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.service.ISdUserModelFileService;
import com.sutran.sd.design.vo.UserModelFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户生图文件数据记录Controller
 *
 * @author sutran
 * @date 2025-10-11
 */
@Tag(name = "我的作品管理", description = "用户生图作品相关接口")
@Slf4j
@RestController
@RequestMapping("/design/user-model-file")
@RequiredArgsConstructor
public class UserModelFileController extends BaseController {

    private final ISdUserModelFileService userModelFileService;

    /**
     * 获取我的作品列表（分页）
     */
    @Operation(summary = "获取我的作品列表", description = "获取当前用户的所有生图作品列表，支持分页查询，按创建时间倒序排列")
    @GetMapping("/my-works")
    public TableDataInfo<UserModelFileVO> getMyWorks(PageQuery pageQuery) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            log.info("获取我的作品列表: 用户ID={}, 页码={}, 页大小={}", currentUserId, pageQuery.getPageNum(), pageQuery.getPageSize());
            
            TableDataInfo<UserModelFileVO> result = userModelFileService.getMyWorksPage(currentUserId, pageQuery);
            return result;
        } catch (Exception e) {
            log.error("获取我的作品列表失败", e);
            return TableDataInfo.build();
        }
    }

    /**
     * 根据分类获取我的作品列表（分页）
     */
    @Operation(summary = "根据分类获取我的作品列表", description = "根据分类获取当前用户的生图作品列表，支持分页查询，支持文生图(0)和图生图(1)")
    @GetMapping("/my-works/category/{category}")
    public TableDataInfo<UserModelFileVO> getMyWorksByCategory(@PathVariable Integer category, PageQuery pageQuery) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            log.info("根据分类获取我的作品列表: 用户ID={}, 分类={}, 页码={}, 页大小={}", currentUserId, category, pageQuery.getPageNum(), pageQuery.getPageSize());
            
            // 验证分类参数
            if (category != null && category != 0 && category != 1) {
                log.warn("分类参数错误: {}", category);
                return TableDataInfo.build();
            }
            
            TableDataInfo<UserModelFileVO> result = userModelFileService.getMyWorksByCategoryPage(currentUserId, category, pageQuery);
            return result;
        } catch (Exception e) {
            log.error("根据分类获取我的作品列表失败: 分类={}", category, e);
            return TableDataInfo.build();
        }
    }

    /**
     * 获取我的作品详情
     */
    @Operation(summary = "获取我的作品详情", description = "根据作品ID获取当前用户的生图作品详细信息")
    @GetMapping("/my-works/{id}")
    public R<UserModelFileVO> getMyWorkDetail(@PathVariable Long id) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            log.info("获取我的作品详情: 作品ID={}, 用户ID={}", id, currentUserId);
            
            UserModelFileVO work = userModelFileService.getMyWorkDetail(id, currentUserId);
            if (work == null) {
                return R.fail("作品不存在或不属于当前用户");
            }
            
            return R.ok(work);
        } catch (Exception e) {
            log.error("获取我的作品详情失败: 作品ID={}", id, e);
            return R.fail("获取作品详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除我的作品
     */
    @Operation(summary = "删除我的作品", description = "根据作品ID删除当前用户的生图作品")
    @DeleteMapping("/my-works/{id}")
    public R<Void> deleteMyWork(@PathVariable Long id) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            log.info("删除我的作品: 作品ID={}, 用户ID={}", id, currentUserId);
            
            // 先检查作品是否属于当前用户
            UserModelFileVO work = userModelFileService.getMyWorkDetail(id, currentUserId);
            if (work == null) {
                return R.fail("作品不存在或不属于当前用户");
            }
            
            int result = userModelFileService.deleteSdUserModelFileById(id);
            if (result > 0) {
                log.info("删除作品成功: 作品ID={}", id);
                return R.ok();
            } else {
                return R.fail("删除作品失败");
            }
        } catch (Exception e) {
            log.error("删除我的作品失败: 作品ID={}", id, e);
            return R.fail("删除作品失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除我的作品
     */
    @Operation(summary = "批量删除我的作品", description = "根据作品ID数组批量删除当前用户的生图作品")
    @DeleteMapping("/my-works/batch")
    public R<Void> deleteMyWorks(@RequestBody Long[] ids) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            log.info("批量删除我的作品: 作品IDs={}, 用户ID={}", ids, currentUserId);
            
            if (ids == null || ids.length == 0) {
                return R.fail("请选择要删除的作品");
            }
            
            // 验证所有作品是否都属于当前用户
            for (Long id : ids) {
                UserModelFileVO work = userModelFileService.getMyWorkDetail(id, currentUserId);
                if (work == null) {
                    return R.fail("作品ID " + id + " 不存在或不属于当前用户");
                }
            }
            
            int result = userModelFileService.deleteSdUserModelFileByIds(ids);
            if (result > 0) {
                log.info("批量删除作品成功: 删除数量={}", result);
                return R.ok();
            } else {
                return R.fail("批量删除作品失败");
            }
        } catch (Exception e) {
            log.error("批量删除我的作品失败: 作品IDs={}", ids, e);
            return R.fail("批量删除作品失败: " + e.getMessage());
        }
    }

    /**
     * 设置作品为公开
     */
    @Operation(summary = "设置作品为公开", description = "将指定作品设置为公开，仅作品所属用户可操作")
    @PutMapping("/my-works/{id}/public")
    public R<Void> makePublic(@PathVariable Long id) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            boolean ok = userModelFileService.setPublic(id, currentUserId, true);
            return ok ? R.ok() : R.fail("设置公开失败：作品不存在或不属于当前用户");
        } catch (Exception e) {
            log.error("设置作品公开失败: 作品ID={}", id, e);
            return R.fail("设置公开失败: " + e.getMessage());
        }
    }

    /**
     * 取消作品公开
     */
    @Operation(summary = "取消作品公开", description = "将指定作品取消公开，仅作品所属用户可操作")
    @PutMapping("/my-works/{id}/private")
    public R<Void> makePrivate(@PathVariable Long id) {
        try {
            Long currentUserId = LoginHelper.getUserId();
            boolean ok = userModelFileService.setPublic(id, currentUserId, false);
            return ok ? R.ok() : R.fail("取消公开失败：作品不存在或不属于当前用户");
        } catch (Exception e) {
            log.error("取消作品公开失败: 作品ID={}", id, e);
            return R.fail("取消公开失败: " + e.getMessage());
        }
    }
}
