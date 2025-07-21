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

    /**
     * 提交预处理任务
     * @param userId    用户ID
     * @param username  用户名
     * @param params    训练参数
     * @param imgNum    图片数量
     * @param preTaskId 预处理任务ID
     * @param submitTime 预处理任务提交时间
     */
    void insert(Long userId, String username, Map<String, Object> params, int imgNum, String preTaskId, Date submitTime);

    /**
     * 开始执行预处理任务
     * @param preTaskId 预处理任务ID
     * @param startTime 开始时间
     */
    void startPreTask(String preTaskId, Date startTime);

    /**
     * 完成预处理任务
     * @param preTaskId 预处理任务ID
     * @param reason 原因
     * @param endTime 结束时间
     */
    void completePreTask(String preTaskId, String reason, Date endTime);

    /**
     * 添加共性词
     * @param preTaskId 预处理任务ID
     * @param additionTagStr 共性词
     */
    void updateAdditionTag(String preTaskId, String additionTagStr);

    /**
     * 提交训练任务
     *
     * @param preTaskId  预处理任务ID
     * @param modelName  模型名称
     * @param trainParams 训练参数
     * @param submitTime 提交时间
     */
    void submitTrainTask(String preTaskId, String modelName, String trainParams, Date submitTime);

    /**
     * 开始执行训练任务
     * @param preTaskId 预处理任务ID
     * @param taskId 训练任务ID
     * @param startTime 开始时间
     * @param sdGpuPool GPU 池
     */
    void startTrainTask(String preTaskId, String taskId, Date startTime, SdGpuPool sdGpuPool);

    /**
     * 完成训练任务
     * @param taskId 训练任务ID
     * @param endTime 完成时间
     */
    void completeTrainTask(String taskId, Date endTime);

    /**
     * 训练任务失败
     *
     * @param preTaskId 预处理任务ID
     * @param reason    原因
     * @param sdGpuPool GPU 池
     * @param endTime   结束时间
     */
    void failTrainTask(String preTaskId, String reason, SdGpuPool sdGpuPool, Date endTime);

    SdTrainTask selectDetailById(String preTaskId);

    SdTrainTask selectDetailByUserId(Long preTaskId);

    SdTrainTask selectDetailByTaskId(String taskId);

    void reduceImgNum(String preTaskId);

    void deleteById(String preTaskId);

    Integer selectNewStatusById(String preTaskId);

    JSONObject selectNewStatusByTaskId(String taskId);

    SdGpuPool selectGpuPoolByTaskId(String taskId);

    boolean deleteByIdAndPicNumIsZero(String preTaskId);

    Long selectCrtUserIdById(String preTaskId);
}
