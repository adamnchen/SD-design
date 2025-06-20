package com.sutran.sd.sdapi.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.WxMsgDto;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainAdditionTagDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainPreImgDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainTagDelDto;
import com.sutran.sd.sdapi.domain.vo.TrainProcessDataVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskVo;
import com.sutran.sd.sdapi.modules.system.SdTrainService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * SD-lora模型训练API
 * @author zj
 * @date 2024-02-27
 */
@RestController
@RequestMapping("/sd/train")
@RequiredArgsConstructor
public class SdTrainController {
    private final SdTrainService sdTrainService;
    private final WxMpService wxMpService;

    /**
     * SD任务-获取当前登录人已发起的训练任务
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @return 任务集合
     */
    @GetMapping("/my-task")
    public R<List<TrainTaskVo>> getTrainTasks(@RequestParam Integer newStatus){
        return R.ok(sdTrainService.getTrainTasks(newStatus, LoginHelper.getUserId()));
    }

    /**
     * SD任务-获取当前任务状态
     * @param preTaskId 预处理任务ID
     * @return 任务状态
     */
    @GetMapping("/getStatus")
    public R<TrainTaskStatusVo> getSdTaskStatus(@RequestParam String preTaskId) {
        return R.ok("操作成功", sdTrainService.getSdTaskStatus(preTaskId));
    }

    /**
     * SD预处理-查询图片列表
     */
    @GetMapping("/pre-img-list")
    public R<JSONObject> getPreImgList(@RequestParam(required = false) String preTaskId) throws IOException {
        return R.ok("操作成功", sdTrainService.getPreImgList(preTaskId));
    }

    /**
     * SD预处理-提交图片
     */
    @PostMapping("/pre-img")
    public R<String> submitPreImg(@RequestParam MultipartFile[] images,
                                  @RequestParam(required = false) Double threshold,
                                  @RequestParam(required = false) String interrogatorModel,
                                  @RequestParam(required = false) String batchOutputActionOnConflict) throws IOException {
        String taskId = sdTrainService.submitPreImg(images,threshold,interrogatorModel,batchOutputActionOnConflict);
        return R.ok("操作成功",taskId);
    }

    /**
     * SD预处理-获取预处理图片进度
     */
    @GetMapping("/pre-img/progress")
    public R<Boolean> getPreImgProgress(@RequestParam String preTaskId) {
        return R.ok(sdTrainService.getPreImgProgress(preTaskId));
    }

    /**
     * SD预处理-删除图片
     */
    @DeleteMapping("/pre-img-info")
    public R<Void> delPreImg(@RequestParam String imgUrl,@RequestParam(required = false) String preTaskId) {
        sdTrainService.delPreImg(imgUrl,preTaskId);
        return R.ok();
    }

    /**
     * SD预处理-修改指定图片的标签内容
     */
    @PutMapping("/pre-img-tag")
    public R<Void> modifyPreImgTag(@RequestBody SdTrainPreImgDto dto) {
        sdTrainService.modifyPreImgTag(dto);
        return R.ok();
    }

    /**
     * SD预处理-添加共性词到全部预处理图片中
     */
    @PostMapping("/addition-tag")
    public R<Void> insertAdditionTag(@RequestBody SdTrainAdditionTagDto dto) {
        sdTrainService.insertAdditionTag(dto);
        return R.ok();
    }

    /**
     * SD预处理-从指定预处理图片中删除指定共性词和标签
     */
    @DeleteMapping("/pre-img-tag")
    public R<Void> delPreImgTag(@RequestParam String preTaskId,@RequestParam String imgUrl,@RequestParam String tag) {
        SdTrainTagDelDto dto = new SdTrainTagDelDto().setPreTaskId(preTaskId).setImgUrl(imgUrl).setTag(tag);
        sdTrainService.delPreImgTag(dto);
        return R.ok();
    }

    /**
     * SD训练-训练Lora模型(请求体添加参数，共性词用逗号隔开: {"additionalTags:: "A,B" ,"preTaskId": ""})
     */
    @PostMapping("/sd-lora")
    public R<Void> trainSdLora(@RequestBody SdTrainLoraDto dto) {
        sdTrainService.trainSdLora(dto);
        return R.ok();
    }

    /**
     * SD训练-查询训练进度
     */
    @GetMapping("/progress")
    public R<TrainProcessDataVo> trainProgress(@RequestParam String taskId) {
        return R.ok("操作成功", sdTrainService.trainProgress(taskId));
    }

    /**
     * SD训练-查询训练进度V2
     */
    @GetMapping("/v2/progress")
    public R<TrainProcessDataVo> trainProgressV2(@RequestParam String taskId) {
        return R.ok("操作成功", sdTrainService.trainProgressV2(taskId));
    }

    /**
     * 测试消息推送
     */
    @PostMapping("/test-msg")
    @SaIgnore
    public void testMsg(@RequestBody WxMsgDto data) throws WxErrorException {
        WxMpTemplateMessage message = WxMpTemplateMessage.builder().toUser(data.getOpenId()).templateId(data.getTemplateId()).url(data.getUrl()).build();
        for (String key : data.getParam().keySet()) {
            message.addData(new WxMpTemplateData(key, data.getParam().getStr(key)));
        }
        wxMpService.getTemplateMsgService().sendTemplateMsg(message);
    }

}
