package com.sutran.sd.draw.service;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;

import java.util.Date;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserTaskService {

    /**
     * 新增ComfyUI任务
     * @param taskId    任务ID
     * @param userId    用户ID
     * @param userName  用户名
     * @param flow      工作流
     */
    void addComfyTask(String taskId, Long userId, String userName, String flow);
    /**
     * 新增用户任务
     * @param taskId    任务ID
     * @param userId    用户ID
     * @param userName  用户名
     * @param category  任务类型
     * @param isRedraw  是否重绘
     */
    void addWebuiTask(String taskId, Long userId, String userName, Integer category, int isRedraw);


    /**
     * 完成用户任务
     * @param taskId        任务ID
     * @param consumeTime   消耗时间
     */
    void completeWebuiTask(String taskId, long consumeTime);
    /**
     * 完成ComfyUI任务
     * @param taskId        任务ID
     * @param endTime       完成时间
     */
    void completeComfyTask(String taskId, Date endTime);


    /**
     * 执行用户任务
     * @param taskId    任务ID
     * @param queueTime 队列时间
     */
    void startWebuiTask(String taskId, long queueTime);
    /**
     * 执行用户Comfy任务
     * @param taskId    任务ID
     * @param startTime 队列时间
     * @param promptId  Comfy内部任务ID
     * @param nodeId    节点ID
     */
    void startComfyTask(String taskId, Date startTime, String promptId, Long nodeId);


    /**
     * 失败用户任务
     * @param taskId    任务ID
     * @param reason    失败原因
     * @param queueTime 队列时间
     */
    void failWebuiTask(String taskId, String reason, long queueTime);
    /**
     * 失败用户Comfy任务
     * @param taskId    任务ID
     * @param reason    失败原因
     * @param endTime   失败时间
     */
    void failComfyTask(String taskId, String reason, Date endTime);

    /**
     * 查询用户任务列表
     * @param pageQuery 分页查询
     * @param userId    用户ID
     * @param category  任务类型
     * @param status    任务状态
     * @return          用户任务列表
     */
    TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Long userId, Integer category, Integer status);

    /**
     * 获取用户执行中的任务
     * @param userId    用户ID
     * @param category  任务类型
     * @return          任务ID
     */
    String getDoingTask(Long userId, Integer category);

    /**
     * 查询用户任务状态
     * @param taskId    任务ID
     * @param userId    用户ID
     * @return          任务状态
     */
    Integer selectStatusByTaskId(String taskId, Long userId);

    /**
     * 删除用户任务
     * @param taskId    任务ID
     */
    void deleteTaskByTaskId(String taskId);

    /**
     * 查询用户xyz测试图片URL
     * @param taskId    任务ID
     * @return          网格URL
     */
    String selectGridUrlByTaskId(String taskId);

    /**
     * 获取任务关联的节点ID
     * @param taskId    任务ID
     * @return          节点ID
     */
    String getNodeIdByTaskId(String taskId);

    /**
     * 获取任务关联的promptID
     * @param taskId    任务ID
     * @return          promptID
     */
    String getPromptIdByTaskId(String taskId);

    /**
     * 获取任务关联的节点URL
     * @param taskId    任务ID
     * @return          节点URL
     */
    SdUserTaskVo getDrawTaskInfoByTaskId(String taskId);
}
