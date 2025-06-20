package com.sutran.sd.sdapi.mapper;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sutran.sd.common.core.mapper.BaseMapperPlus;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelFile;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelFileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * StableDiffusionAPI接口实现
 * @author zj
 * @date 2024-03-02
 */
@Mapper
public interface SdUserModelFileMapper extends BaseMapperPlus<SdUserModelFileMapper, SdUserModelFile, SdUserModelFile> {
    List<SdUserModelFileVo> selectListByTaskIdsAndUserId(@Param("taskIds") List<String> taskIds, @Param("userId") Long userId);

    Page<SdUserModelFileVo> selectAllList(@Param("page") Page<Object> page, @Param("userId") Long userId, @Param("taskId") String taskId, @Param("category") Integer category, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("keyword") String keyword);

    List<SdUserModelFileVo> selectAllListByTaskId(@Param("taskId") String taskId, @Param("userId") Long userId);

    void batchDelByIds(@Param("ids") List<String> ids, @Param("userId") Long userId);

    SdUserModelFileVo selectFirstUrlByTaskIdAndUserId(@Param("taskId") String taskId, @Param("userId") Long userId);

    void removeUserModelFileByTaskId(@Param("taskId") String taskId);

    List<String> listImgUrlByTaskId(@Param("taskId") String taskId, @Param("userId") Long userId);

    List<String> listImgUrlByIds(@Param("ids") List<String> ids);

    void deleteTaskById(@Param("taskId") String taskId, @Param("userId") Long userId);

    List<JSONObject> selectModelTestDataAndTaskInfo(@Param("loraModelId") String loraModelId);

    List<String> selectTaskIdsByIds(@Param("ids") List<String> ids);
}
