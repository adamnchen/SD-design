package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.SdUserTask;
import com.sutran.sd.draw.domain.vo.SdUserModelFileVo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import com.sutran.sd.draw.enums.TaskType;
import com.sutran.sd.draw.mapper.SdUserModelFileMapper;
import com.sutran.sd.draw.mapper.SdUserTaskMapper;
import com.sutran.sd.draw.service.SdUserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

/**
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdUserTaskServiceImpl implements SdUserTaskService {

    private final SdUserTaskMapper baseMapper;
    private final SdUserModelFileMapper sdUserModelFileMapper;

    /**
     * 新增ComfyUI任务
     *
     * @param taskId   任务ID
     * @param userId   用户ID
     * @param userName 用户名
     * @param flow     工作流
     * @param prompt     英文提示词
     * @param promptZh   中文提示词
     */
    @Override
    public void addComfyTask(String taskId, Long userId, String userName, String flow, String prompt, String promptZh) {
        Date now = new Date();
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setTaskType(TaskType.COMFYUI.name())
            .setIsRedraw(0)
            .setStatus(0)
            .setCategory(3)
            .setBelongUserId(userId)
            .setBelongUserName(userName)
            .setPrompt(prompt)
            .setPromptZh(promptZh)
            .setCrtTime(now)
            .setUpdTime(now)
            .setFlow(flow);
        baseMapper.insert(task);
    }

    /**
     * 插入用户任务
     * @param taskId    任务ID
     * @param userId    用户ID
     * @param userName  用户名
     * @param category  任务类型
     * @param isRedraw  是否重绘
     */
    @Override
    public void addWebuiTask(String taskId, Long userId, String userName, Integer category, int isRedraw) {
        Date now = new Date();
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setTaskType(TaskType.WEBUI.name())
            .setIsRedraw(isRedraw)
            .setStatus(0)
            .setCategory(category)
            .setBelongUserId(userId)
            .setBelongUserName(userName)
            .setCrtTime(now)
            .setUpdTime(now);
        baseMapper.insert(task);
    }

    /**
     * 完成用户任务
     * @param taskId        任务ID
     * @param consumeTime   消耗时间
     */
    @Override
    public void completeWebuiTask(String taskId, long consumeTime) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(2)
            .setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

    /**
     * 完成ComfyUI任务
     * @param taskId        任务ID
     * @param endTime       完成时间
     */
    @Override
    public void completeComfyTask(String taskId, Date endTime) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(2)
            .setEndTime(endTime)
            .setUpdTime(new Date());
        baseMapper.updateById(task);
    }

    /**
     * 执行用户任务
     * @param taskId    任务ID
     * @param queueTime 队列时间
     */
    @Override
    public void startWebuiTask(String taskId, long queueTime) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(1)
            .setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

    /**
     * 执行用户Comfy任务
     * @param taskId    任务ID
     * @param startTime 队列时间
     * @param promptId  Comfy内部任务ID
     * @param nodeId    节点ID
     */
    @Override
    public void startComfyTask(String taskId, Date startTime, String promptId, Long nodeId) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(1)
            .setStartTime(startTime)
            .setPromptId(promptId)
            .setNodeId(nodeId)
            .setUpdTime(new Date());
        baseMapper.updateById(task);
    }

    /**
     * 失败用户任务
     * @param taskId    任务ID
     * @param reason    失败原因
     * @param queueTime 队列时间
     */
    @Override
    public void failWebuiTask(String taskId, String reason, long queueTime) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(3)
            .setReason(reason)
            .setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

    /**
     * 失败用户Comfy任务
     * @param taskId    任务ID
     * @param reason    失败原因
     * @param endTime   失败时间
     */
    @Override
    public void failComfyTask(String taskId, String reason, Date endTime) {
        SdUserTask task = new SdUserTask()
            .setTaskId(Long.parseLong(taskId))
            .setStatus(3)
            .setReason(reason)
            .setEndTime(endTime)
            .setUpdTime(new Date());
        baseMapper.updateById(task);
    }

    /**
     * 查询用户任务列表
     * @param pageQuery 分页查询
     * @param userId    用户ID
     * @param category  任务类型
     * @param status    任务状态
     * @return          用户任务列表
     */
    @Override
    public TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Long userId, Integer category, Integer status) {
        Page<SdUserTaskVo> page = baseMapper.selectAllList(pageQuery.build(),userId,category,status);
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return TableDataInfo.build(page);
        }
        for (SdUserTaskVo e : page.getRecords()) {
            SdUserModelFileVo firstVo = sdUserModelFileMapper.selectFirstUrlByTaskIdAndUserId(e.getTaskId(),userId);
            if (firstVo == null) {
                continue;
            }
            e.setFirstImgUrl(firstVo.getFileUrl());
            Object loraInfoList = firstVo.getLoraInfo();
            if (loraInfoList!=null) {
                e.setLoraInfo(JSONArray.parseArray(String.valueOf(loraInfoList)));
            }
            else {
                JSONObject loraInfo = new JSONObject();
                loraInfo.put("loraTitle",firstVo.getLoraTitle());
                loraInfo.put("loraModelId",firstVo.getLoraModelId());
                loraInfo.put("loraModelUrl",firstVo.getLoraModelUrl());
                loraInfo.put("modelStrength",firstVo.getLoraTitleZh());
                e.setLoraInfo(Collections.singletonList(loraInfo));
            }
        }
        return TableDataInfo.build(page);
    }

    /**
     * 查询用户正在执行的任务
     * @param userId    用户ID
     * @param category  任务类型
     * @return          任务ID
     */
    @Override
    public String getDoingTask(Long userId, Integer category) {
        return baseMapper.getDoingTask(userId,category);
    }

    /**
     * 查询用户任务状态
     * @param taskId    任务ID
     * @param userId    用户ID
     * @return          任务状态
     */
    @Override
    public Integer selectStatusByTaskId(String taskId,Long userId) {
        return baseMapper.selectStatusByTaskId(taskId,userId);
    }

    /**
     * 删除用户任务
     * @param taskId    任务ID
     */
    @Override
    public void deleteTaskByTaskId(String taskId) {
        baseMapper.deleteTaskByTaskId(taskId);
    }

    /**
     * 查询用户任务网格URL
     * @param taskId    任务ID
     * @return          网格URL
     */
    @Override
    public String selectGridUrlByTaskId(String taskId) {
        return baseMapper.selectGridUrlByTaskId(taskId);
    }

    /**
     * 获取任务关联的节点ID
     * @param taskId    任务ID
     * @return          节点ID
     */
    @Override
    public String getNodeIdByTaskId(String taskId) {
        return baseMapper.getNodeIdByTaskId(taskId);
    }

    /**
     * 获取任务关联的promptID
     * @param taskId    任务ID
     * @return          promptID
     */
    @Override
    public String getPromptIdByTaskId(String taskId) {
        return baseMapper.getPromptIdByTaskId(taskId);
    }

    /**
     * 获取任务关联的节点URL
     * @param taskId    任务ID
     * @return          节点URL
     */
    @Override
    public SdUserTaskVo getDrawTaskInfoByTaskId(String taskId) {
        return baseMapper.getDrawTaskInfoByTaskId(taskId);
    }

    /**
     * 获取任务关联的promptID
     * @param promptId  promptID
     * @return          任务ID
     */
    @Override
    public String getTaskIdByPromptId(String promptId) {
        return baseMapper.getTaskIdByPromptId(promptId);
    }

    /**
     * 根据promptID查询任务信息
     * @param promptId  promptID
     * @return          任务信息
     */
    @Override
    public SdUserTask getTaskInfoByPromptId(String promptId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getPromptId,promptId));
    }
}
