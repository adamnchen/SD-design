package com.sutran.sd.controller.web.system;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.BatchRemoveDto;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.dto.model.SdTrainTaskDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelDto;
import com.sutran.sd.draw.domain.dto.task.SdUserTaskPageDto;
import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.service.SdGpuPoolService;
import com.sutran.sd.draw.service.SdTrainService;
import com.sutran.sd.draw.service.SdTranslationService;
import com.sutran.sd.draw.service.SdWebuiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [SD]后台API
 * @author zj
 * @date 2024-03-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sd")
public class SysSdController extends BaseController {

    private final SdWebuiApiService sdWebuiApiService;
    private final SdTrainService sdTrainService;
    private final SdGpuPoolService sdGpuPoolService;
    private final SdTranslationService sdTranslationService;

    /**
     * SD 大模型-获取基础大模型列表
     */
    @GetMapping("/checkpoint/list")
    public R<List<CheckPointVo>> listCheckpointModels() {
        return R.ok(sdWebuiApiService.listCheckpointModels());
    }

    /**
     * SD 大模型-切换模基础大模型
     */
    @GetMapping("/checkpoint/option")
    public R<List<CheckPointVo>> checkpointOptions(@RequestParam("title") String title) {
        sdWebuiApiService.checkpointOptions(title);
        return R.ok();
    }

    /**
     * SD Lora模型-查询列表(模型关联的测试任务、xyz数据)
     */
    @GetMapping("/lora/list")
    public TableDataInfo<SdUserModelVo> listLoraModelsOfTestTaskAndTrainData(SdUserModelDto dto) {
        return sdWebuiApiService.listLoraModelsOfTestTaskAndTrainData(dto);
    }

    /**
     *  SD Lora模型-获取取训练模型的数据集
     */
    @GetMapping("/model-train-data/list")
    public R<List<JSONObject>> getModelTrainDateList(@RequestParam String preTaskId) throws IOException {
        return R.ok(sdTrainService.getModelTrainDateList(preTaskId));
    }

    /**
     *  SD Lora模型-删除xyz测试数据
     */
    @DeleteMapping("/xyz-data")
    public R<Void> delXyzData(@RequestParam String taskId) {
        sdWebuiApiService.delXyzData(taskId);
        return R.ok();
    }

    /**
     * SD Lora模型-发布/取消发布模型
     */
    @PutMapping("/lora/publish-status")
    public R<Void> publishModel(@RequestParam String id,@RequestParam Integer publishStatus,@RequestParam(required = false) String modelStrength) {
        sdWebuiApiService.publishModel(id,publishStatus,modelStrength);
        return R.ok();
    }

    /**
     * SD Lora模型-修改模型强度
     */
    @PutMapping("/lora/model-strength")
    public R<Void> modifyModelStrength(@RequestParam String id,@RequestParam String modelStrength) {
        sdWebuiApiService.modifyModelStrength(id,modelStrength);
        return R.ok();
    }

    /**
     * SD Lora模型-删除个人未发布的模型
     */
    @DeleteMapping("/lora")
    public R<Void> removeModel(@RequestParam String id) {
        sdWebuiApiService.removeModelOfAdmin(Collections.singletonList(id));
        return R.ok();
    }

    /**
     * SD Lora模型-批量删除个人未发布的模型
     */
    @DeleteMapping("/lora/batch")
    public R<Void> batchRemoveModel(@RequestBody BatchRemoveDto dto) {
       sdWebuiApiService.removeModelOfAdmin(dto.getIds());
        return R.ok();
    }

    /**
     * SD Lora模型-文生图模型测试
     */
    @PostMapping("/lora/txt2img/test")
    public R<String> testText2ImgLoraModels(@Validated @RequestBody SdText2ImgDto dto) {
        String taskId = sdWebuiApiService.testTxt2ImgOfLoraModel(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     *  SD-获取所有用户任务列表(管理员查询)
     */
    @GetMapping("/task/all-list")
    public TableDataInfo<SdUserTaskVo> allUserTaskList(SdUserTaskPageDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        return sdWebuiApiService.allUserTaskList(pageQuery,dto.getCategory(),dto.getStatus());
    }

    /**
     *  SD-获取指定任务下的全部绘图数据列表
     */
    @GetMapping("/model-file/list")
    public R<List<SdUserModelFileVo>> userModelFileList(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.listUserModelFile(taskId));
    }

    /**
     * SD 任务进度查询
     */
    @GetMapping("/process")
    public R<SdWebuiProgressVo> getProcess(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.getProcess(taskId));
    }

    /**
     * SD 1、查询gpu卡池
     */
    @GetMapping("/sync-gpu-pool/list")
    @SaIgnore
    public R<List<SdGpuPool>> getGuPoolList(@RequestParam(required = false) Integer type) {
        List<SdGpuPool> list = sdGpuPoolService.getList(type);
        if (CollectionUtil.isEmpty(list)){
            return R.ok(Collections.emptyList());
        }
        // 过滤可用的GPU
        List<SdGpuPool> sdGpuPools = list.stream().filter(e -> e.getIsEnable()==1).collect(Collectors.toList());
        return R.ok(sdGpuPools);
    }

    /**
     * SD 2、同步GPU卡池
     */
    @GetMapping("/sync-gpu-pool")
    @SaIgnore
    public R<JSONObject> syncGpuPool(@RequestParam Integer type) {
        return R.ok(sdWebuiApiService.syncGpuPool(type));
    }

    /**
     * SD 3、下线指定GPU卡
     */
    @DeleteMapping("/stop-gpu-pool")
    @SaIgnore
    public R<Void> stopGpuPool(@RequestParam String id) {
        SdGpuPool sdGpuPool = sdGpuPoolService.selectById(id);
        if (sdGpuPool==null) {
            throw new ServiceException("当前GPU服务不存在!");
        }
        else if (sdGpuPool.getIsEnable()==0) {
            throw new ServiceException("当前GPU服务是未启用状态!");
        }
        if (sdGpuPool.getType()==1) {
            sdTrainService.stopGpuPool(sdGpuPool);
        }
        else {
            sdWebuiApiService.stopGpuPool(sdGpuPool);
        }
        return R.ok("GPU停用成功");
    }

    /**
     * SD 4、上线指定GPU服务
     */
    @GetMapping("/start-gpu-pool")
    @SaIgnore
    public R<Void> startGpuPool(@RequestParam String id) {
        SdGpuPool sdGpuPool = sdGpuPoolService.selectById(id);
        if (sdGpuPool==null) {
            throw new ServiceException("当前GPU服务不存在!");
        }
        else if (sdGpuPool.getIsEnable()==0) {
            throw new ServiceException("当前GPU服务是未启用状态!");
        }
        if (sdGpuPool.getType()==1) {
            sdTrainService.startGpuPool(sdGpuPool);
        }
        else {
            sdWebuiApiService.startGpuPool(sdGpuPool);
        }
        return R.ok("GPU启用用成功");
    }

    /**
     * SD 5、从redis同步翻译字典到数据库
     * @param type 类型[0-提示词英译中,1-tag标签中译英]
     */
    @GetMapping("/redis/sync-translation")
    @SaIgnore
    public R<Void> syncTranslationFromRedisToDb(@RequestParam Integer type) {
        sdTranslationService.syncTranslationFromRedisToDb(type);
        return R.ok("同步成功");
    }

    /**
     * SD 6、从数据库同步翻译字典到redis
     * @param type 类型[0-提示词英译中,1-tag标签中译英]
     */
    @GetMapping("/db/sync-translation")
    @SaIgnore
    public R<Void> syncTranslationFromDbToRedis(@RequestParam Integer type) {
        sdTranslationService.syncTranslationFromDbToRedis(type);
        return R.ok("同步成功");
    }


    /**
     * [Fluxgym]分页查询训练任务
     * @param dto 查询参数实体
     * @return 训练任务列表
     */
    @GetMapping("/fluxgym/train-task")
    public TableDataInfo<TrainTaskVo> listTrainTaskOfFluxgym(SdTrainTaskDto dto) {
        return sdTrainService.listTrainTaskOfFluxgym(dto);
    }

    /**
     * [Fluxgym]根据任务ID查询模型名称列表
     * @param taskId 训练任务id
     * @return 模型名称列表
     */
    @GetMapping("/fluxgym/train-task/model-name-list")
    public R<List<FluxgymModelListVo>> listModelNameOfFluxgym(@RequestParam String taskId) {
        return R.ok(sdTrainService.listModelNameOfFluxgym(taskId));
    }

    /**
     * [Fluxgym]根据任务ID查询模型素材原图、提示词和缩略图
     * @param taskId 训练任务id
     * @return 模型名称列表
     */
    @GetMapping("/fluxgym/train-task/model-preview")
    public R<FluxgymModelPreviewVo> listModelPreviewOfFluxgym(@RequestParam String taskId) {
        return R.ok(sdTrainService.listModelPreviewOfFluxgym(taskId));
    }

    /**
     * [Fluxgym]发布/取消发布模型
     * @param id 模型id
     * @param publishStatus 发布状态[0-取消发布,1-发布]
     */
    @PutMapping("/fluxgym/lora/publish-status")
    public R<Void> publishModelOfFluxgym(@RequestParam String id,@RequestParam Integer publishStatus) {
        sdTrainService.publishModelOfFluxgym(id,publishStatus);
        return R.ok();
    }

    /**
     * [Fluxgym]删除当前任务未发布模型
     * @param taskId 任务id
     */
    @DeleteMapping("/fluxgym/lora")
    public R<Void> removeUnpublishedModelOfFluxgym(@RequestParam String taskId) {
        sdTrainService.removeUnpublishedModelOfFluxgym(taskId);
        return R.ok();
    }

}
