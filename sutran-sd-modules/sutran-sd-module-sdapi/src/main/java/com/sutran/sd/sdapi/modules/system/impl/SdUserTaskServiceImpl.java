package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.modules.system.entity.SdUserTask;
import com.sutran.sd.sdapi.mapper.SdUserModelFileMapper;
import com.sutran.sd.sdapi.mapper.SdUserTaskMapper;
import com.sutran.sd.sdapi.modules.system.SdUserTaskService;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelFileVo;
import com.sutran.sd.sdapi.modules.system.vo.SdUserTaskVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;

/**
 * @author zj
 * @date 2024-03-03
 */
@Slf4j
@Service("SdUserTaskService")
public class SdUserTaskServiceImpl implements SdUserTaskService {

    @Resource
    private SdUserTaskMapper baseMapper;
    @Resource
    private SdUserModelFileMapper sdUserModelFileMapper;

    @Override
    public void insertTask(String taskId, Long userId, String userName, Integer category, int isRedraw) {
        Date now = new Date();
        SdUserTask task = new SdUserTask().setIsRedraw(isRedraw).setId(IdUtil.getSnowflakeNextId()).setTaskId(Long.parseLong(taskId)).setStatus(0).setCategory(category).setBelongUserId(userId).setBelongUserName(userName).setCrtTime(now).setUpdTime(now);
        baseMapper.insert(task);
    }

    @Override
    public void completeTask(String taskId, long consumeTime) {
        SdUserTask task = new SdUserTask().setTaskId(Long.parseLong(taskId)).setStatus(2).setConsumeTime(consumeTime).setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

    @Override
    public void doingTask(String taskId, long queueTime) {
        SdUserTask task = new SdUserTask().setTaskId(Long.parseLong(taskId)).setStatus(1).setQueueTime(queueTime).setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

    @Override
    public void failTask(String taskId, String reason, long queueTime) {
        SdUserTask task = new SdUserTask().setTaskId(Long.parseLong(taskId)).setStatus(3).setQueueTime(queueTime).setReason(reason).setUpdTime(new Date());
        baseMapper.update(task,new LambdaQueryWrapper<SdUserTask>().eq(SdUserTask::getTaskId,taskId));
    }

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

    @Override
    public String getDoingTask(Long userId, Integer category) {
        return baseMapper.getDoingTask(userId,category);
    }

    @Override
    public Integer selectStatusByTaskId(String taskId,Long userId) {
        return baseMapper.selectStatusByTaskId(taskId,userId);
    }

    @Override
    public void deleteTaskByTaskId(String taskId) {
        baseMapper.deleteTaskByTaskId(taskId);
    }

    @Override
    public String selectGridUrlByTaskId(String taskId) {
        return baseMapper.selectGridUrlByTaskId(taskId);
    }
}
