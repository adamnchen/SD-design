package com.sutran.sd.draw.service;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.draw.domain.dto.model.SdUserModelDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelModifyDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelShareDto;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.vo.ComfyUserModelVo;
import com.sutran.sd.draw.domain.vo.FluxgymModelListVo;
import com.sutran.sd.draw.domain.vo.SdUserModelVo;

import java.util.List;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserModelService {

    /**
     * 批量插入模型
     * @param models    模型列表
     */
    void batchInsert(List<SdUserModel> models);

    /**
     * 批量添加模型
     * @param models    模型列表
     */
    void batchAdd(List<SdUserModel> models);

    /**
     * 分页查询模型列表
     * @param dto       查询参数实体
     * @param userId    用户ID
     * @param pageQuery 分页参数实体
     * @return          模型列表
     */
    TableDataInfo<SdUserModelVo> selectAllList(SdUserModelPageDto dto, Long userId, PageQuery pageQuery);

    /**
     * 查询任务和训练数据中使用的LORA模型列表
     * @param dto       查询参数实体
     * @return          模型列表
     */
    TableDataInfo<SdUserModelVo> listLoraModelsInTaskAndTrainData(SdUserModelDto dto);

    /**
     * 修改模型
     * @param dto       修改参数实体
     * @param userId    用户ID
     */
    void modifyModel(SdUserModelModifyDto dto, Long userId);

    /**
     * 获取模型详情
     * @param modelId   模型ID
     * @param userId    用户ID
     * @return          模型详情
     */
    SdUserModelVo getModelInfo(String modelId, Long userId);

    /**
     * 获取最新模型列表
     * @param userId    用户ID
     * @param limit     数量
     * @return          模型列表
     */
    List<SdUserModelVo> getLatestModelInfo(Long userId, int limit);

    /**
     * 根据模型ID查询模型
     * @param modelId   模型ID
     * @return          模型实体
     */
    SdUserModel selectById(String modelId);

    /**
     * 发布模型
     * @param id            模型ID
     * @param publishStatus 发布状态
     * @param isUserDel     是否用户删除
     * @param modelStrength 模型强度
     */
    void publishModel(String id, Integer publishStatus, Integer isUserDel, String modelStrength);

    /**
     * 修改模型强度
     * @param id            模型ID
     * @param modelStrength 模型强度
     */
    void modifyModelStrength(String id, String modelStrength);

    /**
     * 判断模型是否存在
     * @param id        模型ID
     * @param userId    用户ID
     * @return          是否存在
     */
    boolean isExistByClassifyId(String id, Long userId);

    /**
     * 删除模型
     * @param id        模型ID
     * @param userId    用户ID
     */
    void removeModelById(String id, Long userId);

    /**
     * 删除模型(admin)
     * @param id    模型ID
     */
    void removeModelOfAdminById(long id);

    /**
     * 根据模型ID列表查询模型列表
     * @param ids   模型ID列表
     * @return      模型列表
     */
    List<SdUserModel> selectByIds(List<String> ids);

    /**
     * 查询模型哈希列表
     * @return  模型哈希列表
     */
    List<String> selectHashList();

    /**
     * 根据模型ID查询用户OpenID和手机号
     * @param id    模型ID
     * @return      用户OpenID和手机号
     */
    JSONObject selectUserOpenIdAndPhoneById(String id);

    /**
     * 删除分享给指定人的指定模型
     * @param modelId   模型ID
     * @param userId    用户ID
     */
    void removeShareModelById(String modelId, Long userId);

    /**
     * 分享指定模型
     * @param dto       分享参数实体
     * @param userId    模型拥有者userId
     */
    void shareModel(SdUserModelShareDto dto, Long userId);

     /**
     * 获取ComfyUI最近使用的n个模型列表
     * @param userId    用户ID
     * @param num       数量
     * @return          模型列表
     */
    List<ComfyUserModelVo> getLatestModelInfoOfComfyui(Long userId, int num);

    /**
     * 获取comfyui lora模型列表
     * @param dto       SdUserModelPageDto
     * @param pageQuery 分页查询参数
     * @return  TableDataInfo<ComfyUserModelVo>
     */
    TableDataInfo<ComfyUserModelVo> listLoraModelsOfComfyui(SdUserModelPageDto dto, PageQuery pageQuery);

     /**
     * 获取comfyui lora模型详情
     * @param id    模型ID
     * @return      模型详情
     */
    ComfyUserModelVo getModelInfoOfComfyui(String id);

    /**
     * [FluxGym]根据任务ID查询模型名称列表
     * @param taskId 训练任务id
     * @return 模型名称列表
     */
    List<FluxgymModelListVo> listModelNameOfFluxgym(String taskId);

    /**
     * 查询模型预览图
     * @param taskId    任务Id
     * @return  预览图地址列表
     */
    List<String> selectModelUrlListByTaskId(String taskId);
}
