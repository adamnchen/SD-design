package com.sutran.sd.draw.domain.bo;

import lombok.Getter;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.List;

/**
 * 绘图任务信息
 *
 * @author zj
 */
@Getter
public class TrainTaskInfo implements Serializable {

    /**
     * 自定义的任务id
     */
    private final String taskId;
    /**
     * 参考图片集合
     */
    private final List<ImageInfoBo> images;
    private final Long userId;
    private final String userName;
    private final String loraName;
    private final List<String> captions;

    /**
     * @param taskId       自定义的任务id
     * @param images       参考图片集合
     * @param userId       用户ID
     * @param userName     用户名
     * @param loraName     Lora模型名称
     * @param captions     图片描述集合
     */
    @ConstructorProperties({"taskId", "images", "userId", "userName", "loraName", "captions"})
    public TrainTaskInfo(String taskId, List<ImageInfoBo> images, Long userId, String userName, String loraName, List<String> captions) {
        this.taskId = taskId;
        this.images = images;
        this.userId = userId;
        this.userName = userName;
        this.loraName = loraName;
        this.captions = captions;
    }
}
