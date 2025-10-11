package com.sutran.sd.draw.domain.bo;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.beans.ConstructorProperties;
import java.io.Serializable;

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

    private final MultipartFile image1;
    private final MultipartFile image2;

    /**
     * 任务超时时间(分钟)
     */
    private final long timeout;

    /**
     * 用户ID
     */
    private long userId;

    /**
     * 绘图次数
     */
    private int drawNum;

    /**
     * @param taskId  自定义的任务id
     * @param flow    绘图任务工作流
     * @param timeout 任务超时时间
     * @param userId    用户ID
     * @param drawNum   绘图次数
     * @param image1 图片1
     * @param image2 图片2
     */
    @ConstructorProperties({"taskId", "flow", "timeout"})
    public DrawingTaskInfo(String taskId, String flow, long timeout, Long userId, Integer drawNum, MultipartFile image1, MultipartFile image2) {
        this.taskId = taskId;
        this.flow = flow;
        this.timeout = timeout;
        this.userId = userId;
        this.drawNum = drawNum;
        this.image1 = image1;
        this.image2 = image2;
    }
}
