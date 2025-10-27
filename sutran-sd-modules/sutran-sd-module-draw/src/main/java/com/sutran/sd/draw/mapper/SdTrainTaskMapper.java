package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdTrainTask;
import com.sutran.sd.draw.domain.vo.FluxgymTaskStatusVo;
import com.sutran.sd.draw.domain.vo.TrainTaskStatusVo;
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

    /**
     * 查询用户最新训练任务详情
     * @param userId 用户ID
     * @return 训练任务详情
     */
    @Select("SELECT id, pre_params AS preParams,task_id AS taskId, img_num AS imgNum, status, model_name AS modelName, train_params AS trainParams, new_status AS newStatus, addition_tag AS additionTag, crt_user_id AS crtUserId, crt_user_name AS crtUserName, crt_time AS crtTime, start_time AS startTime, end_time AS endTime FROM sd_train_task WHERE crt_user_id=#{userId} AND new_status IN (0,1,2) ORDER BY id DESC LIMIT 1")
    SdTrainTask selectDetailByUserId(@Param("userId") Long userId);

    /**
     * 更新训练任务ID和新状态
     * @param preTaskId 预处理任务ID
     * @param taskId 训练任务ID
     * @param trainParams 训练参数
     * @param modelName 模型名称
     * @param startTime 开始时间
     * @param newStatus 新状态
     * @param gupPool GPU池
     */
    @Update("UPDATE sd_train_task SET task_id=#{taskId},train_params=#{trainParams},model_name=#{modelName},start_time=#{startTime},new_status=#{newStatus},gpu_pool=#{gupPool} WHERE id=#{preTaskId}")
    void updateTaskIdAndNewStatus(@Param("preTaskId") String preTaskId, @Param("taskId") String taskId, @Param("trainParams") String trainParams, @Param("modelName") String modelName, @Param("startTime") Date startTime, @Param("newStatus") int newStatus, @Param("gupPool") String gupPool);

    /**
     * 查询训练任务详情
     * @param taskId 训练任务ID
     * @return 训练任务详情
     */
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

    /**
     * 减少预处理图片数量
     * @param preTaskId 预处理任务ID
     */
    @Update("UPDATE sd_train_task SET img_num=img_num-1 WHERE id=#{preTaskId} AND img_num>0")
    void reduceImgNum(@Param("preTaskId") String preTaskId);

     /**
     * 查询预处理任务新状态
     * @param preTaskId 预处理任务ID
     * @return 新状态
     */
    @Select("SELECT new_status FROM sd_train_task WHERE id=#{preTaskId}")
    Integer selectNewStatusById(@Param("preTaskId") String preTaskId);

     /**
     * 查询训练任务新状态
     * @param taskId 训练任务ID
     * @return 新状态
     */
    @Select("SELECT id AS preTaskId,new_status AS newStatus FROM sd_train_task WHERE task_id=#{taskId}")
    JSONObject selectNewStatusByTaskId(@Param("taskId") String taskId);

     /**
     * 查询训练任务状态
     * @param preTaskId 预处理任务ID
     * @return 训练任务状态
     */
    @Select("SELECT id AS preTaskId,new_status AS newStatus,task_id AS taskId FROM sd_train_task WHERE id=#{preTaskId}")
    TrainTaskStatusVo selectTaskStatusByPreTaskId(@Param("preTaskId") String preTaskId);

     /**
     * 查询训练任务GPU池
     * @param taskId 训练任务ID
     * @return GPU池
     */
    @Select("SELECT gpu_pool FROM sd_train_task WHERE task_id=#{taskId} ")
    String selectGpuPoolByTaskId(@Param("taskId") String taskId);

     /**
     * 删除预处理任务
     * @param preTaskId 预处理任务ID
     * @return 删除数量
     */
    @Delete("DELETE FROM sd_train_task WHERE id=#{preTaskId} AND img_num<=0")
    Integer deleteByIdAndPicNumIsZero(@Param("preTaskId") String preTaskId);

     /**
     * 查询创建用户ID
     * @param preTaskId 预处理任务ID
     * @return 用户ID
     */
    @Select("SELECT crt_user_id FROM sd_train_task WHERE id=#{preTaskId}")
    Long selectCrtUserIdById(@Param("preTaskId") String preTaskId);

    /**
     * 查询用户正在运行的训练任务数量
     * @param userId 用户ID
     * @return 训练任务数量
     */
    @Select("SELECT COUNT(*) FROM sd_train_task WHERE crt_user_id=#{userId} AND new_status IN (3,4)")
    Integer countRunningTaskByUserId(@Param("userId") Long userId);

     /**
      * 开始执行Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param nodeId 节点ID
      * @param startTime 开始时间
      * @param trainParams 训练参数
      */
    @Update("UPDATE sd_train_task SET new_status=4,start_time=#{startTime},node_id=#{nodeId},train_params=#{trainParams} WHERE id=#{taskId}")
    void startFluxgymTrainTask(@Param("taskId") String taskId, @Param("nodeId") Long nodeId, @Param("startTime") Date startTime, @Param("trainParams") String trainParams);

     /**
      * 查询训练任务节点URL、任务状态、任务预处理参数
      * @param taskId 训练任务ID
      * @return 节点URL
      */
    @Select("SELECT B.base_url AS baseUrl,A.node_id AS nodeId,A.pre_params AS preParams,A.new_status AS status,A.crt_user_id AS crtUserId,A.crt_user_name AS crtUserName,DATE_FORMAT(A.start_time,'%Y-%m-%d %H:%i:%s') AS startTime,DATE_FORMAT(A.end_time,'%Y-%m-%d %H:%i:%s') AS endTime " +
        "FROM sd_train_task AS A INNER JOIN sd_draw_node AS B ON A.node_id=B.id WHERE A.id=#{taskId}")
    JSONObject selectNodeBaseUrlAndStatusByTaskId(@Param("taskId") String taskId);

     /**
      * 完成Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param endTime 完成时间
      */
    @Update("UPDATE sd_train_task SET new_status=5,end_time=#{endTime} WHERE id=#{taskId}")
    void completeFluxgymTrainTask(@Param("taskId") String taskId, @Param("endTime") Date endTime);

     /**
      * 失败Fluxgym训练任务
      * @param taskId 训练任务ID
      * @param message 失败信息
      * @param endTime 完成时间
      */
    @Update("UPDATE sd_train_task SET new_status=6,reason=#{message},end_time=#{endTime} WHERE id=#{taskId}")
    void failFluxgymTrainTask(@Param("taskId") String taskId, @Param("message") String message, @Param("endTime") Date endTime);

     /**
      * 查询训练任务训练图片数量
      * @param taskId 训练任务ID
      * @return 训练图片数量
      */
    @Select("SELECT img_num FROM sd_train_task WHERE id=#{taskId}")
    int selectTrainImageNumById(@Param("taskId") String taskId);

    /**
     * 获取训练任务的预处理参数
     * @param id 任务ID
     * @return 预处理参数
     */
    @Select("SELECT pre_params FROM sd_train_task WHERE id=#{id}")
    String selectPreParamsById(@Param("id") String id);

    /**
     * [FluxGym]SD训练-查询训练任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    @Select("SELECT id AS taskId,new_status AS status FROM sd_train_task WHERE id=#{taskId}")
    FluxgymTaskStatusVo getFluxgymTaskStatus(@Param("taskId") String taskId);

    /**
     * FluxGym]SD训练-当前用户正在训练的任务ID
     * @param userId 用户ID
     * @return 进行中的任务ID
     */
    @Select("SELECT id FROM sd_train_task WHERE crt_user_id=#{userId} AND new_status IN (3,4)")
    String getDoingFluxgymTask(Long userId);
}
