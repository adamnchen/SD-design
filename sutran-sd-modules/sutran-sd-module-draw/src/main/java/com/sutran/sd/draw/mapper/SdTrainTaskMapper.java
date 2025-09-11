package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.draw.domain.SdTrainTask;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdTrainTaskMapper extends BaseMapperPlus<SdTrainTaskMapper, SdTrainTask, SdTrainTask> {

    @Select("SELECT id, pre_params AS preParams,task_id AS taskId, img_num AS imgNum, status, model_name AS modelName, train_params AS trainParams, new_status AS newStatus, addition_tag AS additionTag, crt_user_id AS crtUserId, crt_user_name AS crtUserName, crt_time AS crtTime, start_time AS startTime, end_time AS endTime FROM sd_train_task WHERE crt_user_id=#{userId} AND status IN (0,1) ORDER BY id DESC LIMIT 1")
    SdTrainTask selectDetailByUserId(@Param("userId") Long userId);

    @Update("UPDATE sd_train_task SET task_id=#{taskId},train_params=#{trainParams},model_name=#{modelName},start_time=#{startTime},new_status=#{newStatus},gpu_pool=#{gupPool} WHERE id=#{preTaskId}")
    void updateTaskIdAndNewStatus(@Param("preTaskId") String preTaskId, @Param("taskId") String taskId, @Param("trainParams") String trainParams, @Param("modelName") String modelName, @Param("startTime") Date startTime, @Param("newStatus") int newStatus, @Param("gupPool") String gupPool);

    @Select("SELECT id, pre_params AS preParams,task_id AS taskId, img_num AS imgNum, status, new_status AS newStatus, model_name AS modelName, train_params AS trainParams, addition_tag AS additionTag, crt_user_id AS crtUserId, crt_user_name AS crtUserName, crt_time AS crtTime, start_time AS startTime, end_time AS endTime FROM sd_train_task WHERE task_id=#{taskId} OR id=#{taskId} ORDER BY id DESC LIMIT 1")
    SdTrainTask selectDetailByTaskId(@Param("taskId") String taskId);


    /**
     * 开始执行预处理任务
     * @param preTaskId 预处理任务ID
     * @param preStartTime 开始时间
     */
    void startPreTask(@Param("preTaskId") String preTaskId,
                      @Param("preStartTime") Date preStartTime);

    /**
     * 完成预处理任务
     * @param preTaskId 预处理任务ID
     * @param reason 原因
     * @param preEndTime 结束时间
     */
    void completePreTask(@Param("preTaskId") String preTaskId,
                         @Param("reason") String reason,
                         @Param("preEndTime") Date preEndTime);
    /**
     * 添加共性词
     * @param preTaskId 预处理任务ID
     * @param additionTagStr 共性词
     */
    @Update("UPDATE sd_train_task SET addition_tag=#{additionTagStr} WHERE id=#{preTaskId}")
    void updateAdditionTag(@Param("preTaskId") String preTaskId,
                           @Param("additionTagStr") String additionTagStr);

    /**
     * 提交训练任务
     *
     * @param preTaskId   预处理任务ID
     * @param modelName 模型名称
     * @param trainParams 训练参数
     * @param submitTime  提交时间
     */
    void submitTrainTaskById(@Param("preTaskId") String preTaskId,
                             @Param("modelName") String modelName,
                             @Param("trainParams") String trainParams,
                             @Param("submitTime") Date submitTime);

    /**
     * 开始执行训练任务
     * @param preTaskId 预处理任务ID
     * @param taskId 训练任务ID
     * @param startTime 开始时间
     * @param sdGpuPool GPU池
     */
    void startTrainTask(@Param("preTaskId") String preTaskId,
                        @Param("taskId") String taskId,
                        @Param("startTime") Date startTime,
                        @Param("sdGpuPool") String sdGpuPool);

    /**
     * 完成训练任务
     * @param taskId 训练任务ID
     * @param endTime 结束时间
     */
    @Update("UPDATE sd_train_task SET status=2,new_status=5,end_time=#{endTime} WHERE task_id=#{taskId}")
    void completeTrainTask(@Param("taskId") String taskId, @Param("endTime") Date endTime);

    /**
     * 训练任务失败
     *
     * @param preTaskId 预处理任务ID
     * @param reason    原因
     * @param sdGpuPool GPU 池
     * @param endTime   结束时间
     */
    @Update("UPDATE sd_train_task SET new_status=6,reason=#{reason},gpu_pool=#{sdGpuPool},end_time=#{endTime} WHERE id=#{preTaskId}")
    void failTrainTask(@Param("preTaskId") String preTaskId,
                       @Param("reason") String reason,
                       @Param("sdGpuPool") String sdGpuPool,
                       @Param("endTime") Date endTime);

    @Update("UPDATE sd_train_task SET img_num=img_num-1 WHERE id=#{preTaskId} AND img_num>0")
    void reduceImgNum(@Param("preTaskId") String preTaskId);

    @Select("SELECT new_status FROM sd_train_task WHERE id=#{preTaskId}")
    Integer selectNewStatusById(@Param("preTaskId") String preTaskId);

    @Select("SELECT id AS preTaskId,new_status AS newStatus FROM sd_train_task WHERE task_id=#{taskId}")
    JSONObject selectNewStatusByTaskId(@Param("taskId") String taskId);

    @Select("SELECT id AS preTaskId,new_status AS newStatus,task_id AS taskId FROM sd_train_task WHERE id=#{preTaskId}")
    TrainTaskStatusVo selectTaskStatusByPreTaskId(@Param("preTaskId") String preTaskId);

    @Select("SELECT gpu_pool FROM sd_train_task WHERE task_id=#{taskId} ")
    String selectGpuPoolByTaskId(@Param("taskId") String taskId);

    @Delete("DELETE FROM sd_train_task WHERE id=#{preTaskId} AND img_num<=0")
    Integer deleteByIdAndPicNumIsZero(@Param("preTaskId") String preTaskId);

    @Select("SELECT crt_user_id FROM sd_train_task WHERE id=#{preTaskId}")
    Long selectCrtUserIdById(@Param("preTaskId") String preTaskId);
}
