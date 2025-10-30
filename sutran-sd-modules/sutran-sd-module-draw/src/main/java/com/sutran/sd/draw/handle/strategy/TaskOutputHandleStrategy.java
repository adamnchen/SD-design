package com.sutran.sd.draw.handle.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.file.FileUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.pojo.ComfyTaskImage;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.service.SdUserModelFileService;
import com.sutran.sd.draw.service.SdUserTaskService;
import com.sutran.sd.draw.utils.JsonUtils;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import com.sutran.sd.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_TASK_PROGRESS;
import static com.sutran.sd.draw.constants.CommonKey.JPG;
import static com.sutran.sd.draw.constants.CommonKey.SD;

/**
 * 任务输出的图片
 * @author zj
 */
@Slf4j
@Service("EXECUTED")
@RequiredArgsConstructor
public class TaskOutputHandleStrategy implements IComfyWebSocketTextHandleStrategy {

    private final SdUserTaskService sdUserTaskService;
    private final SdUserModelFileService sdUserModelFileService;
    private final ISysOssService sysOssService;

    /**
     * 处理消息
     *
     * @param dataNode      消息内容
     */
    @Override
    public void handleMessage(JsonNode dataNode) {
        String promptId = dataNode.get("prompt_id").asText();
        String taskId = sdUserTaskService.getTaskIdByPromptId(promptId);
        try{
            JsonNode imagesNode = dataNode.get("output").get("images");
            //获取上下文输出的图片信息
            List<ComfyTaskImage> currentOutputImages = new ArrayList<>();
            if (imagesNode!=null){
                for (JsonNode imageNode : imagesNode) {
                    ComfyTaskImage imageInfo = JsonUtils.toObject(imageNode, ComfyTaskImage.class);
                    // 只保留任务输出图片
                    if (imageInfo.getFileName().startsWith(taskId)) {
                        currentOutputImages.add(imageInfo);
                    }
                }
            }
            if (CollectionUtil.isEmpty(currentOutputImages)) {
                return;
            }
            log.warn("[任务输出的图片]>>>>>>>>>节点: {}", dataNode);
            SdUserTaskVo task = sdUserTaskService.getDrawTaskInfoByTaskId(taskId);
            if (task == null || StringUtils.isBlank(task.getNodeUrl())) {
                return;
            }
            List<String> urlList = new ArrayList<>();
            OssClient storage = OssFactory.instance();
            for (ComfyTaskImage image : currentOutputImages) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    UrlBuilder builder = UrlBuilder.of(task.getNodeUrl()).addPath("/view")
                        .addQuery("filename", image.getFileName())
                        .addQuery("type", image.getFolder())
                        .addQuery("subfolder", image.getSubFolder());
                    HttpUtil.download(builder.build(), out, false);
                    // 压缩图片大小
                    byte[] compressPic = FileUtils.compressPic(out.toByteArray(), 0.8);
                    UploadResult uploadResult = storage.uploadSuffix(compressPic,JPG,"image/jpeg");
                    urlList.add(uploadResult.getUrl());
                    sysOssService.insertOssData(SD + DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),task.getBelongUserName());
                } catch (Exception e) {
                    log.error("[任务输出图片][上传失败]>>>>>>>>>任务id: {},comfyui内部任务id: {},异常原因: ", taskId, promptId,e);
                }
            }
            if (CollectionUtil.isNotEmpty(urlList)) {
                sdUserModelFileService.asyncBatchInsert(task,urlList);
            }
        }
        finally {
            RedisUtils.setCacheMapValue(DRAW_TASK_PROGRESS, taskId, 100);
        }
    }
}
