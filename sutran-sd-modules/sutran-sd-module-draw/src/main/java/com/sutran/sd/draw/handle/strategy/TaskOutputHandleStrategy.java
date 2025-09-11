package com.sutran.sd.draw.handle.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.common.core.service.OssService;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.draw.domain.pojo.ComfyTaskImage;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import com.sutran.sd.draw.service.SdUserModelFileService;
import com.sutran.sd.draw.service.SdUserTaskService;
import com.sutran.sd.draw.utils.JsonUtils;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final OssService ossService;

    /**
     * 处理消息
     *
     * @param msgType       消息类型
     * @param dataNode      消息内容
     * @param taskId        任务id
     * @param promptId      comfyui内部任务ID
     */
    @Override
    public void handleMessage(ComfyWebSocketMessageType msgType, JsonNode dataNode, String taskId, String promptId) {
        log.warn("[任务输出的图片]>>>>>>>>>任务id: {},comfyui内部任务id: {},节点id: {}", taskId, promptId, dataNode.get("node").asText());
        JsonNode imagesNode = dataNode.get("output").get("images");
        //获取上下文输出的图片信息
        List<ComfyTaskImage> currentOutputImages = new ArrayList<>();
        for (JsonNode imageNode : imagesNode) {
            ComfyTaskImage imageInfo = JsonUtils.toObject(imageNode, ComfyTaskImage.class);
            currentOutputImages.add(imageInfo);
        }
        if (CollectionUtil.isEmpty(currentOutputImages)) {
            return;
        }
        SdUserTaskVo sdUserTaskVo = sdUserTaskService.getDrawTaskInfoByTaskId(taskId);
        if (sdUserTaskVo == null || StringUtils.isBlank(sdUserTaskVo.getNodeUrl())) {
            return;
        }
        List<String> urlList = new ArrayList<>();
        for (ComfyTaskImage image : currentOutputImages) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                UrlBuilder builder = UrlBuilder.of(sdUserTaskVo.getNodeUrl()).addPath("/view")
                    .addQuery("filename", image.getFileName())
                    .addQuery("type", image.getFolder())
                    .addQuery("subfolder", image.getSubFolder());
                HttpUtil.download(builder.build(), out, false);
                OssClient storage = OssFactory.instance();
                UploadResult uploadResult = storage.uploadSuffix(out.toByteArray(),JPG,"image/jpeg");
                urlList.add(uploadResult.getUrl());
                ossService.insertOssData(SD+ DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),userName);
            } catch (Exception e) {
                log.error("任务输出的图片上传失败,任务id: {},comfyui内部任务id: {},节点id: {}", taskId, promptId, dataNode.get("node").asText(),e);
            }
        }
        if (CollectionUtil.isNotEmpty(urlList)) {
            sdUserModelFileService.asyncBatchInsert(sdUserTaskVo,urlList,null);
        }
    }
}
