package com.sutran.sd.controller.sdapi;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskBo;
import com.sutran.sd.draw.service.SdComfyuiTaskService;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.draw.domain.dto.task.SdUserTaskPageDto;
import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.draw.domain.pojo.ComfyTaskHistoryInfo;
import com.sutran.sd.draw.domain.vo.SdUserModelFileVo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.service.SdUserModelService;
import com.sutran.sd.draw.service.SdWebuiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * SD-API接口
 * @author zj
 * @date 2024-02-27
 */
@RestController
@RequestMapping("/sd/api")
@RequiredArgsConstructor
public class SdApiController {

    private final SdWebuiApiService sdWebuiApiService;
    private final SdComfyuiTaskService sdComfyuiTaskService;
    private final SdUserModelService sdUserModelService;

    /**
     * [WebUI]文生图
     */
    @PostMapping("/txt2img")
    public R<String> txt2img(@Validated @RequestBody SdText2ImgDto dto) {
        String taskId = sdWebuiApiService.txt2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [WebUI]图生图
     */
    @PostMapping("/img2img")
    public R<String> img2img(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdWebuiApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [WebUI]图生图(局部重绘)
     */
    @PostMapping("/img2img/mask")
    public R<String> img2imgOfMask(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdWebuiApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [WebUI]获取当前用户任务列表
     */
    @GetMapping("/task/list")
    public TableDataInfo<SdUserTaskVo> userTaskList(SdUserTaskPageDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        return sdWebuiApiService.userDrawTaskList(pageQuery,dto.getCategory(), dto.getStatus());
    }

    /**
     * [WebUI]根据taskId获取当前用户绘图数据列表
     */
    @GetMapping("/model-file/list")
    public R<List<SdUserModelFileVo>> userModelFileList(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.listUserModelFile(taskId));
    }

    /**
     * [WebUI]批量压缩下载绘图图片
     */
    @GetMapping("/task/download")
    public void batchDownloadModelFile(@RequestParam String taskId, HttpServletResponse response) throws IOException {
        sdWebuiApiService.batchDownloadModelFile(taskId,response);
    }

    /**
     * [WebUI]单个下载绘图图片
     */
    @GetMapping("/task/img/download")
    public void downloadModelFile(@RequestParam String imgUrl, HttpServletResponse response) throws IOException {
        if (StrUtil.isEmpty(imgUrl)) {
            return;
        }
        sdWebuiApiService.downloadUserModelFile(imgUrl,response);
    }

    /**
     * [WebUI]删除任务中的单张图片
     */
    @DeleteMapping("/model-file")
    public R<Void> deleteModelFile(@RequestParam String id) {
        sdWebuiApiService.removeUserModelFile(Collections.singletonList(id));
        return R.ok();
    }

    /**
     * [WebUI]获取当前用户正在进行的任务taskId
     */
    @GetMapping("/doing-task")
    public R<String> getDoingTask(@RequestParam Integer category) {
        return R.ok("操作成功", sdWebuiApiService.getDoingTaskId(category));
    }

    /**
     * [WebUI]删除队列中、已完成、已失败的任务
     */
    @DeleteMapping("/task")
    public R<String> deleteTask(@RequestParam String taskId) {
        sdWebuiApiService.deleteTaskById(taskId);
        return R.ok();
    }

    /**
     * [WebUI]进度查询
     */
    @GetMapping("/process")
    public R<JSONObject> getProcess(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.getProcess(taskId));
    }


    /**
     * [ComfyUI]提交模型生图任务
     * @param bo 模型任务提交参数[必填]
     * @return 任务id
     */
    @PostMapping("/comfy/model/submit-task")
    @SaIgnore
    public R<String> submitComfyModelTask(@RequestBody ComfyModelTaskBo bo) {
        // 校验模型是否存在
        SdUserModel model = sdUserModelService.selectById(bo.getModelId());
        if (model == null) {
            throw new TaskErrorException("模型不存在或已被删除!");
        }
        ComfyModelTaskSubmitBo modelTaskBo = new ComfyModelTaskSubmitBo()
            .setModelId(bo.getModelId()).setPrompt(bo.getPrompt())
            .setPromptZh(bo.getPromptZh())
            .setModelType(model.getModelType())
            .setModelName(model.getModelName())
            .setModelStrength(StringUtils.isNotBlank(bo.getModelStrength())?bo.getModelStrength():model.getModelStrength())
            .setBatchSize(bo.getBatchSize());
        String taskId = sdComfyuiTaskService.submitModelTask(modelTaskBo);
        return R.ok(taskId);
    }

    /**
     * [ComfyUI]提交工作流生图任务
     * @param flowId 工作流ID[必填]
     * @return 任务id
     */
    @GetMapping("/comfy/flow/submit-task")
    @SaIgnore
    public R<String> submitComfyFlowTask(@RequestParam String flowId) {
        String taskId = sdComfyuiTaskService.submitComfyFlowTask(flowId);
        return R.ok(taskId);
    }

    /**
     * [ComfyUI]获取指定生图任务详情
     * @param taskId 任务ID[必填]
     * @return 任务详情
     */
    @GetMapping("/comfy/model/history-task")
    @SaIgnore
    public R<ComfyTaskHistoryInfo> getComfyModelHistoryTask(@RequestParam String taskId) {
        return R.ok(sdComfyuiTaskService.getComfyModelHistoryTask(taskId));
    }

    /**
     * [ComfyUI]获取指定任务生成进度
     * @param taskId 任务ID[必填]
     * @return 任务进度
     */
    @GetMapping("/comfy/model/task-progress")
    @SaIgnore
    public R<Integer> getComfyTaskProgress(@RequestParam String taskId) {
        return R.ok(sdComfyuiTaskService.getComfyTaskProgress(taskId));
    }

}
