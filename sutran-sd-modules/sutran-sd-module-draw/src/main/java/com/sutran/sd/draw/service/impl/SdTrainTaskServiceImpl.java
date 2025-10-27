package com.sutran.sd.draw.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.SdTrainTask;
import com.sutran.sd.draw.domain.vo.FluxgymTaskStatusVo;
import com.sutran.sd.draw.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.draw.mapper.SdTrainTaskMapper;
import com.sutran.sd.draw.service.SdTrainTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdTrainTaskServiceImpl implements SdTrainTaskService {

    private final SdTrainTaskMapper baseMapper;

    /**
     * 按用户ID和任务状态查询列表
     *
     * @param userId    用户ID
     * @param newStatus 任务状态
     * @param pageQuery 分页参数
     * @return 任务列表
     */
    @Override
    public Page<SdTrainTask> selectListByUserIdAndNewStatus(Long userId, Integer newStatus, PageQuery pageQuery) {
        LambdaQueryWrapper<SdTrainTask> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SdTrainTask::getCrtUserId, userId).eq(newStatus!=null, SdTrainTask::getNewStatus, newStatus).orderByDesc(SdTrainTask::getCrtTime);
        return baseMapper.selectPage(pageQuery.build(),lqw);
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


    /**
     * 提交预处理任务
     * @param userId    用户ID
     * @param username  用户名
     * @param params    训练参数
     * @param imgNum    图片数量
     * @param preTaskId 预处理任务ID
     * @param preSubmitTime 预处理任务提交时间
     */
    @Override
    public void insert(Long userId, String username, Map<String, Object> params, int imgNum, String preTaskId, Date preSubmitTime) {
        SdTrainTask task = new SdTrainTask().setId(Long.parseLong(preTaskId))
            // 处于预处理队列中
            .setStatus(0).setNewStatus(0)
            .setPreParams(JSONObject.toJSONString(params)).setImgNum(imgNum).setPreSubmitTime(preSubmitTime)
            .setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
        baseMapper.insert(task);
    }
    /**
     * 开始执行预处理任务
     * @param preTaskId 预处理任务ID
     * @param startTime 开始时间
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void startPreTask(String preTaskId, Date startTime) {
        baseMapper.startPreTask(preTaskId,startTime);
    }
    /**
     * 完成预处理任务
     * @param preTaskId 预处理任务ID
     * @param reason 原因
     * @param endTime 结束时间
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void completePreTask(String preTaskId, String reason, Date endTime) {
        baseMapper.completePreTask(preTaskId,reason,endTime);
    }

    /**
     * 添加共性词
     * @param preTaskId 预处理任务ID
     * @param additionTagStr 共性词
     */
    @Override
    public void updateAdditionTag(String preTaskId, String additionTagStr) {
        baseMapper.updateAdditionTag(preTaskId,additionTagStr);
    }

    /**
     * 提交训练任务
     *
     * @param preTaskId  预处理任务ID
     * @param modelName  模型名称
     * @param trainParams 训练参数
     * @param submitTime 提交时间
     */
    @Override
    public void submitTrainTask(String preTaskId, String modelName, String trainParams, Date submitTime) {
        baseMapper.submitTrainTaskById(preTaskId,modelName,trainParams,submitTime);
    }

    /**
     * 开始执行训练任务
     * @param preTaskId 预处理任务ID
     * @param taskId 训练任务ID
     * @param startTime 开始时间
     * @param sdGpuPool GPU 池
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void startTrainTask(String preTaskId, String taskId, Date startTime, SdGpuPool sdGpuPool) {
        baseMapper.startTrainTask(preTaskId,taskId,startTime,JSONObject.toJSONString(sdGpuPool));
    }

    /**
     * 完成训练任务
     * @param taskId 训练任务ID
     * @param endTime 结束时间
     */
    @Async("threadPoolTaskExecutor")
    @Override
    public void completeTrainTask(String taskId, Date endTime) {
        baseMapper.completeTrainTask(taskId, endTime);
    }

    /**
     * 训练任务失败
     *
     * @param preTaskId 预处理任务ID
     * @param reason    原因
     * @param sdGpuPool GPU 池
     * @param endTime   结束时间
     */
    @Override
    public void failTrainTask(String preTaskId, String reason, SdGpuPool sdGpuPool, Date endTime) {
        baseMapper.failTrainTask(preTaskId,reason,sdGpuPool==null?null:JSONObject.toJSONString(sdGpuPool),endTime);
    }

    @Override
    public SdTrainTask selectDetailById(String preTaskId) {
        return baseMapper.selectById(preTaskId);
    }

    @Override
    public SdTrainTask selectDetailByUserId(Long userId) {
        return baseMapper.selectDetailByUserId(userId);
    }


    @Override
    public SdTrainTask selectDetailByTaskId(String taskId) {
        return baseMapper.selectDetailByTaskId(taskId);
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
    public Integer selectNewStatusById(String preTaskId) {
        return baseMapper.selectNewStatusById(preTaskId);
    }

    @Override
    public JSONObject selectNewStatusByTaskId(String taskId) {
        return baseMapper.selectNewStatusByTaskId(taskId);
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
    public Long selectCrtUserIdById(String preTaskId) {
        return baseMapper.selectCrtUserIdById(preTaskId);
    }

    @Override
    public Integer countRunningTaskByUserId(Long userId) {
        return baseMapper.countRunningTaskByUserId(userId);
    }

    @Override
    public void insert(SdTrainTask sdTrainTask) {
        baseMapper.insert(sdTrainTask);
    }

    @Override
    public void startFluxgymTrainTask(String taskId, Long nodeId, Date startTime, Map<String, Object> trainParams) {
        baseMapper.startFluxgymTrainTask(taskId,nodeId,startTime,JSONObject.toJSONString(trainParams));
    }

    @Override
    public JSONObject selectNodeBaseUrlAndStatusByTaskId(String taskId) {
        return baseMapper.selectNodeBaseUrlAndStatusByTaskId(taskId);
    }

    @Override
    public void completeFluxgymTrainTask(String taskId, Date endTime) {
        baseMapper.completeFluxgymTrainTask(taskId,endTime);
    }

    @Override
    public void failFluxgymTrainTask(String taskId, String message, Date endTime) {
        baseMapper.failFluxgymTrainTask(taskId,message,endTime);
    }

    @Override
    public int selectTrainImageNumById(String taskId) {
        return baseMapper.selectTrainImageNumById(taskId);
    }

    @Override
    public Page<SdTrainTask> listTrainTaskOfFluxgym(Integer newStatus, PageQuery pageQuery) {
        LambdaQueryWrapper<SdTrainTask> lqw = new LambdaQueryWrapper<>();
        lqw.eq(newStatus!=null, SdTrainTask::getNewStatus, newStatus).orderByDesc(SdTrainTask::getCrtTime);
        return baseMapper.selectPage(pageQuery.build(),lqw);
    }

    /**
     * 获取训练任务的预处理参数
     * @param id 任务ID
     * @return 预处理参数
     */
    @Override
    public String selectPreParamsById(String id) {
        return baseMapper.selectPreParamsById(id);
    }

    /**
     * [FluxGym]SD训练-查询训练任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    @Override
    public FluxgymTaskStatusVo getFluxgymTaskStatus(String taskId) {
        return baseMapper.getFluxgymTaskStatus(taskId);
    }

    /**
     * FluxGym]SD训练-当前用户正在训练的任务ID
     * @param userId 用户ID
     * @return 进行中的任务ID
     */
    @Override
    public String getDoingFluxgymTask(Long userId) {
        return baseMapper.getDoingFluxgymTask(userId);
    }

}
