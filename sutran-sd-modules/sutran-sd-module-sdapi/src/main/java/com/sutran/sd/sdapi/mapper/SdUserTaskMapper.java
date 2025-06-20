package com.sutran.sd.sdapi.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.sdapi.modules.system.entity.SdUserTask;
import com.sutran.sd.sdapi.modules.system.vo.SdUserTaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserTaskMapper extends BaseMapperPlus<SdUserTaskMapper, SdUserTask, SdUserTask> {
    Page<SdUserTaskVo> selectAllList(@Param("page") Page<Object> page, @Param("userId") Long userId, @Param("category") Integer category, @Param("status") Integer status);

    String getDoingTask(@Param("userId") Long userId, @Param("category") Integer category);

    Integer selectStatusByTaskId(@Param("taskId") String taskId, @Param("userId") Long userId);

    void deleteTaskByTaskId(@Param("taskId") String taskId);

    String selectGridUrlByTaskId(@Param("taskId") String taskId);
}
