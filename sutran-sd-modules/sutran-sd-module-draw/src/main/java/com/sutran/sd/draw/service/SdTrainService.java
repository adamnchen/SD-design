package com.sutran.sd.draw.service;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.bo.TrainCaptionBo;
import com.sutran.sd.draw.domain.dto.model.SdTrainTaskDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainAdditionTagDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainPreImgDto;
import com.sutran.sd.draw.domain.dto.train.SdTrainTagDelDto;
import com.sutran.sd.draw.domain.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author zj
 * @date 2024-03-24
 */
public interface SdTrainService {
    /**
     * [V2]获取当前登录人已发起的训练任务V2
     *
     * @param pageQuery 分页参数
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @param userId    登录人id
     * @return 任务集合
     */
    TableDataInfo<TrainTaskVo> getTrainTasksV2(PageQuery pageQuery, Integer newStatus, Long userId);
    /**
     * [V2]SD-获取当前任务状态信息
     * @param preTaskId 预处理任务id
     * @return 任务状态
     */
    TrainTaskStatusVo getSdTaskStatus(String preTaskId);
    /**
     * SD-定时任务获取当前任务状态信息
     * @param taskId 任务id
     * @return 任务状态
     */
    TrainTaskStatusVo getSdTaskStatusOfJob(String taskId);
    /**
     * SD-获取预处理图片任务相关数据
     * @param preTaskId 预处理任务id
     * @return 预处理图片列表
     * @throws IOException IO异常
     */
    TrainPreImgTaskVo getPreImgList(String preTaskId) throws IOException;
    /**
     * [V2]SD-获取预处理图片任务相关数据
     * @param preTaskId 预处理任务id
     * @return 预处理图片列表
     * @throws IOException IO异常
     */
    TrainPreImgTaskVo getPreImgListV2(String preTaskId) throws IOException;
    /**
     * SD-提交预处理图片
     * @param images                        图片集合
     * @param threshold                     阈值
     * @param interrogatorModel             识别模型名称
     * @param batchOutputActionOnConflict   冲突时的处理方式
     * @return 预处理任务id
     * @throws IOException                  IO异常
     */
    String submitPreImg(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException;
    /**
     * [V2]SD-提交预处理图片
     * @param images                        图片集合
     * @param threshold                     阈值
     * @param interrogatorModel             识别模型名称
     * @param batchOutputActionOnConflict   冲突时的处理方式
     * @return 预处理任务id
     * @throws IOException                  IO异常
     */
    String submitPreImgV2(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException;

    /**
     * 删除预处理中的指定图片
     * @param imgUrl                        图片url
     * @param preTaskId                     预处理任务id
     */
    void delPreImg(String imgUrl, String preTaskId);

    /**
     * 修改指定图片上的标签值
     * @param dto                           图片信息
     */
    void modifyPreImgTag(SdTrainPreImgDto dto);
    /**
     * 添加共性词到全部预处理图片中
     * @param dto                           共性词信息
     */
    void insertAdditionTag(SdTrainAdditionTagDto dto);
    /**
     * 删除预处理中的指定标签
     * @param dto                           标签信息
     */
    void delPreImgTag(SdTrainTagDelDto dto);
    /**
     * 获取指定预处理任务的进度
     * @param preTaskId                     预处理任务id
     * @return                              进度
     */
    boolean getPreImgProgress(String preTaskId);
    /**
     * [V2]获取指定预处理任务的进度
     * @param preTaskId                     预处理任务id
     * @return                              进度
     */
    int getPreImgProgressV2(String preTaskId);
    /**
     * 训练模型
     * @param dto                           训练信息
     */
    void trainSdLora(SdTrainLoraDto dto);
    /**
     * [V2]训练模型
     * @param dto                           训练信息
     */
    void trainSdLoraV2(SdTrainLoraDto dto);
    /**
     * 获取训练进度
     * @param taskId                        任务id
     * @return                              进度
     */
    TrainProcessDataVo trainProgress(String taskId);
    /**
     * [V2]获取训练进度
     * @param taskId                        任务id
     * @return                              进度
     */
    TrainProcessDataVo trainProgressV2(String taskId);
    /**
     * 停止GPU卡池
     * @param sdGpuPool GPU卡池
     */
    void stopGpuPool(SdGpuPool sdGpuPool);
    /**
     * 启动GPU卡池
     * @param sdGpuPool GPU卡池
     */
    void startGpuPool(SdGpuPool sdGpuPool);
    /**
     * 获取取训练模型的数据集
     * @param preTaskId 训练任务ID
     * @throws IOException IO异常
     * @return 数据集
     */
    List<JSONObject> getModelTrainDateList(String preTaskId) throws IOException;

     /**
      * [FluxGym]SD训练-图片识别
      * @param images 图片集合
      * @param loraName      训练模型名称(用于触发词)
      * @return 识别结果
      */
     FluxgymImgDealResultVo imgIdentifyTask(MultipartFile[] images, String loraName);

     /**
      * [V2]SD训练-提交训练
      *
      * @param images        图片集合
      * @param loraName      训练模型名称(用于触发词)
      * @param captions      图片描述词
      * @param modelTag      模型标签
      * @param isOpen        是否公开[0-否,1-是]
      * @param modelDesc     模型描述
      * @return 任务id
      * @throws IOException 图片IO异常
      */
    String startTrainTaskV2(MultipartFile[] images, String loraName, List<TrainCaptionBo> captions, String modelTag, Integer isOpen, String modelDesc) throws IOException;

     /**
      * [FluxGym]查询训练进度
      *
      * @param taskId 任务id
      * @param nodeId 节点id
      * @param isSchedule 是否定时任务查询
      * @return 进度
      */
     FluxgymTrainProgressVo getFluxgymProgress(String taskId, String nodeId,boolean isSchedule);

    /**
     * [FluxGym]查询训练任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
     FluxgymTaskStatusVo getFluxgymTaskStatus(String taskId);

     /**
      * [FluxGym]处理训练完成后的模型文件
      *
      * @param taskId     任务id
      */
     void dealFluxgymTrainModelFile(String taskId);

    /**
     * [FluxGym]查询训练任务列表
     * @param dto 查询参数实体
     * @return 训练任务列表
     */
     TableDataInfo<TrainTaskVo> listTrainTaskOfFluxgym(SdTrainTaskDto dto);

     /**
      * [FluxGym]根据任务ID查询模型名称列表
      * @param taskId 训练任务id
      * @return 模型名称列表
      */
    List<FluxgymModelListVo> listModelNameOfFluxgym(String taskId);

     /**
      * [FluxGym]根据任务ID查询模型素材原图、提示词和缩略图
      * @param taskId 训练任务id
      * @return 模型名称列表
      */
    FluxgymModelPreviewVo listModelPreviewOfFluxgym(String taskId);

     /**
      * [FluxGym]发布/取消发布模型
      * @param id 模型id
      * @param publishStatus 发布状态[0-取消发布,1-发布]
      */
    void publishModelOfFluxgym(String id, Integer publishStatus);

     /**
      * [FluxGym]删除当前任务未发布模型
      * @param taskId 任务id
      */
    void removeUnpublishedModelOfFluxgym(String taskId);

    /**
     * [FluxGym]当前用户正在训练的任务ID
     * @param userId 用户ID
     * @return 进行中的任务ID
     */
    String getDoingFluxgymTask(Long userId);

    /**
     * [FluxGym]获取当前登录人已发起的训练任务V2
     *
     * @param pageQuery 分页参数
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @param userId    登录人id
     * @return 任务集合
     */
    TableDataInfo<TrainTaskVo> getFluxgymTrainTasks(PageQuery pageQuery, Integer newStatus, Long userId);
}
