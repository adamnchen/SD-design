package com.sutran.sd.sdapi.modules.train;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainAdditionTagDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainPreImgDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainTagDelDto;
import com.sutran.sd.sdapi.domain.vo.TrainPreImgTaskVo;
import com.sutran.sd.sdapi.domain.vo.TrainProcessDataVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskStatusVo;
import com.sutran.sd.sdapi.domain.vo.TrainTaskVo;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
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
    void stopGpuPool(SdGpuPool sdGpuPool);
    void startGpuPool(SdGpuPool sdGpuPool);
    List<JSONObject> getModelTrainDateList(String preTaskId) throws IOException;
}
