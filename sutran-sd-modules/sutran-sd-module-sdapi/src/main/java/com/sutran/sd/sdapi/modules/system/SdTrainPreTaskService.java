package com.sutran.sd.sdapi.modules.system;

import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import com.sutran.sd.sdapi.modules.system.entity.SdTrainTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-24
 */
public interface SdTrainPreTaskService {
    void insert(Long userId, String username, Map<String, Object> params, int imgNum, String taskId);

    SdTrainTask selectDetailById(String preTaskId);

    SdTrainTask selectDetailByUserId(Long preTaskId);

    void completePreTask(String preTaskId);

    void updateTaskIdAndNewStatus(String preTaskId, String taskId, Map<String, Object> trainParams, String modelName, Date date, int newStatus, SdGpuPool sdGpuPool);

    void completeTrainTask(String taskId, Date date);

    SdTrainTask selectDetailByTaskId(String taskId);

    void modifyNewStatusById(String preTaskId, int newStatus, String reason, SdGpuPool sdGpuPool);

    void reduceImgNum(String preTaskId);

    void deleteById(String preTaskId);

    JSONObject selectNewStatusAndGpuPoolById(String preTaskId);

    /**
     * 获取任务状态信息
     * @param preTaskId 预处理任务ID
     * @return  任务状态信息
     */
    TrainTaskStatusVo selectTaskStatusByPreTaskId(String preTaskId);

    void updateAdditionTag(String preTaskId, String additionTagStr);

    SdGpuPool selectGpuPoolByTaskId(String taskId);

    boolean deleteByIdAndPicNumIsZero(String preTaskId);

    List<SdTrainTask> selectByUserId(Long userId, Integer newStatus);
}
