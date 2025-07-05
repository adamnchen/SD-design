package com.sutran.sd.sdapi.modules.system;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import com.sutran.sd.sdapi.modules.system.entity.SdTrainTask;

import java.util.Date;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-24
 */
public interface SdTrainPreTaskService {

    /**
     * 按用户ID和任务状态查询列表
     *
     * @param userId    用户ID
     * @param newStatus 任务状态
     * @param pageQuery 分页参数
     * @return 任务列表
     */
    Page<SdTrainTask> selectListByUserIdAndNewStatus(Long userId, Integer newStatus, PageQuery pageQuery);

    /**
     * 获取任务状态信息
     * @param preTaskId 预处理任务ID
     * @return  任务状态信息
     */
    TrainTaskStatusVo selectTaskStatusByPreTaskId(String preTaskId);

    void insert(Long userId, String username, Map<String, Object> params, int imgNum, String preTaskId);

    SdTrainTask selectDetailById(String preTaskId);

    SdTrainTask selectDetailByUserId(Long preTaskId);

    void completePreTask(String preTaskId);

    void updateTaskIdAndNewStatus(String preTaskId, String taskId, Map<String, Object> trainParams, String modelName, Date date, int newStatus, SdGpuPool sdGpuPool);

    void completeTrainTask(String taskId, Date date);

    SdTrainTask selectDetailByTaskId(String taskId);

    void modifyNewStatusById(String preTaskId, int newStatus, String reason, SdGpuPool sdGpuPool);

    void reduceImgNum(String preTaskId);

    void deleteById(String preTaskId);

    Integer selectNewStatusById(String preTaskId);

    JSONObject selectNewStatusByTaskId(String taskId);

    void updateAdditionTag(String preTaskId, String additionTagStr);

    SdGpuPool selectGpuPoolByTaskId(String taskId);

    boolean deleteByIdAndPicNumIsZero(String preTaskId);

    Long selectCrtUserIdById(String preTaskId);
}
