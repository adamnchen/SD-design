package com.sutran.sd.comfyapi.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

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
    private final JSONObject flow;

    /**
     * 任务超时时间(分钟)
     */
    private final long timeout;

    /**
     * @param taskId  自定义的任务id
     * @param flow    绘图任务工作流
     * @param timeout 任务超时时间
     */
    @ConstructorProperties({"taskId", "flow", "timeout"})
    public DrawingTaskInfo(String taskId, JSONObject flow, long timeout) {
        this.taskId = taskId;
        this.flow = flow;
        this.timeout = timeout;
    }
}
