package com.sutran.sd.draw.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdUserModelFile;
import com.sutran.sd.draw.domain.vo.ComfyUserModelFileVo;
import com.sutran.sd.draw.domain.vo.SdUserModelFileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelFileMapper extends BaseMapperPlus<SdUserModelFileMapper, SdUserModelFile, SdUserModelFile> {
    /**
     * 根据任务id列表和用户id查询用户模型文件列表
     * @param taskIds 任务id列表
     * @param userId  用户id
     * @return        用户模型文件列表
     */
    List<SdUserModelFileVo> selectListByTaskIdsAndUserId(@Param("taskIds") List<String> taskIds, @Param("userId") Long userId);

    /**
     * 查询所有用户模型文件列表
     * @param page     分页参数
     * @param userId   用户id
     * @param taskId   任务id
     * @param category 模型类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param keyword   关键词
     * @return         用户模型文件列表
     */
    Page<SdUserModelFileVo> selectAllList(@Param("page") Page<Object> page, @Param("userId") Long userId, @Param("taskId") String taskId, @Param("category") Integer category, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("keyword") String keyword);

    /**
     * 根据任务id查询所有用户模型文件列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return       用户模型文件列表
     */
    List<SdUserModelFileVo> selectAllListByTaskId(@Param("taskId") String taskId, @Param("userId") Long userId);

    /**
     * 根据用户模型文件id列表和用户id删除用户模型文件
     * @param ids     用户模型文件id列表
     * @param userId  用户id
     */
    void batchDelByIds(@Param("ids") List<String> ids, @Param("userId") Long userId);

    /**
     * 根据任务id和用户id查询第一个用户模型文件url
     * @param taskId 任务id
     * @param userId 用户id
     * @return       用户模型文件url
     */
    SdUserModelFileVo selectFirstUrlByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") Long userId);

    /**
     * 根据任务id删除用户模型文件
     * @param taskId 任务id
     */
    void removeUserModelFileByTaskId(@Param("taskId") String taskId);

    /**
     * 根据任务id和用户id查询所有用户模型文件url列表
     * @param taskId 任务id
     * @param userId 用户id
     * @return       用户模型文件url列表
     */
    List<String> listImgUrlByTaskId(@Param("taskId") String taskId, @Param("userId") Long userId);

    /**
     * 根据用户模型文件id列表查询所有用户模型文件url列表
     * @param ids 用户模型文件id列表
     * @return    用户模型文件url列表
     */
    List<String> listImgUrlByIds(@Param("ids") List<String> ids);

    /**
     * 根据任务id和用户id删除任务
     * @param taskId 任务id
     * @param userId 用户id
     */
    void deleteTaskById(@Param("taskId") String taskId, @Param("userId") Long userId);

    /**
     * 根据用户模型文件id列表查询所有用户模型文件信息列表
     * @param loraModelId 用户模型文件id列表
     * @return    用户模型文件信息列表
     */
    List<JSONObject> selectModelTestDataAndTaskInfo(@Param("loraModelId") String loraModelId);

    /**
     * 根据用户模型文件id列表查询所有任务id列表
     * @param ids 用户模型文件id列表
     * @return    任务id列表
     */
    List<String> selectTaskIdsByIds(@Param("ids") List<String> ids);

    /**
     * 查询指定任务的生图列表
     * @param taskId 任务id
     * @return 任务详情
     */
    @Select("SELECT A.id,A.task_id AS taskId,A.file_url AS fileUrl,A.belong_user_id AS belongUserId,A.belong_user_name AS belongUserName,A.model_strength AS modelStrength,B.init_img_list AS initImgList,B.prompt,B.prompt_zh AS promptZh " +
        "FROM sd_user_model_file AS A INNER JOIN sd_user_task AS B ON A.task_id=B.task_id WHERE A.task_id=#{taskId}")
    List<ComfyUserModelFileVo> getComfyImageOutputByTaskId(@Param("taskId") String taskId);

    /**
     * 检查指定任务是否已生成图片
     * @param taskId 任务id
     * @return 是否已生成图片
     */
    @Select("SELECT COUNT(1) FROM sd_user_model_file WHERE task_id=#{taskId} AND file_url IS NOT NULL")
    boolean checkHasImgByTaskId(@Param("taskId") String taskId);
}
