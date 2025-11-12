package com.sutran.sd.controller.sdapi;

import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.annotation.RequireMember;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskBo;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.draw.domain.dto.task.SdUserTaskPageDto;
import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.service.SdComfyuiApiService;
import com.sutran.sd.draw.service.SdUserModelService;
import com.sutran.sd.draw.service.SdWebuiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * SD-绘图接口
 * @author zj
 * @date 2024-02-27
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@RestController
@RequestMapping("/sd/api")
@RequiredArgsConstructor
public class SdDrawController {

    private final SdWebuiApiService sdWebuiApiService;
    private final SdComfyuiApiService sdComfyuiApiService;
    private final SdUserModelService sdUserModelService;

    /**
     * [WebUI]文生图
     */
    @PostMapping("/txt2img")
    @RequireMember(value = "文生图", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> txt2img(@Validated @RequestBody SdText2ImgDto dto) {
        String taskId = sdWebuiApiService.txt2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [WebUI]图生图
     */
    @PostMapping("/img2img")
    @RequireMember(value = "图生图", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> img2img(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdWebuiApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [WebUI]图生图(局部重绘)
     */
    @PostMapping("/img2img/mask")
    @RequireMember(value = "图生图(局部重绘)", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> img2imgOfMask(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdWebuiApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * [通用]获取当前用户任务列表
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
     * [通用]根据taskId获取当前用户绘图数据列表
     */
    @GetMapping("/model-file/list")
    public R<List<SdUserModelFileVo>> userModelFileList(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.listUserModelFile(taskId));
    }

    /**
     * [通用]批量压缩下载绘图图片
     */
    @GetMapping("/task/download")
    public void batchDownloadModelFile(@RequestParam String taskId, HttpServletResponse response) throws IOException {
        sdWebuiApiService.batchDownloadModelFile(taskId,response);
    }

    /**
     * [通用]单个下载绘图图片
     */
    @GetMapping("/task/img/download")
    public void downloadModelFile(@RequestParam String imgUrl, HttpServletResponse response) throws IOException {
        if (StrUtil.isEmpty(imgUrl)) {
            return;
        }
        sdWebuiApiService.downloadUserModelFile(imgUrl,response);
    }

    /**
     * [通用]删除任务中的单张图片
     */
    @DeleteMapping("/model-file")
    public R<Void> deleteModelFile(@RequestParam String id) {
        sdWebuiApiService.removeUserModelFile(Collections.singletonList(id));
        return R.ok();
    }

    /**
     * [通用]获取当前用户正在进行的任务taskId
     */
    @GetMapping("/doing-task")
    public R<String> getDoingTask(@RequestParam Integer category) {
        return R.ok("操作成功", sdWebuiApiService.getDoingTaskId(category));
    }

    /**
     * [通用]删除队列中、已完成、已失败的任务
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
    public R<SdWebuiProgressVo> getProcess(@RequestParam String taskId) {
        return R.ok(sdWebuiApiService.getProcess(taskId));
    }


    /**
     * [ComfyUI]获取修图工具(固定工作流)
     * @return 修图工具列表
     */
    @PostMapping("/comfy/photo-edit-tool/list")
    public R<List<ComfyuiImageToolVo>> queryPhotoEditToolList() {
        return R.ok(sdComfyuiApiService.queryFixedFlowList());
    }

    /**
     * [ComfyUI]提交模型生图任务(从模型列表获取模型)
     * @param bo 模型任务提交参数[必填]
     * @return 任务id
     */
    @PostMapping("/comfy/model/submit-task")
    @RequireMember(value = "ComfyUI模型生图", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> submitComfyModelTask(@Validated @RequestBody ComfyModelTaskBo bo) {
        // 校验模型是否存在
        SdUserModel model = sdUserModelService.selectById(bo.getModelId());
        if (model == null) {
            throw new TaskErrorException("模型不存在或已被删除!");
        }
        if (StringUtils.isBlank(model.getModelName())) {
            throw new TaskErrorException("模型名称不能为空!");
        }
        if (!model.getModelName().endsWith(".safetensors")) {
            model.setModelName(model.getModelName()+".safetensors");
        }
        ComfyModelTaskSubmitBo modelTaskBo = new ComfyModelTaskSubmitBo()
            .setModelId(bo.getModelId()).setPrompt(bo.getPrompt())
            .setPromptZh(bo.getPromptZh())
            .setModelType(model.getModelType())
            .setModelName(model.getModelName())
            .setModelStrength(StringUtils.isNotBlank(bo.getModelStrength())?bo.getModelStrength():model.getModelStrength())
            .setBatchSize(bo.getBatchSize());
        if ("FLUX".equals(model.getModelType())) {
            modelTaskBo.setCheckPoint("F.1基础算法模型F.1-dev-fp8.safetensors");
        }
        else if ("SDXL".equals(model.getModelType())) {
            modelTaskBo.setCheckPoint("sd_xl_base_1.0.safetensors");
        }
        String taskId = sdComfyuiApiService.submitComfyModelTask(modelTaskBo);
        return R.ok("提交成功",taskId);
    }

    /**
     * [ComfyUI]提交工作流生图任务
     * @param flowId 工作流ID[必填]
     * @param prompt 描述词(英文)
     * @param promptZh 描述词(中文)
     * @param image1 图片1
     * @param image2 图片2
     * @return 任务id
     */
    @PostMapping("/comfy/flow/submit-task")
    @RequireMember(value = "ComfyUI工作流生图", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> submitComfyFlowTask(@RequestParam String flowId,
                                         @RequestParam(required = false) String prompt,
                                         @RequestParam(required = false) String promptZh,
                                         @RequestParam(required = false) MultipartFile image1,
                                         @RequestParam(required = false) MultipartFile image2) throws IOException {
        MultipartFile[] images = new MultipartFile[2];
        images[0] = image1;
        images[1] = image2;
        String taskId = sdComfyuiApiService.submitComfyFlowTask(flowId,prompt,promptZh,images);
        return R.ok("提交成功",taskId);
    }

    /**
     * [ComfyUI]查询指定任务的生图列表
     * @param taskId 任务ID[必填]
     * @return 任务详情
     */
    @GetMapping("/comfy/model/history-task")
    public R<List<ComfyUserModelFileVo>> getComfyModelHistoryTask(@RequestParam String taskId) {
        return R.ok(sdComfyuiApiService.getComfyImageOutputByTaskId(taskId));
    }

    /**
     * [ComfyUI]获取指定任务生成进度
     * @param taskId 任务ID[必填]
     * @return 任务进度
     */
    @GetMapping("/comfy/model/task-progress")
    public R<Integer> getComfyTaskProgress(@RequestParam String taskId) {
        return R.ok(sdComfyuiApiService.getComfyTaskProgress(taskId));
    }

}
