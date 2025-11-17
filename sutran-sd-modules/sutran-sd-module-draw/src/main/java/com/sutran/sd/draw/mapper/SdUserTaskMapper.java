package com.sutran.sd.draw.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.draw.domain.SdUserTask;
import com.sutran.sd.draw.domain.vo.ComfyuiDoingTaskVo;
import com.sutran.sd.draw.domain.vo.SdUserTaskVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserTaskMapper extends BaseMapperPlus<SdUserTaskMapper, SdUserTask, SdUserTask> {

    /**
     * 查询用户任务列表
     * @param page      分页对象
     * @param userId    用户ID
     * @param category  任务类型
     * @param status    任务状态
     * @return          任务列表
     */
    Page<SdUserTaskVo> selectAllList(@Param("page") Page<Object> page, @Param("userId") Long userId, @Param("category") Integer category, @Param("status") Integer status);

    /**
     * 查询用户正在进行的任务
     * @param userId    用户ID
     * @param category  任务类型
     * @return          任务ID
     */
    @Select("SELECT task_id FROM sd_user_task WHERE category=#{category} AND belong_user_id=#{userId} AND status IN (0,1) ORDER BY task_id DESC LIMIT 1")
    String getDoingTask(@Param("userId") Long userId, @Param("category") Integer category);

    /**
     * 查询指定人的指定任务状态
     * @param taskId    任务ID
     * @param userId    用户ID
     * @return          任务状态
     */
    @Select("SELECT status FROM sd_user_task WHERE task_id=#{taskId} AND belong_user_id=#{userId}")
    Integer selectStatusByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") Long userId);

    /**
     * 查询指定任务状态
     * @param taskId    任务ID
     * @return          任务状态
     */
    @Select("SELECT status FROM sd_user_task WHERE task_id=#{taskId}")
    Integer selectStatusByTaskId(String taskId);

    /**
     * 删除任务
     * @param taskId    任务ID
     */
    @Delete("DELETE FROM sd_user_task WHERE task_id=#{taskId} AND status IN (0,1,2,3)")
    void deleteTaskByTaskId(@Param("taskId") String taskId);

    /**
     * 查询任务网格URL
     * @param taskId    任务ID
     * @return          网格URL
     */
    @Select("SELECT file_url FROM sd_user_model_file WHERE task_id=#{taskId} AND category=2 LIMIT 1")
    String selectGridUrlByTaskId(@Param("taskId") String taskId);

    /**
     * 获取任务关联的节点ID
     * @param taskId    任务ID
     * @return          节点ID
     */
    @Select("select node_id from sd_user_task where task_id = #{taskId}")
    String getNodeIdByTaskId(@Param("taskId") String taskId);

    /**
     * 获取任务关联的promptID
     * @param taskId    任务ID
     * @return          promptID
     */
    @Select("select prompt_id from sd_user_task where task_id = #{taskId}")
    String getPromptIdByTaskId(@Param("taskId") String taskId);

    /**
     * 获取任务关联的节点URL
     * @param taskId    任务ID
     * @return          节点URL
     */
    SdUserTaskVo getDrawTaskInfoByTaskId(@Param("taskId") String taskId);

    /**
     * 获取任务关联的节点URL
     * @param promptId    任务ID
     * @return          节点URL
     */
    SdUserTaskVo getDrawTaskInfoByPromptId(@Param("promptId") String promptId);

    /**
     * 根据promptID查询任务ID
     * @param promptId  promptID
     * @return          任务ID
     */
    @Select("select task_id from sd_user_task where prompt_id = #{promptId}")
    String getTaskIdByPromptId(@Param("promptId") String promptId);

    /**
     * 获取当前用户正在进行的任务taskId以及任务类型
     * @param userId    用户ID
     * @return  ComfyuiDoingTaskVo
     */
    @Select("select task_id AS taskId,category from sd_user_task where belong_user_id = #{userId} AND category IN (3,4) AND status IN (0,1) ORDER BY task_id DESC LIMIT 1")
    ComfyuiDoingTaskVo getDoingTaskV2(@Param("userId") Long userId);
}
