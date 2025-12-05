package com.sutran.sd.controller.sdapi;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson.JSONArray;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.sutran.sd.common.annotation.RequireMember;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.WxMsgDto;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.draw.domain.bo.TrainCaptionBo;
import com.sutran.sd.draw.domain.dto.train.SdTrainAdditionTagDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainPreImgDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainTagDelDto;
import com.sutran.sd.draw.domain.vo.*;
import com.sutran.sd.draw.service.SdTrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * SD-lora模型训练API
 * @author zj
 * @date 2024-02-27
 */
@Slf4j
@RestController
@RequestMapping("/sd/train")
@RequiredArgsConstructor
public class SdTrainController {
    private final SdTrainService sdTrainService;
    private final WxMpService wxMpService;

    /**
     * [LoraScripts][V2]SD任务-分页获取已发起的训练任务
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @return 任务集合
     */
    @ApiOperationSupport(order = 1)
    @GetMapping("/v2/my-task/page")
    public TableDataInfo<TrainTaskVo> pageTrainTasksV2(@RequestParam(required = false) Integer newStatus,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "20") int pageSize){
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        return sdTrainService.getTrainTasksV2(pageQuery, newStatus, LoginHelper.getUserId());
    }

    /**
     * [LoraScripts]SD任务-获取指定任务状态
     * @param preTaskId 预处理任务ID
     * @return 任务状态
     */
    @ApiOperationSupport(order = 2)
    @GetMapping("/getStatus")
    public R<TrainTaskStatusVo> getSdTaskStatus(@RequestParam String preTaskId) {
        return R.ok("操作成功", sdTrainService.getSdTaskStatus(preTaskId));
    }

    /**
     * [LoraScripts]SD预处理-查询标签处理的图片列表
     */
    @ApiOperationSupport(order = 3)
    @GetMapping("/pre-img-list")
    public R<TrainPreImgTaskVo> getPreImgList(@RequestParam(required = false) String preTaskId) throws IOException {
        return R.ok("操作成功", sdTrainService.getPreImgList(preTaskId));
    }

    /**
     * [LoraScripts][V2]SD预处理-查询标签处理的图片列表
     */
    @ApiOperationSupport(order = 3)
    @GetMapping("/v2/pre-img-list")
    public R<TrainPreImgTaskVo> getPreImgListV2(@RequestParam String preTaskId) throws IOException {
        return R.ok("操作成功", sdTrainService.getPreImgListV2(preTaskId));
    }

    /**
     * [LoraScripts]SD预处理-提交图片
     */
    @ApiOperationSupport(order = 5)
    @PostMapping("/pre-img")
    public R<String> submitPreImg(@RequestParam MultipartFile[] images,
                                  @RequestParam(required = false) Double threshold,
                                  @RequestParam(required = false) String interrogatorModel,
                                  @RequestParam(required = false) String batchOutputActionOnConflict) throws IOException {
        String taskId = sdTrainService.submitPreImg(images,threshold,interrogatorModel,batchOutputActionOnConflict);
        return R.ok("操作成功",taskId);
    }

    /**
     * [LoraScripts][V2]SD预处理-提交图片
     */
    @ApiOperationSupport(order = 4)
    @PostMapping("/v2/pre-img")
    public R<String> submitPreImgV2(@RequestParam MultipartFile[] images,
                                    @RequestParam(required = false) Double threshold,
                                    @RequestParam(required = false) String interrogatorModel,
                                    @RequestParam(required = false) String batchOutputActionOnConflict) throws IOException {
        String taskId = sdTrainService.submitPreImgV2(images,threshold,interrogatorModel,batchOutputActionOnConflict);
        return R.ok("操作成功",taskId);
    }

    /**
     * [LoraScripts]SD预处理-获取预处理图片进度
     */
    @ApiOperationSupport(order = 6)
    @GetMapping("/pre-img/progress")
    public R<Boolean> getPreImgProgress(@RequestParam String preTaskId) {
        return R.ok(sdTrainService.getPreImgProgress(preTaskId));
    }

    /**
     * [LoraScripts][V2]SD预处理-获取预处理图片进度
     */
    @ApiOperationSupport(order = 7)
    @GetMapping("/v2/pre-img/progress")
    public R<Integer> getPreImgProgressV2(@RequestParam String preTaskId) {
        return R.ok(sdTrainService.getPreImgProgressV2(preTaskId));
    }

    /**
     * [LoraScripts]SD预处理-删除图片
     */
    @ApiOperationSupport(order = 7)
    @DeleteMapping("/pre-img-info")
    public R<Void> delPreImg(@RequestParam String imgUrl,@RequestParam String preTaskId) {
        sdTrainService.delPreImg(imgUrl,preTaskId);
        return R.ok();
    }

    /**
     * [LoraScripts]SD预处理-修改指定图片的标签内容
     */
    @ApiOperationSupport(order = 8)
    @PutMapping("/pre-img-tag")
    public R<Void> modifyPreImgTag(@RequestBody SdTrainPreImgDto dto) {
        sdTrainService.modifyPreImgTag(dto);
        return R.ok();
    }

    /**
     * [LoraScripts]SD预处理-添加共性词到全部预处理图片中
     */
    @ApiOperationSupport(order = 9)
    @PostMapping("/addition-tag")
    public R<Void> insertAdditionTag(@RequestBody SdTrainAdditionTagDto dto) {
        sdTrainService.insertAdditionTag(dto);
        return R.ok();
    }

    /**
     * [LoraScripts]SD预处理-从指定预处理图片中删除指定共性词和标签
     */
    @ApiOperationSupport(order = 10)
    @DeleteMapping("/pre-img-tag")
    public R<Void> delPreImgTag(@RequestParam String preTaskId,@RequestParam String imgUrl,@RequestParam String tag) {
        SdTrainTagDelDto dto = new SdTrainTagDelDto().setPreTaskId(preTaskId).setImgUrl(imgUrl).setTag(tag);
        sdTrainService.delPreImgTag(dto);
        return R.ok();
    }

    /**
     * [LoraScripts]SD训练-训练Lora模型(请求体添加参数，共性词用逗号隔开: {"additionalTags:: "A,B" ,"preTaskId": ""})
     */
    @ApiOperationSupport(order = 11)
    @PostMapping("/sd-lora")
    @com.sutran.sd.common.annotation.RequireMember(value = "AI模型训练", newUserBenefit = {com.sutran.sd.common.annotation.RequireMember.NewUserBenefitType.DRAW})
    public R<Void> trainSdLora(@RequestBody SdTrainLoraDto dto) {
        sdTrainService.trainSdLora(dto);
        return R.ok();
    }

    /**
     * [LoraScripts][V2]SD训练-训练Lora模型(请求体添加参数，共性词用逗号隔开: {"additionalTags:: "A,B" ,"preTaskId": ""})
     */
    @ApiOperationSupport(order = 11)
    @PostMapping("/v2/sd-lora")
    @com.sutran.sd.common.annotation.RequireMember(value = "AI模型训练", newUserBenefit = {com.sutran.sd.common.annotation.RequireMember.NewUserBenefitType.DRAW})
    public R<Void> trainSdLoraV2(@RequestBody SdTrainLoraDto dto) {
        sdTrainService.trainSdLoraV2(dto);
        return R.ok();
    }

    /**
     * [LoraScripts]SD训练-查询训练进度
     */
    @ApiOperationSupport(order = 12)
    @GetMapping("/progress")
    public R<TrainProcessDataVo> trainProgress(@RequestParam String taskId) {
        return R.ok("操作成功", sdTrainService.trainProgress(taskId));
    }

    /**
     * [LoraScripts][V2]SD训练-查询训练进度
     */
    @ApiOperationSupport(order = 13)
    @GetMapping("/v2/progress")
    public R<TrainProcessDataVo> trainProgressV2(@RequestParam String taskId) {
        return R.ok("操作成功", sdTrainService.trainProgressV2(taskId));
    }


    /**
     * [FluxGym]提交预处理(图片识别)
     * @param images        图片集合
     * @param loraName      训练模型名称(用于触发词)
     * @return 识别结果
     */
    @ApiOperationSupport(order = 14)
    @PostMapping("/fluxgym/img-identify")
    public R<FluxgymImgDealResultVo> imgIdentify(@RequestParam("images") MultipartFile[] images, @RequestParam("loraName") String loraName) {
        return R.ok("操作成功",sdTrainService.imgIdentifyTask(images,loraName));
    }

    /**
     * [FluxGym][V2]提交训练
     * @param images        图片集合
     * @param loraName      训练模型名称(用于触发词)
     * @param captions      图片述词[{"caption":"描述词英文","captionZh":"描述词中文"}]
     * @param modelTag      模型标签(多个用逗号隔开)
     * @param isOpen        是否公开[0-否,1-是]
     * @param modelDesc     模型描述
     * @throws IOException  图片IO异常
     * @return 任务id
     */
    @ApiOperationSupport(order = 15)
    @PostMapping(value = "/fluxgym/start-train/v2",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireMember(value = "AI模型训练", newUserBenefit = {RequireMember.NewUserBenefitType.DRAW})
    public R<String> starTrainV2(@RequestParam("images") MultipartFile[] images,
                                 @RequestParam("loraName") String loraName,
                                 @RequestParam("captions") String captions,
                                 @RequestParam(value = "modelTag",required = false) String modelTag,
                                 @RequestParam(value = "isOpen",required = false) Integer isOpen,
                                 @RequestParam(value = "modelDesc",required = false) String modelDesc) throws IOException {
        List<TrainCaptionBo> captionList = JSONArray.parseArray(captions, TrainCaptionBo.class);
        return R.ok("操作成功",sdTrainService.startTrainTaskV2(images,loraName,captionList,modelTag,isOpen,modelDesc));
    }

    /**
     * [FluxGym]查询训练进度
     * @param taskId 任务ID
     */
    @ApiOperationSupport(order = 16)
    @GetMapping("/fluxgym/progress")
    public R<FluxgymTrainProgressVo> getFluxgymProgress(@RequestParam String taskId){
        return R.ok("操作成功",sdTrainService.getFluxgymProgress(taskId, null, false));
    }

    /**
     * [FluxGym]查询训练任务状态
     * @param taskId 任务ID
     */
    @ApiOperationSupport(order = 16)
    @GetMapping("/fluxgym/task-status")
    public R<FluxgymTaskStatusVo> getTaskInfo(@RequestParam String taskId){
        return R.ok("操作成功",sdTrainService.getFluxgymTaskStatus(taskId));
    }

    /**
     * [FluxGym]当前用户正在训练的任务ID
     */
    @ApiOperationSupport(order = 17)
    @GetMapping("/fluxgym/doing-task")
    public R<String> getDoingTask(){
        return R.ok("操作成功",sdTrainService.getDoingFluxgymTask(LoginHelper.getUserId()));
    }

    /**
     * [FluxGym]分页获取当前用户的训练任务
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @return 任务集合
     */
    @ApiOperationSupport(order = 18)
    @GetMapping("/fluxgym/my-task/page")
    public TableDataInfo<TrainTaskVo> getFluxgymTrainTasks(@RequestParam(required = false) Integer newStatus,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "20") int pageSize){
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);
        return sdTrainService.getFluxgymTrainTasks(pageQuery, newStatus, LoginHelper.getUserId());
    }

    /**
     * 测试消息推送
     */
    @PostMapping("/test-msg")
    @ApiOperationSupport(order = 19)
    @SaIgnore
    public void testMsg(@RequestBody WxMsgDto data) throws WxErrorException {
        WxMpTemplateMessage message = WxMpTemplateMessage.builder().toUser(data.getOpenId()).templateId(data.getTemplateId()).url(data.getUrl()).build();
        for (String key : data.getParam().keySet()) {
            message.addData(new WxMpTemplateData(key, data.getParam().getStr(key)));
        }
        wxMpService.getTemplateMsgService().sendTemplateMsg(message);
    }

}
