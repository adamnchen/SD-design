package com.sutran.sd.draw.service.impl;

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
import com.sutran.sd.common.core.service.UserService;
import com.sutran.sd.common.exception.TaskErrorException;
import com.sutran.sd.common.exception.WorkFlowErrorException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.FileUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.domain.SdFlow;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.bo.DrawingTaskInfo;
import com.sutran.sd.draw.domain.bo.ImageInfoBo;
import com.sutran.sd.draw.domain.pojo.*;
import com.sutran.sd.draw.domain.vo.ComfyUserModelFileVo;
import com.sutran.sd.draw.domain.vo.ComfyuiImageToolVo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.enums.ImageType;
import com.sutran.sd.draw.enums.LoadBalanceStrategy;
import com.sutran.sd.draw.service.*;
import com.sutran.sd.draw.utils.JsonUtils;
import com.sutran.sd.draw.websocket.ComfyWebsocketClient;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import com.sutran.sd.system.domain.vo.SysOssVo;
import com.sutran.sd.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.sutran.sd.common.constant.CacheConstants.*;
import static com.sutran.sd.draw.constants.CommonKey.JPG;
import static com.sutran.sd.draw.constants.CommonKey.SD;
import static com.sutran.sd.framework.mq.MqConstant.*;

/**
 * ComfyUI客户端
 *
 * @author zj
 */
@SuppressWarnings({"AlibabaAvoidComplexCondition", "AlibabaUndefineMagicConstant"})
@Service
@Slf4j
@RequiredArgsConstructor
public class SdComfyuiApiServiceImpl implements SdComfyuiApiService {

    private final SdDrawNodeService sdDrawNodeService;
    private final SdUserTaskService sdUserTaskService;
    private final ComfyWebsocketClient comfyWebsocketClient;
    private final SdUserModelFileService sdUserModelFileService;
    private final ISysOssService sysOssService;
    private final UserService userService;
    private final SdFlowService sdFlowService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 查询固定工作流列表
     * @return 工作流列表
     */
    @Override
    public List<ComfyuiImageToolVo> queryFixedFlowList() {
        return sdFlowService.queryFixedFlowList();
    }

    /**
     * 提交模型生图任务
     * @param modelTaskBo 任务参数
     * @return 任务id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitComfyModelTask(ComfyModelTaskSubmitBo modelTaskBo) {
        final Long userId = LoginHelper.getUserId();
        final String userName = LoginHelper.getUsername();
        if (StringUtils.isBlank(modelTaskBo.getBatchSize()) || Integer.parseInt(modelTaskBo.getBatchSize())<=0) {
            throw new TaskErrorException("生图数量至少1张");
        }
        final int batchSize = Integer.parseInt(modelTaskBo.getBatchSize());
        // 根据模型类型获取工作流
        SdFlow sdFlow = sdFlowService.getNoFixedFlow(modelTaskBo.getModelType());
        if (sdFlow == null || StringUtils.isBlank(sdFlow.getFlow())) {
            throw new TaskErrorException(String.format("未找到模型类型为[%s]的工作流", modelTaskBo.getModelType()));
        }
        // 校验生图数量,获取当前用户对应的会员的剩余数量并扣除本次绘图数量
        userService.checkDrawNumOfMember(userId,batchSize);

        // 生图任务落库
        final String taskId = IdUtil.getSnowflakeNextIdStr();

        // 替换lora模型和强度(强度和生图数量是数值型，需要去除引号)
        String flow = sdFlow.getFlow().replace("{{modelName}}",modelTaskBo.getModelName())
            .replace("\"{{modelStrength}}\"",modelTaskBo.getModelStrength())
            .replace("\"{{batchSize}}\"",modelTaskBo.getBatchSize())
            .replace("{{fileNamePrefix}}",taskId);
        String prompt = StringUtils.isBlank(modelTaskBo.getPrompt())?sdFlow.getInitPrompt():modelTaskBo.getPrompt();
        String promptZh = StringUtils.isBlank(modelTaskBo.getPromptZh())?sdFlow.getInitPromptZh():modelTaskBo.getPromptZh();
        if (prompt!=null) {
            flow = flow.replace("{{prompt}}",prompt);
        }

        sdUserTaskService.addComfyTask(taskId,userId,userName,flow,prompt,promptZh,null);
        // 生图任务存放到MQ队列
        DrawingTaskInfo taskInfo = new DrawingTaskInfo(taskId, flow,10,userId,batchSize,null);
        submitComfyTaskToQueue(taskInfo);
        return taskId;
    }

    /**
     * 提交工作流生图任务
     *
     * @param flowId   工作流id
     * @param prompt 描述词(英文)
     * @param promptZh 描述词(中文)
     * @param imageList 图片列表
     * @return 任务id
     */
    @Override
    public String submitComfyFlowTask(String flowId, String prompt, String promptZh, MultipartFile[] imageList) throws IOException {
        final Long userId = LoginHelper.getUserId();
        final String userName = LoginHelper.getUsername();

        // 根据模型类型获取工作流
        SdFlow sdFlow = sdFlowService.getFixedFlowById(flowId);
        if (sdFlow == null || StringUtils.isBlank(sdFlow.getFlow())) {
            throw new TaskErrorException("未找到工作流");
        }
        if (sdFlow.getDrawNum() == null || sdFlow.getDrawNum() <= 0) {
            throw new TaskErrorException(String.format("工作流[%s]未配置生图数量", sdFlow.getName()));
        }

        // 校验生图数量,获取当前用户对应的会员的剩余数量并扣除本次绘图数量
        userService.checkDrawNumOfMember(userId, sdFlow.getDrawNum());

        // 处理参考图片
        List<String> imageUrls = new ArrayList<>();
        List<ImageInfoBo> images = new ArrayList<>();
        for (MultipartFile file : imageList) {
            if (file==null || file.isEmpty()) {
                continue;
            }
            SysOssVo ossVo = sysOssService.upload(file);
            imageUrls.add(ossVo.getUrl());
            images.add(new ImageInfoBo().setImageName(file.getOriginalFilename()).setContentType(file.getContentType()).setFileData(file.getBytes()));
        }
        // 生图任务落库
        final String taskId = IdUtil.getSnowflakeNextIdStr();

        // 处理工作流引导词
        String flowStr = sdFlow.getFlow().replace("{{fileNamePrefix}}",taskId);
        // 只判断是否为null，空字符串还是需要替换的
        prompt = StringUtils.isBlank(prompt)?sdFlow.getInitPrompt():prompt;
        promptZh = StringUtils.isBlank(promptZh)?sdFlow.getInitPromptZh():promptZh;
        if (prompt!=null) {
            flowStr = flowStr.replace("{{prompt}}",prompt);
        }

        sdUserTaskService.addComfyTask(taskId, userId, userName, flowStr, prompt, promptZh, imageUrls);
        // 生图任务存放到MQ队列
        DrawingTaskInfo taskInfo = new DrawingTaskInfo(taskId, flowStr, 10, userId, sdFlow.getDrawNum(),images);
        submitComfyTaskToQueue(taskInfo);
        return taskId;
    }

    /**
     * 查询指定任务的生图列表
     * @param taskId 任务id
     * @return 任务详情
     */
    @Override
    public List<ComfyUserModelFileVo> getComfyImageOutputByTaskId(String taskId) {
        return sdUserModelFileService.getComfyImageOutputByTaskId(taskId);
    }

    /**
     * 获取任务进度
     * @param taskId 任务id
     * @return 任务进度
     */
    @Override
    public Integer getComfyTaskProgress(String taskId) {
        // 检查任务状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]
        Long userId = LoginHelper.getUserId();
        Integer status = sdUserTaskService.selectStatusByTaskIdAndUserId(taskId, userId);
        if (status==null) {
            throw new TaskErrorException("未找到任务");
        }
        else if (status==0) {
            return 0;
        }
        else if (status==1) {
            Integer progress = RedisUtils.getCacheMapValue(DRAW_TASK_PROGRESS, taskId);
            if (progress!=null && progress == 100) {
                // 查询是否已生成图片
                boolean hasImg = sdUserModelFileService.checkHasImgByTaskId(taskId);
                if (hasImg) {
                    return 100;
                }
                else {
                    // 图片还没有生成，则返回99
                    return 99;
                }
            }
            return progress!=null?progress:0;
        }
        else {
            // 查询是否已生成图片
            boolean hasImg = sdUserModelFileService.checkHasImgByTaskId(taskId);
            if (hasImg) {
                return 100;
            }
            // 图片还没有生成，则返回99
            return 99;
        }
    }

    /**
     * 提交任务到队列
     * @param taskInfo 任务信息
     */
    private void submitComfyTaskToQueue(DrawingTaskInfo taskInfo) {
        try {
            rabbitTemplate.convertAndSend(SD_COMFY_DRAW_EXCHANGE,SD_COMFY_DRAW_ROUTING_KEY,taskInfo,new CorrelationData(taskInfo.getTaskId()));
        }
        catch (Exception e) {
            //重试次数
            int retryCount = 5;
            for (int i = 0; i < retryCount; i++) {
                try {
                    rabbitTemplate.convertAndSend(SD_COMFY_DRAW_EXCHANGE,SD_COMFY_DRAW_ROUTING_KEY,taskInfo,new CorrelationData(taskInfo.getTaskId()));
                }
                catch (Exception ignored) {}
            }
        }
    }

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
                log.error("[ComfyUI绘图MQ]>>>>>>>>>任务不存在,任务ID: {}", taskId);
                // 归还绘图次数
                userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                // 消费该消息
                channel.basicAck(deliveryTag, false);
                return;
            }
            else {
                // 获取可用节点 以及 锁定节点任务
                SdDrawNode node = sdDrawNodeService.selectDrawNodeAndLockNodeTask(LoadBalanceStrategy.WEIGHTED_LEAST_LOAD, taskId);
                if (node == null) {
                    // 没有可用节点,重新放回队列
                    log.info("[ComfyUI绘图MQ]>>>>>>>>>没有可用节点,任务ID: {}", taskId);
                    try {
                        channel.basicNack(deliveryTag, false, true);
                    }
                    catch (IOException ex) {
                        log.error("[ComfyUI绘图MQ]>>>>>>>>>MQ消息消费异常重新入队列异常,异常信息: ", ex);
                    }
                    return;
                }
                // 处理工作流字符串
                String flowStr;
                try{
                    flowStr = dealInputImage(taskInfo,node);
                    if (CollectionUtil.isNotEmpty(taskInfo.getImages())) {
                        sdUserTaskService.updateFlowOfComfyTask(taskId, flowStr);
                    }
                }
                catch (Exception e){
                    sdUserTaskService.failComfyTask(taskId,"初始图片上传到comfyui失败!",new Date());
                    // 归还绘图次数
                    userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                    // 归还节点
                    RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, node.getId().toString());
                    // 消费该消息
                    channel.basicAck(deliveryTag, false);
                    return;
                }
                // 提交任务，返回ComfyUI内部任务ID
                String promptId = submitDrawTask(taskId, JSONObject.parseObject(flowStr), node);
                if (StringUtils.isNotBlank(promptId)) {
                    RedisUtils.setCacheObject(COMFY_TASK+taskId,promptId, Duration.ofMinutes(1));
                    RedisUtils.setCacheObject(COMFY_TASK+promptId,taskId, Duration.ofMinutes(1));
                }
                // 检查任务是否有缓存
                checkCacheTask(promptId,taskId,node,taskInfo,task);
            }
            // 消费该消息
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            log.error("[ComfyUI绘图MQ]>>>>>>>>>MQ消息消费异常,异常信息: ", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            }
            catch (IOException ex) {
                log.error("[ComfyUI绘图MQ]>>>>>>>>>MQ消息消费异常重新入队列异常,异常信息: ", ex);
            }
        }
    }

    /**
     * 检查是否有任务缓存 以及 处理提交任务后的操作
     * @param promptId  comfyui内部任务ID
     * @param taskId    任务ID
     * @param node      执行任务节点
     * @param taskInfo  绘图任务提交信息
     * @param task      任务实体类
     */
    private void checkCacheTask(String promptId, String taskId, SdDrawNode node, DrawingTaskInfo taskInfo, SdUserTaskVo task) {
        if (StringUtils.isBlank(promptId)) {
            return;
        }
        // 修改存储节点任务ID以及开始时间
        sdUserTaskService.startComfyTask(taskId, new Date(), promptId, node.getId());

        // 获取历史任务信息
        ComfyTaskHistoryInfo historyInfo = getTaskInfoById(promptId, node);
        // 判断任务是否已完成或缓存，任务完成则直接生成图片数据
        if (historyInfo!=null && historyInfo.getCompleted()!=null && historyInfo.getCompleted()) {
            // 任务已完成：如果更新失败，则表示有其他地方以及完成且更新任务完成状态，此时则不用再处理新增生图了
            boolean isComplete = sdUserTaskService.completeComfyTask(taskId, new Date());
            if (!isComplete) {
                dealTaskAndNodeAndWebsocket(taskId, node.getId().toString());
                return;
            }
            if (CollectionUtil.isEmpty(historyInfo.getOutputs())) {
                // 归还绘图次数
                userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                return;
            }
            List<String> urlList = new ArrayList<>();
            for (ComfyTaskImage image : historyInfo.getOutputs()) {
                // 只保留任务输出图片
                if (image.getFileName().startsWith(taskId)) {
                    continue;
                }
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    UrlBuilder builder = UrlBuilder.of(node.getBaseUrl()).addPath("/view").addQuery("filename", image.getFileName()).addQuery("type", image.getFolder()).addQuery("subfolder", image.getSubFolder());
                    HttpUtil.download(builder.build(), out, false);
                    OssClient storage = OssFactory.instance();
                    UploadResult uploadResult = storage.uploadSuffix(out.toByteArray(),JPG,"image/jpeg");
                    sysOssService.insertOssData(SD + DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),task.getBelongUserName());
                    urlList.add(uploadResult.getUrl());
                } catch (Exception e) {
                    log.error("[任务输出图片][上传失败]>>>>>>>>>任务id: {},comfyui内部任务id: {},异常原因: ", taskId, promptId,e);
                    // 归还绘图次数
                    userService.returnedDrawNum(taskInfo.getUserId(), taskInfo.getDrawNum());
                }
            }
            if (CollectionUtil.isNotEmpty(urlList)) {
                sdUserModelFileService.asyncBatchInsert(task,urlList);
            }
            // 归还节点
            RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, node.getId().toString());
            return;
        }

        // 添加任务进度缓存
        RedisUtils.setCacheMapValue(DRAW_TASK_PROGRESS, taskId, 0);
        // 连接comfyui的websocket获取进度
        String wsUrl = node.getBaseUrl().replace("https", "wss").replace("http", "ws") + "/ws?clientId=" + taskId;
        comfyWebsocketClient.createComfyUiWebSocket(wsUrl, promptId, taskId);
    }

    /**
     * 处理图片上传到comfyui
     *
     * @param taskInfo    任务信息
     * @param node        生图节点
     * @return 处理后的工作流字符串
     */
    private String dealInputImage(DrawingTaskInfo taskInfo, SdDrawNode node) {
        String flowStr = taskInfo.getFlow();
        if (CollectionUtil.isEmpty(taskInfo.getImages())) {
            return flowStr;
        }
        for (int i = 0; i < taskInfo.getImages().size(); i++) {
            ImageInfoBo file = taskInfo.getImages().get(i);
            ComfyUploadImage image = uploadImage(file.getFileData(), node, file.getImageName(), ImageType.input);
            if (image!=null) {
                flowStr = flowStr.replace("{{inputImage"+(i+1)+"}}",image.getName());
            }
        }
        return flowStr;
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
    private String submitDrawTask(String taskId, JSONObject flow, SdDrawNode sdDrawNode) {
        try{
            JSONObject param =  new JSONObject();
            param.put("client_id", taskId);
            param.put("prompt", flow);
            HttpRequest request = HttpRequest.post(sdDrawNode.getBaseUrl() + "/prompt").body(param.toJSONString(), "application/json").timeout(2000);
            //提交任务
            String resp = execHttpRequest(request);
            log.warn("[提交任务]>>>>>>>>>提交[{}]任务返回结果: {}", taskId, resp);
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
     * api: /history/{promptId}<br>
     * 获得某一个任务信息
     *
     * @param promptId comfyUI内部任务id
     * @return 历史任务信息
     */
    private ComfyTaskHistoryInfo getTaskInfoById(String promptId) {
        SdUserTaskVo task = sdUserTaskService.getTaskInfoByPromptId(promptId);
        if (task == null) {
            throw new TaskErrorException("任务不存在!");
        }
        SdDrawNode node = sdDrawNodeService.findById(task.getNodeId());
        return getTaskInfoById(promptId, node);
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
        try{
            HttpRequest request = HttpRequest.get(node.getBaseUrl() + "/history/" + promptId).timeout(2000);
            String historyInfo = execHttpRequest(request);
            JsonNode taskNode = JsonUtils.toJsonNode(historyInfo).get(promptId);
            if (taskNode == null) {
                return null;
            }
            return JsonUtils.toObject(taskNode, ComfyTaskHistoryInfo.class);
        }
        catch (Exception e){
            log.error("[获取任务信息]>>>>>>>>>获取任务信息异常,异常信息: ", e);
            return null;
        }
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
     * @param type 图片存放位置(input | temp | output （默认 input ）)
     * @return 上传后的图片信息
     */
    @Override
    public ComfyUploadImage uploadImage(File file, SdDrawNode node, ImageType type) {
        // type 图片存放位置(input | temp | output （默认 input ）)
        HttpRequest request = HttpRequest.post(node.getBaseUrl() + "/upload/image").form("image", file).timeout(2000);
        if (type!=null) {
            request.form("type", type.name());
        }
        String resp = execHttpRequest(request);
        return JsonUtils.toObject(resp, ComfyUploadImage.class);
    }

    /**
     * api: /upload/image<br>
     * 上传图片到ComfyUI服务器
     *
     * @param node 节点信息
     * @param bytes 图片对象
     * @param type 图片存放位置(input | temp | output （默认 input ）)
     * @param fileName 文件名称携带后缀
     * @return 上传后的图片信息
     */
    @Override
    public ComfyUploadImage uploadImage(byte[] bytes, SdDrawNode node, String fileName, ImageType type) {
        if (bytes == null) {
            return null;
        }
        File file = FileUtils.bytesToTempFile(bytes, fileName, false);
        ComfyUploadImage taskImage = uploadImage(file, node, type);
        // 上传到comfyui后删除临时文件
        FileUtils.deleteFile(file);
        return taskImage;
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
        // 任务不存在或不处于进行中
        if (taskVo == null || (taskVo.getStatus()!=null && taskVo.getStatus()==2)) {
            dealTaskAndNodeAndWebsocket(taskId, nodeId);
            return;
        }
        SdDrawNode node = sdDrawNodeService.findById(Long.parseLong(nodeId));
        if (node == null) {
            dealTaskAndNodeAndWebsocket(taskId, nodeId);
            return;
        }
        ComfyTaskHistoryInfo taskInfo = getTaskInfoById(taskId, node);
        if (taskInfo == null) {
            // 任务还处于执行中
            if (taskVo.getStatus()==1) {
                return;
            }
            dealTaskAndNodeAndWebsocket(taskId, nodeId);
        }
        // 任务已完成
        else if (taskInfo.getCompleted() != null && taskInfo.getCompleted()) {
            boolean isComplete = sdUserTaskService.completeComfyTask(taskId, new Date());
            if (!isComplete) {
                dealTaskAndNodeAndWebsocket(taskId, nodeId);
                return;
            }
            if (CollectionUtil.isEmpty(taskInfo.getOutputs())) {
                return;
            }
            List<String> urlList = new ArrayList<>();
            for (ComfyTaskImage image : taskInfo.getOutputs()) {
                // 只保留任务输出图片
                if (image.getFileName().startsWith(taskId)) {
                    continue;
                }
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    UrlBuilder builder = UrlBuilder.of(node.getBaseUrl()).addPath("/view")
                        .addQuery("filename", image.getFileName())
                        .addQuery("type", image.getFolder())
                        .addQuery("subfolder", image.getSubFolder());
                    HttpUtil.download(builder.build(), out, false);
                    OssClient storage = OssFactory.instance();
                    UploadResult uploadResult = storage.uploadSuffix(out.toByteArray(),JPG,"image/jpeg");
                    urlList.add(uploadResult.getUrl());
                    sysOssService.insertOssData(SD + DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),taskVo.getBelongUserName());
                } catch (Exception e) {
                    log.error("[任务输出图片][上传失败]>>>>>>>>>任务id: {},comfyui内部任务id: {},异常原因: ", taskId, taskVo.getPromptId(), e);
                }
            }
            if (CollectionUtil.isNotEmpty(urlList)) {
                sdUserModelFileService.asyncBatchInsert(taskVo,urlList);
            }
            dealTaskAndNodeAndWebsocket(taskId, nodeId);
        }
        // 任务超时
        else if (taskVo.getStartTime()!=null && (new Date().after(DateUtil.offsetMinute(taskVo.getStartTime(), 2)))){
            sdUserTaskService.failComfyTask(taskId, "任务超时", new Date());
            dealTaskAndNodeAndWebsocket(taskId, nodeId);
        }
    }

    private void dealTaskAndNodeAndWebsocket(String taskId, String nodeId) {
        // 归还节点
        RedisUtils.delCacheMapValue(DRAW_NODE_TASK_MAP, nodeId);
        // 清除缓存中的任务进度
        RedisUtils.delCacheMapValue(DRAW_TASK_PROGRESS, taskId);
        // 关闭websocket
        comfyWebsocketClient.closeComfyUiWebSocket(taskId);
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
