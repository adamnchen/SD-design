package com.sutran.sd.sdapi.modules.comfyui.impl;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.JsonUtils;
import com.sutran.sd.sdapi.modules.comfyui.IComfyApiService;
import com.sutran.sd.sdapi.modules.comfyui.entity.*;
import com.sutran.sd.sdapi.modules.system.SdUserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

import static com.sutran.sd.sdapi.mq.MqConstant.*;

/**
 * @author zj
 * @date 2025-03-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ComfyApiApiService implements IComfyApiService {

    private final SdUserTaskService sdUserTaskService;
    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * api: /prompt<br>
     * 提交图片生成任务
     * @param flow   工作流
     * @return ComfyUI内部任务id
     */
    @Override
    public String submitDrawTask(ComfyWorkFlow flow) {
        final Long userId = LoginHelper.getUserId();
        final String userName = LoginHelper.getUsername();
        // 获取当前用户是否存在正在进行的任务
        String taskId = sdUserTaskService.getDoingTask(LoginHelper.getUserId(),1);
        if (StrUtil.isNotEmpty(taskId)) {
            throw new ServiceException("当前用户存在未完成的任务!",501,taskId);
        }
        // 获取绘图剩余次数
        Integer drawNum = userService.selectDrawNumById(LoginHelper.getUserId());
        //if (drawNum!=null && drawNum<dto.getBatch_size()) {
        //    throw new ServiceException("余额不足,请联系管理员!");
        //}
        // 生成客户端ID 和 任务数据
        taskId = IdUtil.getSnowflakeNextIdStr();
        ComfyTaskInfo taskInfo = new ComfyTaskInfo().setClientId(taskId).setFlow(flow);
        // 新增任务
        sdUserTaskService.insertTask(taskId,userId,userName,3,0);
        // 请求放入消息队列中
        //rabbitTemplate.convertAndSend(SD_COMFY_DRAW_EXCHANGE,SD_COMFY_DRAW_ROUTING_KEY,msg,new CorrelationData(taskId));
        return taskId;
    }


    /**
     * api: /history<br>
     * 获得全部历史任务信息
     *
     * @return 历史任务信息 key为任务id value为任务信息
     */
    @Override
    public Map<String, ComfyTaskHistoryInfo> getTaskInfo() {
        return null;
    }

    /**
     * api: /history/{comfyTaskId}<br>
     * 获得某一个任务信息
     *
     * @param comfyTaskId 任务id
     * @return 历史任务信息
     */
    @Override
    public ComfyTaskHistoryInfo getTaskInfoById(String comfyTaskId) {
        return null;
    }

    /**
     * api: /queue
     * 获得当前队列状态
     *
     * @return 当前队列状态
     */
    @Override
    public ComfyTaskQueueStatus getQueueStatus() {
        return null;
    }

    /**
     * api: /queue<br>
     * 删除一个正在等待的绘画任务
     *
     * @param comfyTaskId comfyUI内部任务id
     */
    @Override
    public void cancelDrawTask(String comfyTaskId) {

    }

    /**
     * api: /interrupt<br>
     * 取消当前运行的任务
     */
    @Override
    public void cancelRunningTask() {

    }

    /**
     * api: /system_stats<br>
     * 获取系统运行信息
     *
     * @return 系统运行环境 硬件设备
     */
    @Override
    public ComfySystemEnvironment getSystemDeviceInfo() {
        return null;
    }

    /**
     * api: /upload/image<br>
     * 上传图片到ComfyUI服务器
     *
     * @param file 图片对象
     * @return 上传后的图片信息
     */
    @Override
    public ComfyTaskImage uploadImage(File file) {
        return null;
    }

    /**
     * api: /view
     * 获取输入的图片文件
     *
     * @param imageName 图片文件名
     * @return 图片文件二进制数组
     */
    @Override
    public byte[] getInputImageFile(String imageName) {
        return getImageFile(imageName, "input");
    }

    /**
     * api: /view
     * 获取生成的图片文件
     *
     * @param imageName 图片文件名
     * @return 图片文件二进制数组
     */
    @Override
    public byte[] getOutputImageFile(String imageName) {
        return getImageFile(imageName, "output");
    }

    /**
     * 获取图片文件
     *
     * @param imageName 图片名
     * @param folder    图片所在文件夹
     * @return 图片文件二进制数组
     */
    private byte[] getImageFile(String imageName, String folder) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            UrlBuilder builder = UrlBuilder.of("")
                .addQuery("filename", imageName)
                .addQuery("type", "input")
                .addQuery("type", folder)
                .addPath("/view");
            HttpUtil.download(builder.build(), out, false);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行request 并自动关闭response
     *
     * @param request request对象
     * @return response中的body
     */
    private String execHttpRequest(HttpRequest request) {
        try (HttpResponse response = request.execute()) {
            return response.body();
        }
    }
}
