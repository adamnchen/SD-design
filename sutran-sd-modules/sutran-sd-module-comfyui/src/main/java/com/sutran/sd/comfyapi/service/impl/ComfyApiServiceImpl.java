package com.sutran.sd.comfyapi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.sutran.sd.comfyapi.domain.DrawingTaskInfo;
import com.sutran.sd.comfyapi.service.ComfyApiService;
import com.sutran.sd.common.core.service.OssService;
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.exception.WorkFlowErrorException;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.domain.SdUserTask;
import com.sutran.sd.draw.domain.pojo.*;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.enums.LoadBalanceStrategy;
import com.sutran.sd.draw.service.SdDrawNodeService;
import com.sutran.sd.draw.service.SdUserModelFileService;
import com.sutran.sd.draw.service.SdUserTaskService;
import com.sutran.sd.draw.utils.JsonUtils;
import com.sutran.sd.draw.websocket.ComfyWebsocketClient;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_NODE_TASK_MAP;
import static com.sutran.sd.common.constant.CacheConstants.DRAW_TASK_PROGRESS;
import static com.sutran.sd.draw.constants.CommonKey.JPG;
import static com.sutran.sd.draw.constants.CommonKey.SD;
import static com.sutran.sd.draw.mq.MqConstant.SD_COMFY_DRAW_QUEUE;

/**
 * ComfyUI客户端
 *
 * @author zj
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ComfyApiServiceImpl implements ComfyApiService {

    private final SdDrawNodeService sdDrawNodeService;
    private final SdUserTaskService sdUserTaskService;
    private final ComfyWebsocketClient comfyWebsocketClient;
    private final OssService ossService;
    private final UserService userService;
    private final SdUserModelFileService sdUserModelFileService;

    /**
     * 接收到绘图任务
     *
     * @param channel rabbitmq channel
     * @param message rabbitmq message
     */
    @RabbitListener(queues = SD_COMFY_DRAW_QUEUE)
    public void receiveTask(byte[] msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            DrawingTaskInfo taskInfo = JSON.parseObject(msg, DrawingTaskInfo.class);
            final String taskId = taskInfo.getTaskId();
            SdUserTaskVo task = sdUserTaskService.getDrawTaskInfoByTaskId(taskId);
            if (task == null) {
                log.error("[MQ消息消费]>>>>>>>>>任务不存在,任务ID: {}", taskId);
                // 归还绘图次数
                userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                return;
            }
            else {
                // 获取可用节点 以及 锁定节点任务
                SdDrawNode node = sdDrawNodeService.selectNodeAndLockNodeTask(LoadBalanceStrategy.WEIGHTED_LEAST_LOAD, taskId);
                if (node == null) {
                    // 没有可用节点,重新放回队列
                    log.warn("[MQ消息消费]>>>>>>>>>没有可用节点,任务ID: {}", taskId);
                    try {
                        channel.basicNack(deliveryTag, false, true);
                    } catch (IOException ex) {
                        log.error("[MQ消息消费]>>>>>>>>>MQ消息消费异常重新入队列异常,异常信息: ", ex);
                    }
                    return;
                }

                // 提交任务，返回ComfyUI内部任务ID
                String promptId = submitDrawTask(taskId, taskInfo.getFlow(), node);
                // 提交失败 或者 已有缓存任务
                if (StringUtils.isNotBlank(promptId)) {
                    // 修改存储节点任务ID以及开始时间
                    sdUserTaskService.startComfyTask(taskId, new Date(), promptId, node.getId());
                    // 获取历史任务信息
                    ComfyTaskHistoryInfo historyInfo = getTaskInfoById(promptId, node);
                    // 判断任务是否已完成或缓存，任务完成则直接生成图片数据
                    if (historyInfo!=null && historyInfo.getCompleted()!=null && historyInfo.getCompleted()) {
                        // 任务已完成
                        sdUserTaskService.completeComfyTask(taskId, new Date());
                        if (CollectionUtil.isEmpty(historyInfo.getOutputs())) {
                            // 归还绘图次数
                            userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                            return;
                        }
                        List<String> urlList = new ArrayList<>();
                        for (ComfyTaskImage image : historyInfo.getOutputs()) {
                            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                                UrlBuilder builder = UrlBuilder.of(node.getBaseUrl()).addPath("/view")
                                    .addQuery("filename", image.getFileName())
                                    .addQuery("type", image.getFolder())
                                    .addQuery("subfolder", image.getSubFolder());
                                HttpUtil.download(builder.build(), out, false);
                                OssClient storage = OssFactory.instance();
                                UploadResult uploadResult = storage.uploadSuffix(out.toByteArray(),JPG,"image/jpeg");
                                urlList.add(uploadResult.getUrl());
                                ossService.insertOssData(SD + DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),task.getBelongUserName());
                            } catch (Exception e) {
                                log.error("[任务输出图片][上传失败]>>>>>>>>>任务id: {},comfyui内部任务id: {},异常原因: ", taskId, promptId,e);
                                // 归还绘图次数
                                userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                            }
                        }
                        if (CollectionUtil.isNotEmpty(urlList)) {
                            sdUserModelFileService.asyncBatchInsert(task,urlList,null);
                        }
                        // 归还节点
                        RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, node.getId().toString());
                        return;
                    }

                    // 添加任务进度缓存
                    RedisUtils.setCacheMapValue(DRAW_TASK_PROGRESS, taskId, 0);
                    // 连接comfyui的websocket获取进度
                    String wsUrl = node.getBaseUrl().replace("https", "ws").replace("http", "ws") + "/ws?clientId=" + taskId;
                    comfyWebsocketClient.createComfyUiWebSocket(wsUrl, promptId, taskId);
                }
            }
            // 消费该消息
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            log.error("[MQ消息消费]>>>>>>>>>MQ消息消费异常,异常信息: ", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("[MQ消息消费]>>>>>>>>>MQ消息消费异常重新入队列异常,异常信息: ", ex);
            }
        }
    }

    /**
     * api: /prompt<br>
     * 提交图片生成任务
     *
     * @param taskId 自定义的任务id
     * @param flow   工作流
     * @param sdDrawNode  节点信息
     * @return ComfyUI内部任务id(promptId)
     */
    @Override
    public String submitDrawTask(String taskId, JSONObject flow, SdDrawNode sdDrawNode) {
        try{
            JSONObject param =  new JSONObject();
            param.put("client_id", taskId);
            param.put("prompt", flow);
            HttpRequest request = HttpRequest.post(sdDrawNode.getBaseUrl() + "/prompt").body(param.toJSONString(), "application/json").timeout(2000);
            //提交任务
            String resp = execHttpRequest(request);
            JsonNode jsonNode = JsonUtils.toJsonNode(resp);
            JsonNode taskIdNode = jsonNode.get("prompt_id");
            if (taskIdNode == null) {
                //任务提交错误 工作流节点出现错误
                throw new WorkFlowErrorException("工作流执行异常");
            }
            return taskIdNode.asText();
        }
        catch (Exception e){
            log.error("[提交任务]>>>>>>>>>提交任务异常,异常信息: ", e);
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, sdDrawNode.getId().toString());
            // 完成任务
            sdUserTaskService.failComfyTask(taskId, e.getMessage(), new Date());
            return null;
        }
    }

    /**
     * api: /prompt<br>
     * 获取服务器当前剩余任务列队的数量
     *
     * @param node 节点信息
     * @return ComfyUI内部任务id
     */
    @Override
    public ComfyTaskQueueRemaining getQueueRemaining(SdDrawNode node) {
        HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/prompt").timeout(2000);
        String historyInfo = execHttpRequest(request);
        JsonNode taskNode = JsonUtils.toJsonNode(historyInfo);
        return JsonUtils.toObject(taskNode, ComfyTaskQueueRemaining.class);
    }

    /**
     * api: /history<br>
     * 获得全部历史任务信息
     *
     * @return 历史任务信息 key为任务id value为任务信息
     */
    @Override
    public Map<String, ComfyTaskHistoryInfo> getTaskInfo(SdDrawNode node) {
        HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/history").timeout(2000);
        String historyInfo = execHttpRequest(request);
        return JsonUtils.toMapObject(historyInfo, ComfyTaskHistoryInfo.class);
    }

    /**
     * api: /history/{promptId}<br>
     * 获得某一个任务信息
     *
     * @param promptId 任务id
     * @return 历史任务信息
     */
    @Override
    public ComfyTaskHistoryInfo getTaskInfoById(String promptId, SdDrawNode node) {
        HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/history/" + promptId).timeout(2000);
        String historyInfo = execHttpRequest(request);
        JsonNode taskNode = JsonUtils.toJsonNode(historyInfo).get(promptId);
        return JsonUtils.toObject(taskNode, ComfyTaskHistoryInfo.class);
    }

    /**
     * api: /history/{promptId}<br>
     * 获得某一个任务信息
     *
     * @param promptId comfyUI内部任务id
     * @return 历史任务信息
     */
    @Override
    public ComfyTaskHistoryInfo getTaskInfoById(String promptId) {
        SdUserTask task = sdUserTaskService.getTaskInfoByPromptId(promptId);
        if (task == null) {
            throw new TaskErrorException("任务不存在!");
        }
        SdDrawNode node = sdDrawNodeService.findById(task.getNodeId());
        return getTaskInfoById(promptId, node);
    }

    /**
     * api: /queue
     * 获得当前队列状态
     *
     * @return 当前队列状态
     */
    @Override
    public ComfyTaskQueueStatus getQueueStatus(SdDrawNode node) {
        HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/queue").timeout(2000);
        String queueStatusInfo = execHttpRequest(request);
        return JsonUtils.toObject(queueStatusInfo, ComfyTaskQueueStatus.class);
    }

    /**
     * api: /queue<br>
     * 删除一个正在等待的绘画任务
     *
     * @param promptId comfyUI内部任务id
     * @param node 节点信息
     */
    @Override
    public void cancelDrawTask(String promptId, SdDrawNode node) {
        String param = String.format("{\"delete\":[\"%s\"]}", promptId);
        HttpRequest request = HttpRequest.post(node.getBaseUrl() + "/queue").body(param, "application/json").timeout(2000);
        execHttpRequest(request);
    }

    /**
     * api: /interrupt<br>
     * 取消当前运行的任务
     */
    @Override
    public void cancelRunningTask(SdDrawNode node) {
        HttpRequest request = HttpRequest.post(node.getBaseUrl() + "/interrupt").timeout(2000);
        execHttpRequest(request);
    }

    /**
     * api: /system_stats<br>
     * 获取系统运行信息
     *
     * @param node 节点信息
     * @return 系统运行环境 硬件设备
     */
    @Override
    public ComfySystemEnvironment getSystemDeviceInfo(SdDrawNode node) {
        HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/system_stats").timeout(2000);
        String systemStatsInfo = execHttpRequest(request);
        return JsonUtils.toObject(systemStatsInfo, ComfySystemEnvironment.class);
    }

    /**
     * api: /upload/image<br>
     * 上传图片到ComfyUI服务器
     *
     * @param node 节点信息
     * @param file 图片对象
     * @return 上传后的图片信息
     */
    @Override
    public ComfyTaskImage uploadImage(File file, SdDrawNode node) {
        HttpRequest request = HttpRequest.post(node.getBaseUrl() + "/upload/image").form("file", file).timeout(2000);
        String resp = execHttpRequest(request);
        return JsonUtils.toObject(resp, ComfyTaskImage.class);
    }

    /**
     * api: /view
     * 获取输入的图片文件
     *
     * @param imageName 图片文件名
     * @return 图片文件二进制数组
     */
    @Override
    public byte[] getInputImageFile(String imageName, SdDrawNode node) {
        return getImageFile(imageName, "input",node);
    }

    /**
     * api: /view
     * 获取生成的图片文件
     *
     * @param imageName 图片文件名
     * @return 图片文件二进制数组
     */
    @Override
    public byte[] getOutputImageFile(String imageName, SdDrawNode node) {
        return getImageFile(imageName, "output",node);
    }

    /**
     * 自动处理Comfy任务
     *
     * @param nodeId 节点ID
     * @param taskId 任务ID
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void autoDealComfyTask(String nodeId, String taskId) {
        SdUserTaskVo taskVo = sdUserTaskService.getDrawTaskInfoByTaskId(taskId);
        if (taskVo == null) {
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, nodeId);
            // 清除缓存中的任务进度
            RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, taskId);
            // 关闭websocket
            comfyWebsocketClient.closeComfyUiWebSocket(taskId);
            return;
        }
        SdDrawNode node = sdDrawNodeService.findById(Long.parseLong(nodeId));
        if (node == null) {
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, nodeId);
            // 清除缓存中的任务进度
            RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, taskId);
            // 关闭websocket
            comfyWebsocketClient.closeComfyUiWebSocket(taskId);
            return;
        }
        ComfyTaskHistoryInfo taskInfo = getTaskInfoById(taskId, node);
        if (taskInfo == null) {
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, nodeId);
            // 清除缓存中的任务进度
            RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, taskId);
            // 关闭websocket
            comfyWebsocketClient.closeComfyUiWebSocket(taskId);
            return;
        }
        // 任务已完成
        if (taskInfo.getCompleted() != null && taskInfo.getCompleted()) {
            sdUserTaskService.completeComfyTask(taskId, new Date());
            if (CollectionUtil.isEmpty(taskInfo.getOutputs())) {
                return;
            }
            List<String> urlList = new ArrayList<>();
            for (ComfyTaskImage image : taskInfo.getOutputs()) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    UrlBuilder builder = UrlBuilder.of(node.getBaseUrl()).addPath("/view")
                        .addQuery("filename", image.getFileName())
                        .addQuery("type", image.getFolder())
                        .addQuery("subfolder", image.getSubFolder());
                    HttpUtil.download(builder.build(), out, false);
                    OssClient storage = OssFactory.instance();
                    UploadResult uploadResult = storage.uploadSuffix(out.toByteArray(),JPG,"image/jpeg");
                    urlList.add(uploadResult.getUrl());
                    ossService.insertOssData(SD + DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),taskVo.getBelongUserName());
                } catch (Exception e) {
                    log.error("[任务输出图片][上传失败]>>>>>>>>>任务id: {},comfyui内部任务id: {},异常原因: ", taskId, taskVo.getPromptId(), e);
                }
            }
            if (CollectionUtil.isNotEmpty(urlList)) {
                sdUserModelFileService.asyncBatchInsert(taskVo,urlList,null);
            }
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, node.getId().toString());
        }
    }

    /**
     * 获取图片文件
     *
     * @param imageName 图片名
     * @param folder    图片所在文件夹
     * @return 图片文件二进制数组
     */
    private byte[] getImageFile(String imageName, String folder, SdDrawNode node) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            UrlBuilder builder = UrlBuilder.of(node.getBaseUrl())
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
