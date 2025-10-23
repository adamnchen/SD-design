package com.sutran.sd.draw.service;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.SdGpuPool;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.domain.dto.img2img.SdImg2ImgDto;
import com.sutran.sd.draw.domain.dto.model.*;
import com.sutran.sd.draw.domain.dto.txt2img.SdText2ImgDto;
import com.sutran.sd.draw.domain.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * StableDiffusionAPI接口
 * @author zj
 * @date 2024-03-02
 */
public interface SdWebuiApiService {
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

    /**
     * 获取模型分类列表
     * @return  List<SdUserModelClassifyVo>
     */
    List<SdUserModelClassifyVo> listModelClassify();

    /**
     * 添加模型分类
     * @param dto   SdUserModelClassifyDto
     */
    void addModelClassify(SdUserModelClassifyDto dto);

    /**
     * 修改模型分类
     * @param dto   SdUserModelClassifyDto
     */
    void modifyModelClassify(SdUserModelClassifyDto dto);

    /**
     * 删除模型分类
     * @param id    模型分类ID
     */
    void removeModelClassify(String id);

    /**
     * 分享模型
     * @param dto   SdUserModelShareDto
     */
    void shareModel(SdUserModelShareDto dto);

    /**
     * 获取lora模型列表
     * @param dto   SdUserModelPageDto
     * @param pageQuery  分页查询参数
     * @return  TableDataInfo<SdUserModelVo>
     */
    TableDataInfo<SdUserModelVo> listLoraModels(SdUserModelPageDto dto, PageQuery pageQuery);

    /**
     * 获取lora模型列表-测试任务和训练数据
     * @param dto   SdUserModelDto
     * @return  TableDataInfo<SdUserModelVo>
     */
    TableDataInfo<SdUserModelVo> listLoraModelsOfTestTaskAndTrainData(SdUserModelDto dto);

    /**
     * 删除模型
     * @param id    模型ID
     */
    void removeModel(String id);

    /**
     * 删除模型-管理员
     * @param ids    模型ID列表
     */
    void removeModelOfAdmin(List<String> ids);

    /**
     * 修改模型
     * @param dto   SdUserModelModifyDto
     */
    void modifyModel(SdUserModelModifyDto dto);

    /**
     * 获取模型信息
     * @param id    模型ID
     * @return  SdUserModelVo
     */
    SdUserModelVo getModelInfo(String id);

    /**
     * 获取最新模型列表
     * @param limit  数量
     * @return  List<SdUserModelVo>
     */
    List<SdUserModelVo> getLatestModelInfo(int limit);

    /**
     * 文本生成图片
     * @param dto   SdText2ImgDto
     * @return  String
     */
    String txt2img(SdText2ImgDto dto);

    /**
     * 图片生成图片
     * @param dto   SdImg2ImgDto
     * @return  String
     */
    String img2img(SdImg2ImgDto dto);

    /**
     * 测试lora模型（xyz）
     * @param dto   SdText2ImgDto
     * @return  String
     */
    String testTxt2ImgOfLoraModel(SdText2ImgDto dto);

    /**
     * 获取用户任务列表
     * @param pageQuery 分页查询参数
     * @param category  任务类型
     * @param status    任务状态
     * @return  TableDataInfo<SdUserTaskVo>
     */
    TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Integer category, Integer status);

    /**
     * 获取所有用户任务列表
     * @param pageQuery 分页查询参数
     * @param category  任务类型
     * @param status    任务状态
     * @return  TableDataInfo<SdUserTaskVo>
     */
    TableDataInfo<SdUserTaskVo> allUserTaskList(PageQuery pageQuery, Integer category, Integer status);

    /**
     * 获取用户模型文件列表
     * @param pageQuery 分页查询参数
     * @param dto   SdUserModelFilePageDto
     * @return  TableDataInfo<SdUserModelFileVo>
     */
    TableDataInfo<SdUserModelFileVo> listUserModelFile(PageQuery pageQuery, SdUserModelFilePageDto dto);

    /**
     * 获取用户模型文件列表-测试任务和训练数据
     * @param taskId    任务ID
     * @return  List<SdUserModelFileVo>
     */
    List<SdUserModelFileVo> listUserModelFile(String taskId);

    /**
     * 删除用户模型文件
     * @param ids    用户模型文件ID列表
     */
    void removeUserModelFile(List<String> ids);

    /**
     * 获取任务进度
     * @param taskId    任务ID
     * @return  JSONObject
     */
    SdWebuiProgressVo getProcess(String taskId);

    /**
     * 发布模型
     * @param id            模型ID
     * @param publishStatus 发布状态
     * @param modelStrength 模型强度
     */
    void publishModel(String id, Integer publishStatus, String modelStrength);

    /**
     * 修改模型强度
     * @param id            模型ID
     * @param modelStrength 模型强度
     */
    void modifyModelStrength(String id, String modelStrength);

    /**
     * 获取当前正在进行的任务ID
     * @param category  任务类型
     * @return  String
     */
    String getDoingTaskId(Integer category);

    /**
     * 删除任务
     * @param taskId    任务ID
     */
    void deleteTaskById(String taskId);

    /**
     * 批量下载模型文件
     * @param taskId    任务ID
     * @param response  HttpServletResponse
     * @throws IOException  IOException
     */
    void batchDownloadModelFile(String taskId, HttpServletResponse response) throws IOException;

    /**
     * 批量下载用户模型文件
     * @param ids       模型ID列表
     * @param response  HttpServletResponse
     * @throws IOException  IOException
     */
    void batchDownloadUserModelFile(List<String> ids, HttpServletResponse response) throws IOException;

    /**
     * 下载用户模型文件
     * @param imgUrl    图片URL
     * @param response  HttpServletResponse
     * @throws IOException  IOException
     */
    void downloadUserModelFile(String imgUrl, HttpServletResponse response) throws IOException;

    /**
     * 同步GPU池
     * @param type  类型
     * @return  JSONObject
     */
    JSONObject syncGpuPool(Integer type);

    /**
     * 停止GPU池
     * @param sdGpuPool  SdGpuPool
     */
    void stopGpuPool(SdGpuPool sdGpuPool);

    /**
     * 启动GPU池
     * @param sdGpuPool  SdGpuPool
     */
    void startGpuPool(SdGpuPool sdGpuPool);

    /**
     * 删除XYZ数据
     * @param taskId    任务ID
     */
    void delXyzData(String taskId);
}
