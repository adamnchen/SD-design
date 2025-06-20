package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.mapper.SdTrainPreTaskMapper;
import com.sutran.sd.sdapi.modules.system.SdTrainPreTaskService;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import com.sutran.sd.sdapi.modules.system.entity.SdTrainTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-24
 */
@Slf4j
@Service("SdTrainPreTaskService")
public class SdTrainPreTaskServiceImpl implements SdTrainPreTaskService {

    @Resource
    private SdTrainPreTaskMapper baseMapper;

    @Override
    public void insert(Long userId, String username, Map<String, Object> params, int imgNum, String taskId) {
        SdTrainTask task = new SdTrainTask().setId(StrUtil.isEmptyIfStr(taskId)?IdUtil.getSnowflakeNextId():Long.parseLong(taskId))
            // 处于预处理队列中
            .setStatus(0).setNewStatus(0)
            .setPreParams(JSONObject.toJSONString(params)).setImgNum(imgNum)
            .setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
        baseMapper.insert(task);
    }

    @Override
    public SdTrainTask selectDetailById(String preTaskId) {
        return baseMapper.selectById(preTaskId);
    }

    @Override
    public SdTrainTask selectDetailByUserId(Long userId) {
        return baseMapper.selectDetailByUserId(userId);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void completePreTask(String preTaskId) {
        baseMapper.completePreTask(preTaskId);
    }

    @Override
    public void updateTaskIdAndNewStatus(String preTaskId, String taskId, Map<String, Object> trainParams, String modelName, Date startTime, int newStatus, SdGpuPool sdGpuPool) {
        baseMapper.updateTaskIdAndNewStatus(preTaskId,taskId,JSON.toJSONString(trainParams),modelName,startTime,newStatus,JSON.toJSONString(sdGpuPool));
    }

    @Override
    public void completeTrainTask(String taskId, Date endTime) {
        baseMapper.completeTrainTask(taskId, endTime);
    }

    @Override
    public SdTrainTask selectDetailByTaskId(String taskId) {
        return baseMapper.selectDetailByTaskId(taskId);
    }

    @Override
    public void modifyNewStatusById(String preTaskId, int newStatus, String message, SdGpuPool sdGpuPool) {
        baseMapper.modifyNewStatusById(preTaskId,newStatus,message,sdGpuPool==null?null:JSON.toJSONString(sdGpuPool));
    }

    @Override
    public void reduceImgNum(String preTaskId) {
        baseMapper.reduceImgNum(preTaskId);
    }

    @Override
    public void deleteById(String preTaskId) {
        baseMapper.deleteById(preTaskId);
    }

    @Override
    public JSONObject selectNewStatusAndGpuPoolById(String preTaskId) {
        return baseMapper.selectNewStatusAndGpuPoolById(preTaskId);
    }

    /**
     * 获取任务状态信息
     * @param preTaskId 预处理任务ID
     * @return  任务状态信息
     */
    @Override
    public TrainTaskStatusVo selectTaskStatusByPreTaskId(String preTaskId) {
        return baseMapper.selectTaskStatusByPreTaskId(preTaskId);
    }

    @Override
    public void updateAdditionTag(String preTaskId, String additionTagStr) {
        baseMapper.updateAdditionTag(preTaskId,additionTagStr);
    }

    @Override
    public SdGpuPool selectGpuPoolByTaskId(String taskId) {
        String gpuPool = baseMapper.selectGpuPoolByTaskId(taskId);
        return StrUtil.isEmptyIfStr(gpuPool)?null:JSON.parseObject(gpuPool,SdGpuPool.class);
    }

    @Override
    public boolean deleteByIdAndPicNumIsZero(String preTaskId) {
        Integer i = baseMapper.deleteByIdAndPicNumIsZero(preTaskId);
        return i!=null && i>0;
    }

    @Override
    public List<SdTrainTask> selectByUserId(Long userId, Integer newStatus) {
        LambdaQueryWrapper<SdTrainTask> lqw = new LambdaQueryWrapper<>();
        return baseMapper.selectList(lqw.eq(SdTrainTask::getCrtUserId,userId).eq(SdTrainTask::getNewStatus,newStatus));
    }
}
