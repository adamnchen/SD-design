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
public class DrawingTaskInfo implements Serializable {

    /**
     * 自定义的任务id
     */
    private final String taskId;

    /**
     * 绘图任务工作流
     */
    private final String flow;

    /**
     * 参考图片集合
     */
    private final List<DrawingImageInfoBo> images;

    /**
     * 任务超时时间(分钟)
     */
    private final long timeout;

    /**
     * 用户ID
     */
    private final long userId;

    /**
     * 绘图次数
     */
    private final int drawNum;

    /**
     * @param taskId  自定义的任务id
     * @param flow    绘图任务工作流
     * @param timeout 任务超时时间
     * @param userId    用户ID
     * @param drawNum   绘图次数
     * @param images 参考图片集合
     */
    @ConstructorProperties({"taskId", "flow", "timeout"})
    public DrawingTaskInfo(String taskId, String flow, long timeout, Long userId, Integer drawNum, List<DrawingImageInfoBo> images) {
        this.taskId = taskId;
        this.flow = flow;
        this.timeout = timeout;
        this.userId = userId;
        this.drawNum = drawNum;
        this.images = images;
    }
}
