package com.sutran.sd.draw.service;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.SdUserWork;
import com.sutran.sd.draw.domain.dto.SdUserModelFilePageDto;
import com.sutran.sd.draw.domain.vo.*;

import java.util.List;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserWorkService {

    /**
     * 异步批量插入
     * @param rs     接口返回结果
     * @param userId        用户id
     * @param userName      用户名
     * @param loraInfos     lora信息
     * @param modelName     模型名称
     * @param taskId        任务id
     * @param category      类别
     * @param prompt        提示词
     * @param initImg       初始化图片
     * @param promptDesc    提示词描述
     * @param promptZh      提示词中文
     * @param negativePrompt    负面提示词
     * @param negativePromptZh  负面提示词中文
     * @param isRedraw      是否重绘
     */
    void asyncBatchInsert(SdApiResult rs, Long userId, String userName, List<JSONObject> loraInfos, String modelName, String taskId, int category, String prompt, String initImg, String promptDesc, String promptZh, String negativePrompt, String negativePromptZh, Integer isRedraw);

    /**
     * 异步批量插入
     *
     * @param sdUserTaskVo 任务信息
     * @param urlList      图片url列表
     */
    void asyncBatchInsert(SdUserTaskVo sdUserTaskVo, List<String> urlList);

    /**
     * 分页查询用户模型文件列表
     * @param pageQuery 分页查询参数
     * @param userId    用户id
     * @param dto       查询参数
     * @return          模型文件列表
     */
    TableDataInfo<SdUserWorkVo> listUserWork(PageQuery pageQuery, Long userId, SdUserModelFilePageDto dto);

    /**
     * 分页查询用户模型文件列表
     * @param taskId    任务id
     * @param userId    用户id
     * @return          模型文件列表
     */
    List<SdUserWorkVo> listUserWork(String taskId, Long userId);

    /**
     * 删除用户模型文件
     * @param ids     模型文件id列表
     * @param userId  用户id
     */
    void removeUserWork(List<String> ids, Long userId);

    /**
     * 删除用户模型文件
     * @param taskId 任务id
     */
    void removeUserWorkByTaskId(String taskId);

    /**
     * 查询用户模型文件图片url列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return       图片url列表
     */
    List<String> listImgUrlByTaskId(String taskId, Long userId);

    /**
     * 查询用户模型文件图片url列表
     * @param ids 模型文件id列表
     * @return    图片url列表
     */
    List<String> listImgUrlByIds(List<String> ids);

    /**
     * 查询模型测试数据和任务信息
     * @param loraModelId lora模型id
     * @return            模型测试数据和任务信息
     */
    List<JSONObject> selectModelTestDataAndTaskInfo(String loraModelId);

    /**
     * 查询指定任务的生图列表
     * @param taskId 任务id
     * @return 任务详情
     */
    List<ComfyUserWorkVo> getComfyImageOutputByTaskId(String taskId);

    /**
     * 检查指定任务是否已生成图片
     * @param taskId 任务id
     * @return 是否已生成图片
     */
    boolean checkHasImgByTaskId(String taskId);

    /**
     * 根据用户ID查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @return 用户生图文件数据记录集合
     */
    List<SdUserWork> selectSdUserWorkListByUserId(Long userId);

    /**
     * 根据用户ID和分类查询用户生图文件数据记录列表
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @return 用户生图文件数据记录集合
     */
    List<SdUserWork> selectSdUserWorkListByUserIdAndCategory(Long userId, Integer category);

    /**
     * 新增用户生图文件数据记录
     *
     * @param sdUserModelFile 用户生图文件数据记录
     * @return 结果
     */
    int insertSdUserWork(SdUserWork sdUserModelFile);




    /**
     * 批量删除用户生图文件数据记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSdUserWorkByIds(Long[] ids);

    /**
     * 删除用户生图文件数据记录信息
     *
     * @param id 用户生图文件数据记录主键
     * @return 结果
     */
    int deleteSdUserWorkById(Long id);

    /**
     * 获取我的作品详情（VO格式）
     *
     * @param id 作品ID
     * @param userId 用户ID
     * @return 作品详情
     */
    UserWorkVo getMyWorkDetail(Long id, Long userId);

    /**
     * 获取我的作品列表（分页查询）
     *
     * @param userId 用户ID
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    TableDataInfo<UserWorkVo> getMyWorksPage(Long userId, PageQuery pageQuery);

    /**
     * 根据分类获取我的作品列表（分页查询）
     *
     * @param userId 用户ID
     * @param category 分类[0-文生图，1-图生图]
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    TableDataInfo<UserWorkVo> getMyWorksByCategoryPage(Long userId, Integer category, PageQuery pageQuery);

    /**
     * 设置或取消作品公开
     *
     * @param id 作品ID
     * @param userId 当前用户ID
     * @param isPublic 是否公开 true/false
     * @return 操作是否成功
     */
    boolean setPublic(Long id, Long userId, boolean isPublic);

    /**
     * 查询公开作品列表（分页）
     *
     * @param pageQuery 分页查询参数
     * @return 分页结果（仅公开作品）
     */
    TableDataInfo<UserWorkVo> getPublicWorksPage(PageQuery pageQuery);
}
