package com.sutran.sd.sdapi.modules.webui;

import com.alibaba.fastjson2.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.sdapi.domain.dto.model.*;
import com.sutran.sd.sdapi.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.sdapi.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.sdapi.modules.system.entity.SdGpuPool;
import com.sutran.sd.sdapi.modules.system.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * StableDiffusionAPI接口
 * @author zj
 * @date 2024-03-02
 */
public interface SdApiService {
    /**
     * 获取基础大模型列表
     * @return  List<CheckPointVo>
     */
    List<CheckPointVo> listCheckpointModels();

    /**
     * 切换基础大模型
     * @param title  模型名称
     */
    void checkpointOptions(String title);

    /**
     * 刷新并获取lora模型
     */
    void refreshLoraModels();

    List<SdUserModelClassifyVo> listModelClassify();

    void addModelClassify(SdUserModelClassifyDto dto);

    void modifyModelClassify(SdUserModelClassifyDto dto);

    void removeModelClassify(String id);

    void shareModel(SdUserModelShareDto dto);

    TableDataInfo<SdUserModelVo> listLoraModels(SdUserModelPageDto dto, PageQuery pageQuery);

    TableDataInfo<SdUserModelVo> listLoraModelsOfTestTaskAndTrainData(SdUserModelDto dto);

    void removeModel(String id);

    void removeModelOfAdmin(List<String> ids);

    void modifyModel(SdUserModelModifyDto dto);

    SdUserModelVo getModelInfo(String id);

    List<SdUserModelVo> getLatestModelInfo(int limit);

    String txt2img(SdText2ImgDto dto);

    String img2img(SdImg2ImgDto dto);

    String testTxt2ImgOfLoraModel(SdText2ImgDto dto);

    TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Integer category, Integer status);

    TableDataInfo<SdUserTaskVo> allUserTaskList(PageQuery pageQuery, Integer category, Integer status);

    TableDataInfo<SdUserModelFileVo> listUserModelFile(PageQuery pageQuery, SdUserModelFilePageDto dto);

    List<SdUserModelFileVo> listUserModelFile(String taskId);

    void removeUserModelFile(List<String> ids);

    JSONObject getProcess(String taskId);

    void publishModel(String id, Integer publishStatus, String modelStrength);

    void modifyModelStrength(String id, String modelStrength);

    String getDoingTaskId(Integer category);

    void deleteTaskById(String taskId);

    void batchDownloadModelFile(String taskId, HttpServletResponse response) throws IOException;

    void batchDownloadUserModelFile(List<String> ids, HttpServletResponse response) throws IOException;

    void downloadUserModelFile(String imgUrl, HttpServletResponse response) throws IOException;

    JSONObject syncGpuPool(Integer type);

    void stopGpuPool(SdGpuPool sdGpuPool);

    void startGpuPool(SdGpuPool sdGpuPool);

    void delXyzData(String taskId);
}
