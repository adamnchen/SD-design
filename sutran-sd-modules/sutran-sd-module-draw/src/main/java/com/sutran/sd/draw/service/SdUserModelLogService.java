package com.sutran.sd.draw.service;

/**
 * @author zj
 * @date 2024-03-03
 */
public interface SdUserModelLogService {
    /**
     * 异步插入模型日志
     * @param userId       用户id
     * @param userName     用户名
     * @param modelId      模型id
     * @param title        标题
     * @param modelName    模型名称
     * @param modelStrength 模型强度
     */
    void asyncInsertData(Long userId, String userName, Long modelId, String title, String modelName, String modelStrength);
}
