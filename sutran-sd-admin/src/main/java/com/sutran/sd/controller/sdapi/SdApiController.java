package com.sutran.sd.controller.sdapi;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.sdapi.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.sdapi.domain.dto.task.SdUserTaskPageDto;
import com.sutran.sd.sdapi.modules.webui.SdApiService;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelFileVo;
import com.sutran.sd.sdapi.modules.system.vo.SdUserTaskVo;
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
    private final SdApiService sdApiService;

    /**
     * SD-文生图
     */
    @PostMapping("/txt2img")
    public R<String> txt2img(@Validated @RequestBody SdText2ImgDto dto) {
        String taskId = sdApiService.txt2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * SD-图生图
     */
    @PostMapping("/img2img")
    public R<String> img2img(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * SD-图生图(局部重绘)
     */
    @PostMapping("/img2img/mask")
    public R<String> img2imgOfMask(@Validated @RequestBody SdImg2ImgDto dto) {
        String taskId = sdApiService.img2img(dto);
        return R.ok("操作成功",taskId);
    }

    /**
     * SD-获取当前用户任务列表
     */
    @GetMapping("/task/list")
    public TableDataInfo<SdUserTaskVo> userTaskList(SdUserTaskPageDto dto) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(dto.getPageNum());
        pageQuery.setPageSize(dto.getPageSize());
        pageQuery.setOrderByColumn(dto.getOrderByColumn());
        pageQuery.setIsAsc(dto.getIsAsc());
        return sdApiService.userDrawTaskList(pageQuery,dto.getCategory(), dto.getStatus());
    }

    /**
     * SD-根据taskId获取当前用户绘图数据列表
     */
    @GetMapping("/model-file/list")
    public R<List<SdUserModelFileVo>> userModelFileList(@RequestParam String taskId) {
        return R.ok(sdApiService.listUserModelFile(taskId));
    }

    /**
     * SD-批量压缩下载绘图图片
     */
    @GetMapping("/task/download")
    public void batchDownloadModelFile(@RequestParam String taskId, HttpServletResponse response) throws IOException {
        sdApiService.batchDownloadModelFile(taskId,response);
    }

    /**
     * SD-单个下载绘图图片
     */
    @GetMapping("/task/img/download")
    public void downloadModelFile(@RequestParam String imgUrl, HttpServletResponse response) throws IOException {
        if (StrUtil.isEmpty(imgUrl)) {
            return;
        }
        sdApiService.downloadUserModelFile(imgUrl,response);
    }

    /**
     * SD-删除任务中的单张图片
     */
    @DeleteMapping("/model-file")
    public R<Void> deleteModelFile(@RequestParam String id) {
        sdApiService.removeUserModelFile(Collections.singletonList(id));
        return R.ok();
    }

    /**
     * SD-获取当前用户正在进行的任务taskId
     */
    @GetMapping("/doing-task")
    public R<String> getDoingTask(@RequestParam Integer category) {
        return R.ok("操作成功",sdApiService.getDoingTaskId(category));
    }

    /**
     * SD-删除队列中、已完成、已失败的任务
     */
    @DeleteMapping("/task")
    public R<String> deleteTask(@RequestParam String taskId) {
        sdApiService.deleteTaskById(taskId);
        return R.ok();
    }

    /**
     * SD-进度查询
     */
    @GetMapping("/process")
    public R<JSONObject> getProcess(@RequestParam String taskId) {
        return R.ok(sdApiService.getProcess(taskId));
    }

}
