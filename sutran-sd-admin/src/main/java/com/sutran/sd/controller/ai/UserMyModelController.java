package com.sutran.sd.controller.ai;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.entity.Model;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.design.service.IModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户我的模型控制器
 * 
 * @author SutranSD
 */
@Slf4j
@RestController
@RequestMapping("/design/my-model")
@RequiredArgsConstructor
public class UserMyModelController {
    
    private final IModelService modelService;
    
    /**
     * 分页查询我的模型列表
     * 
     * @param pageQuery 分页参数
     * @param modelType 模型类型（可选）：SDXL、FLUX
     * @param isUserDel 是否删除（可选）：0-未删除、1-已删除
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<TableDataInfo<Model>> getMyModelPage(PageQuery pageQuery,
                                                  @RequestParam(required = false) String modelType,
                                                  @RequestParam(required = false) Integer isUserDel) {
        try {
            TableDataInfo<Model> result = modelService.getUserModelPage(pageQuery, modelType, isUserDel);
            return R.ok(result);
        } catch (Exception e) {
            log.error("分页查询我的模型失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询我的模型列表（不分页）
     * 
     * @param modelType 模型类型（可选）：SDXL、FLUX
     * @param isUserDel 是否删除（可选）：0-未删除、1-已删除
     * @return 模型列表
     */
    @GetMapping("/list")
    public R<List<Model>> getMyModelList(@RequestParam(required = false) String modelType,
                                         @RequestParam(required = false) Integer isUserDel) {
        try {
            List<Model> result = modelService.getUserModelList(modelType, isUserDel);
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询我的模型列表失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询我的模型详情
     * 
     * @param id 模型ID
     * @return 模型详情
     */
    @GetMapping("/{id}")
    public R<Model> getMyModelById(@PathVariable Long id) {
        try {
            Model result = modelService.getUserModelById(id);
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询我的模型详情失败，模型ID：{}", id, e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 统计我的模型数量
     * 
     * @param modelType 模型类型（可选）：SDXL、FLUX
     * @param isUserDel 是否删除（可选）：0-未删除、1-已删除
     * @return 模型数量
     */
    @GetMapping("/count")
    public R<Long> countMyModels(@RequestParam(required = false) String modelType,
                                @RequestParam(required = false) Integer isUserDel) {
        try {
            Long count = modelService.countUserModels(modelType, isUserDel);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计我的模型数量失败", e);
            return R.fail("统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除我的模型
     * 
     * @param id 模型ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteMyModel(@PathVariable Long id) {
        try {
            boolean success = modelService.deleteUserModel(id);
            if (success) {
                return R.ok("删除成功");
            } else {
                return R.fail("删除失败");
            }
        } catch (Exception e) {
            log.error("删除我的模型失败，模型ID：{}", id, e);
            return R.fail("删除失败：" + e.getMessage());
        }
    }
}
