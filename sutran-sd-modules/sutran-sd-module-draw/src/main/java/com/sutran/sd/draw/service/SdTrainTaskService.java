package com.sutran.sd.draw.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.draw.domain.vo.FluxgymTaskStatusVo;
import com.sutran.sd.draw.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.SdTrainTask;

import java.util.Date;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-24
 */
public interface SdTrainTaskService {

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

    /**
     * 查询训练任务详情
     * @param preTaskId 预处理任务ID
     * @return 训练任务详情
     */
    SdTrainTask selectDetailById(String preTaskId);

    /**
     * 查询训练任务详情
     * @param preTaskId 预处理任务ID
     * @return 训练任务详情
     */
    SdTrainTask selectDetailByUserId(Long preTaskId);

    /**
     * 查询训练任务详情
     * @param taskId 训练任务ID
     * @return 训练任务详情
     */
    SdTrainTask selectDetailByTaskId(String taskId);

    /**
     * 减少图片数量
     * @param preTaskId 预处理任务ID
     */
    void reduceImgNum(String preTaskId);

    /**
     * 删除训练任务
     * @param preTaskId 预处理任务ID
     */
    void deleteById(String preTaskId);

    /**
     * 查询训练任务状态
     * @param preTaskId 预处理任务ID
     * @return 训练任务状态
     */
    Integer selectNewStatusById(String preTaskId);

    /**
     * 查询训练任务状态
     * @param taskId 训练任务ID
     * @return 训练任务状态
     */
    JSONObject selectNewStatusByTaskId(String taskId);

    /**
     * 查询训练任务GPU池
     * @param taskId 训练任务ID
     * @return GPU 池
     */
    SdGpuPool selectGpuPoolByTaskId(String taskId);

    /**
     * 删除训练任务
     * @param preTaskId 预处理任务ID
     * @return 是否删除成功
     */
    boolean deleteByIdAndPicNumIsZero(String preTaskId);

    /**
     * 查询训练任务创建用户ID
     * @param preTaskId 预处理任务ID
     * @return 创建用户ID
     */
    Long selectCrtUserIdById(String preTaskId);

    /**
     * 查询用户正在运行的训练任务数量
     * @param userId 用户ID
     * @return 训练任务数量
     */
    Integer countRunningTaskByUserId(Long userId);

    /**
     * 插入训练任务
     * @param sdTrainTask 训练任务
     */
    void insert(SdTrainTask sdTrainTask);

     /**
      * 开始执行Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param nodeId 节点ID
      * @param startTime 开始时间
      * @param trainParams 训练参数
      */
    void startFluxgymTrainTask(String taskId, Long nodeId, Date startTime, Map<String, Object> trainParams);

    /**
     * 、任务状态、任务预处理参数
     * @param taskId 训练任务ID
     * @return 节点URL
     */
     JSONObject selectNodeBaseUrlAndStatusByTaskId(String taskId);

     /**
      * 完成Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param endTime 完成时间
      */
    void completeFluxgymTrainTask(String taskId, Date endTime);

     /**
      * 失败Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param message 失败信息
      * @param endTime 完成时间
      */
    void failFluxgymTrainTask(String taskId, String message, Date endTime);

     /**
      * 查询训练任务训练图片数量
      * @param taskId 训练任务ID
      * @return 训练图片数量
      */
    int selectTrainImageNumById(String taskId);

    /**
     * 按任务状态查询列表
     *
     * @param newStatus 任务状态
     * @param pageQuery 分页参数
     * @return 任务列表
     */
    Page<SdTrainTask> listTrainTaskOfFluxgym(Integer newStatus, PageQuery pageQuery);

    /**
     * 获取训练任务的预处理参数
     * @param id 任务ID
     * @return 预处理参数
     */
    String selectPreParamsById(String id);

    /**
     * [FluxGym]SD训练-查询训练任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    FluxgymTaskStatusVo getFluxgymTaskStatus(String taskId);

    /**
     * FluxGym]SD训练-当前用户正在训练的任务ID
     * @param userId 用户ID
     * @return 进行中的任务ID
     */
    String getDoingFluxgymTask(Long userId);
}
