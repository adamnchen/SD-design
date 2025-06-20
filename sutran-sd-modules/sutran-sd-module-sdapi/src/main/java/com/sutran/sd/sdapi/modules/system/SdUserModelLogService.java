package com.sutran.sd.sdapi.modules.system;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserModelLogService {
    void asyncInsertData(Long userId, String userName, Long modelId, String title, String modelName, String modelStrength);
}
