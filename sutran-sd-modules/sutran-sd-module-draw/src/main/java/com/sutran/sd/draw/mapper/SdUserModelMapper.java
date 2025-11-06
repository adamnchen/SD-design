package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.domain.entity.SysUser;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.dto.model.SdUserModelDto;
import com.sutran.sd.draw.domain.dto.model.SdUserModelPageDto;
import com.sutran.sd.draw.domain.SdUserModel;
import com.sutran.sd.draw.domain.vo.ComfyUserModelVo;
import com.sutran.sd.draw.domain.vo.FluxgymModelListVo;
import com.sutran.sd.draw.domain.vo.SdUserModelVo;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelMapper extends BaseMapperPlus<SdUserModelMapper, SdUserModel, SdUserModel> {
    /**
     * 分页查询模型列表
     * @param dto       查询参数实体
     * @param userId    用户ID
     * @param page      分页参数实体
     * @return          模型列表
     */
    Page<SdUserModelVo> selectAllList(@Param("dto") SdUserModelPageDto dto, @Param("userId") Long userId, @Param("page") Page<SysUser> page);

    /**
     * 分页查询模型列表（管理员）
     * @param page  分页参数实体
     * @param dto   查询参数实体
     * @return      模型列表
     */
    Page<SdUserModelVo> selectAllListOfAdmin(@Param("page") Page<SysUser> page, @Param("dto") SdUserModelDto dto);

    /**
     * 获取模型详情
     * @param id        模型ID
     * @param userId    用户ID
     * @return          模型详情
     */
    SdUserModelVo getModelInfo(@Param("id") String id, @Param("userId") Long userId);

    /**
     * 获取最新模型列表
     * @param userId    用户ID
     * @param limit     数量
     * @return          模型列表
     */
    List<SdUserModelVo> getLatestModelInfo(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 插入模型数据
     * @param model 模型实体
     */
    void insertData(SdUserModel model);

    /**
     * 发布模型
     * @param id            模型ID
     * @param publishStatus 发布状态
     * @param modelStrength 模型强度
     */
    void publishModel(@Param("id") String id, @Param("publishStatus") Integer publishStatus, @Param("modelStrength") String modelStrength);

    /**
     * 修改模型强度
     * @param id            模型ID
     * @param modelStrength 模型强度
     */
    @Update("UPDATE sd_user_model SET model_strength=#{modelStrength} WHERE id=#{id}")
    void modifyModelStrength(@Param("id") String id, @Param("modelStrength") String modelStrength);

    /**
     * 获取模型详情列表
     * @param ids   模型ID列表
     * @return      模型详情列表
     */
    List<JSONObject> selectInfosByIds(@Param("ids") Set<String> ids);

    /**
     * 获取所有模型的hash值
     * @return 模型hash值列表
     */
    @Select("SELECT DISTINCT hash FROM sd_user_model")
    List<String> selectHashList();

    /**
     * 获取模型归属人的openId、手机号、用户ID、模型路径
     * @param id    任务ID
     * @return 数据
     */
    @Select("SELECT DISTINCT B.wx_open_id AS wxOpenId,B.phonenumber,B.user_id AS userId,A.file_name AS fileName,A.publish_status AS publishStatus FROM sd_user_model AS A,sys_user AS B WHERE A.belong_user_id=B.user_id AND A.id=#{id}")
    JSONObject selectUserOpenIdAndPhoneById(@Param("id") String id);

    /**
     * 获取任务中使用的LORA模型列表
     * @param taskId    任务ID
     * @return          模型列表
     */
    @Select("SELECT DISTINCT B.model_name_zh AS modelNameZh,DATE_FORMAT(C.crt_time,'%Y-%m-%d %H:%i:%s') AS startTime,DATE_FORMAT(C.upd_time,'%Y-%m-%d %H:%i:%s') AS endTime FROM sd_user_model_file AS A LEFT JOIN sd_user_model AS B ON A.lora_model_id=B.id LEFT JOIN sd_user_task AS C ON A.task_id=C.task_id WHERE A.task_id=#{taskId}")
    JSONObject selectLoraModelNameByTaskId(@Param("taskId") String taskId);

    /**
     * 删除分享给指定人的指定模型
     * @param modelId   模型ID
     * @param userId    用户ID
     */
    @Delete("DELETE FROM sd_user_model_share WHERE model_id=#{modelId} AND user_id=#{userId}")
    void removeShareModelById(@Param("modelId") String modelId, @Param("userId") Long userId);

    /**
     * 分享模型
     * @param modelId       模型Id
     * @param toShareUserId 被分享userId
     * @param userId        模型拥有者userId
     * @param date          分享时间
     */
    @Insert("INSERT IGNORE INTO sd_user_model_share (model_id, user_id, crt_user_id, crt_time) VALUES (#{modelId},#{toShareUserId},#{userId},#{date})")
    void shareModel(@Param("modelId") String modelId, @Param("toShareUserId") String toShareUserId, @Param("userId") Long userId, @Param("date") Date date);

    /**
     * 获取ComfyUI最近使用的n个模型列表
     * @param userId    用户ID
     * @param limit     数量
     * @return          模型列表
     */
    List<ComfyUserModelVo> getLatestModelInfoOfComfyui(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据任务ID查询模型的预参数
     * @param taskIds   任务ID列表
     * @return          模型预参数列表
     */
    List<JSONObject> selectPreParamByTaskIds(@Param("taskIds") List<String> taskIds);

    /**
     * 获取当前用户能看到的comfyui lora模型列表
     * @param dto       SdUserModelPageDto
     * @param userId    用户ID
     * @param page      分页查询参数
     * @return  TableDataInfo<ComfyUserModelVo>
     */
    Page<ComfyUserModelVo> selectAllListOfComfyui(@Param("dto") SdUserModelPageDto dto, @Param("userId") Long userId, @Param("page") Page<SysUser> page);

    /**
     * 获取归属当前用户的comfyui lora模型列表
     * @param dto       SdUserModelPageDto
     * @param userId    用户ID
     * @param page      分页查询参数
     * @return  TableDataInfo<ComfyUserModelVo>
     */
    Page<ComfyUserModelVo> selectUserAllListOfComfyui(@Param("dto") SdUserModelPageDto dto, @Param("userId") Long userId, @Param("page") Page<SysUser> page);

     /**
     * 获取comfyui lora模型详情
     * @param id    模型ID
     * @return      模型详情
     */
    ComfyUserModelVo selectModelInfoOfComfyui(@Param("id") String id);

    /**
     * [FluxGym]根据任务ID查询模型名称列表
     * @param taskId 训练任务id
     * @return 模型名称列表
     */
    @Select("SELECT id AS modelId,model_name_zh AS modelName,publish_status AS publishStatus FROM sd_user_model WHERE task_id=#{taskId} ORDER BY model_name_zh")
    List<FluxgymModelListVo> listModelNameOfFluxgym(@Param("taskId") String taskId);

    /**
     * 查询模型预览图
     * @param taskId    任务Id
     * @return  预览图地址列表
     */
    @Select("SELECT url FROM sd_user_model WHERE task_id=#{taskId} ORDER BY model_name_zh")
    List<String> selectModelUrlListByTaskId(@Param("taskId") String taskId);

    /**
     * 检查是否能删除当前任务下的模型（只要存在已发布的模型，就可以删除未发布的）
     * @param taskId    任务ID
     * @return 是否能删除
     */
    @Select("SELECT COUNT(1) FROM sd_user_model WHERE task_id=#{taskId} AND publish_status=1")
    boolean checkCanDelModelByTaskId(@Param("taskId") String taskId);

     /**
     * 逻辑删除个人模型
     * @param modelId   模型ID
     * @param userId    用户ID
     */
    @Update("UPDATE sd_user_model SET is_user_del=1 WHERE id=#{modelId} AND belong_user_id=#{userId}")
    void updateRemoveUserModelById(@Param("modelId") String modelId, @Param("userId") Long userId);
}
