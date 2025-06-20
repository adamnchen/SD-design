package com.sutran.sd.sdapi.modules.system;

import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainAdditionTagDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainPreImgDto;
import com.sutran.sd.sdapi.domain.dto.train.SdTrainTagDelDto;
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
     * 获取当前登录人已发起的训练任务
     * @param newStatus 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     * @param userId 登录人id
     * @return 任务集合
     */
    List<TrainTaskVo> getTrainTasks(Integer newStatus, Long userId);
    /**
     * SD-获取当前任务状态信息
     * @param preTaskId 预处理任务id
     * @return 任务状态
     */
    TrainTaskStatusVo getSdTaskStatus(String preTaskId);
    TrainTaskStatusVo getSdTaskStatusOfJob(String taskId);
    String submitPreImg(MultipartFile[] images, Double threshold, String interrogatorModel, String batchOutputActionOnConflict) throws IOException;
    JSONObject getPreImgList(String preTaskId) throws IOException;
    void delPreImg(String imgUrl, String preTaskId);
    void modifyPreImgTag(SdTrainPreImgDto dto);
    void insertAdditionTag(SdTrainAdditionTagDto dto);
    void delPreImgTag(SdTrainTagDelDto dto);
    boolean getPreImgProgress(String preTaskId);
    void trainSdLora(SdTrainLoraDto dto);
    TrainProcessDataVo trainProgress(String taskId);
    TrainProcessDataVo trainProgressV2(String taskId);
    void stopGpuPool(SdGpuPool sdGpuPool);
    void startGpuPool(SdGpuPool sdGpuPool);
    List<JSONObject> getModelTrainDateList(String preTaskId) throws IOException;
}
