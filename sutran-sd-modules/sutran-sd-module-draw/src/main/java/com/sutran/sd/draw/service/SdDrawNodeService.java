package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.enums.LoadBalanceStrategy;

import java.util.List;

/**
 * SD绘图 || 工作流(SdFlow)表服务接口
 *
 * @author makejava
 * @since 2025-09-07 23:17:31
 */
public interface SdDrawNodeService {
    /**
     * 新增comfyui节点
     * @param sdDrawNode 节点信息
     */
    void save(SdDrawNode sdDrawNode);
    /**
     * 根据ID更新comfyui节点
     * @param sdDrawNode 节点信息
     */
    void update(SdDrawNode sdDrawNode);
    /**
     * 根据ID删除comfyui节点
     * @param id 节点ID
     */
    void delete(Long id);
    /**
     * 获取所有绘图节点
     * @return 获取所有节点
     */
    List<SdDrawNode> getAllDrawNodeList();
    /**
     * 获取所有训练节点
     * @return 节点列表
     */
    List<SdDrawNode> getAllTrainNodeList();
    /**
     * 根据ID查询comfyui节点
     * @param id 节点ID
     * @return 节点信息
     */
    SdDrawNode findById(Long id);
    /**
     * comfyui节点健康检查
     */
    void drawNodeHealthCheck();
    /**
     * 训练节点健康检查
     */
    void trainNodeHealthCheck();
    /**
     * 获取可用comfyui节点
     * @return 可用节点列表
     */
    List<SdDrawNode> getAvailableDrawNodes();
    /**
     * 获取可用fluxgym节点
     * @return 可用节点列表
     */
    List<SdDrawNode> getAvailableTrainNodes();
    /**
     * 选择comfyui节点 和 锁定节点任务
     * @param strategy 负载均衡策略
     * @param taskId    任务ID
     * @return 选中的节点
     */
    SdDrawNode selectDrawNodeAndLockNodeTask(LoadBalanceStrategy strategy, String taskId);
    /**
     * 选择fluxgym节点 和 锁定节点任务
     * @param taskId    任务ID
     * @return 选中的节点
     */
    SdDrawNode selectTrainNodeAndLockNodeTask(String taskId);
}

