package com.sutran.sd.sdapi.modules.system;

import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.sdapi.modules.system.vo.SdUserTaskVo;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserTaskService {
    void insertTask(String taskId, Long userId, String userName, Integer category, int isRedraw);

    void completeTask(String taskId, long consumeTime);

    void doingTask(String taskId, long queueTime);

    void failTask(String taskId, String reason, long queueTime);

    TableDataInfo<SdUserTaskVo> userDrawTaskList(PageQuery pageQuery, Long userId, Integer category, Integer status);

    String getDoingTask(Long userId, Integer category);

    Integer selectStatusByTaskId(String taskId, Long userId);

    void deleteTaskByTaskId(String taskId);

    String selectGridUrlByTaskId(String taskId);
}
