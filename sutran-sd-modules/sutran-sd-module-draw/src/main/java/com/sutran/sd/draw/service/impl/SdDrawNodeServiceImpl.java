package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.domain.pojo.ComfyTaskQueueStatus;
import com.sutran.sd.draw.domain.pojo.FluxGymHealthInfo;
import com.sutran.sd.draw.enums.LoadBalanceStrategy;
import com.sutran.sd.draw.enums.NodeStatus;
import com.sutran.sd.draw.enums.NodeType;
import com.sutran.sd.draw.mapper.SdDrawNodeMapper;
import com.sutran.sd.draw.service.SdDrawNodeService;
import com.sutran.sd.draw.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.sutran.sd.common.constant.CacheConstants.DRAW_NODE_TASK_MAP;
import static com.sutran.sd.common.constant.CacheConstants.TRAIN_NODE_TASK_MAP;

/**
 * SD绘图 || 工作流(SdFlow)表服务实现类
 *
 * @author makejava
 * @since 2025-09-07 23:17:31
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class SdDrawNodeServiceImpl implements SdDrawNodeService {

    private final SdDrawNodeMapper sdDrawNodeMapper;

    private static final Map<String, SdDrawNode> DRAW_NODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, SdDrawNode> TRAIN_NODE_CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 初始化节点
     */
    @PostConstruct
    public void init() {
        loadAllNodes();
        this.drawNodeHealthCheck();
        this.trainNodeHealthCheck();
    }
    public void loadAllNodes() {
        List<SdDrawNode> nodes = getActiveNodeList();
        if (CollectionUtil.isEmpty(nodes)) {
            return;
        }
        DRAW_NODE_CACHE.clear();
        TRAIN_NODE_CACHE.clear();
        for (SdDrawNode node : nodes) {
            node.setCurrentTasks(0);
            node.setStatus(NodeStatus.OFFLINE.name());
            node.setQueueSize(0);
            if (node.getNodeType().equals(NodeType.DRAW)) {
                DRAW_NODE_CACHE.put(node.getId().toString(), node);
            } else {
                TRAIN_NODE_CACHE.put(node.getId().toString(), node);
            }
        }
    }

    /**
     * 新增节点
     * @param node 节点信息
     */
    @Override
    public void save(SdDrawNode node) {
        sdDrawNodeMapper.insert(node);
        node.setCurrentTasks(0).setStatus(NodeStatus.OFFLINE.name()).setQueueSize(0);
        if (node.getNodeType().equals(NodeType.DRAW)) {
            DRAW_NODE_CACHE.put(node.getId().toString(), node);
        }
        else {
            TRAIN_NODE_CACHE.put(node.getId().toString(), node);
        }
    }

    /**
     * 根据ID更新节点
     * @param sdDrawNode 节点信息
     */
    @Override
    public void update(SdDrawNode sdDrawNode) {
        sdDrawNodeMapper.updateById(sdDrawNode);
        if (sdDrawNode.getNodeType().equals(NodeType.DRAW)) {
            SdDrawNode node = DRAW_NODE_CACHE.get(sdDrawNode.getId().toString());
            if (node == null) {
                DRAW_NODE_CACHE.put(sdDrawNode.getId().toString(), sdDrawNode);
            }
            else {
                node.setName(sdDrawNode.getName())
                    .setCode(sdDrawNode.getCode())
                    .setRegion(sdDrawNode.getRegion())
                    .setBaseUrl(sdDrawNode.getBaseUrl())
                    .setWeight(sdDrawNode.getWeight())
                    .setMaxConcurrentTasks(sdDrawNode.getMaxConcurrentTasks());
                DRAW_NODE_CACHE.put(sdDrawNode.getId().toString(), node);
            }
        }
        else {
            SdDrawNode node = TRAIN_NODE_CACHE.get(sdDrawNode.getId().toString());
            if (node == null) {
                TRAIN_NODE_CACHE.put(sdDrawNode.getId().toString(), sdDrawNode);
            }
            else {
                node.setName(sdDrawNode.getName())
                    .setCode(sdDrawNode.getCode())
                    .setRegion(sdDrawNode.getRegion())
                    .setBaseUrl(sdDrawNode.getBaseUrl())
                    .setWeight(sdDrawNode.getWeight())
                    .setMaxConcurrentTasks(sdDrawNode.getMaxConcurrentTasks());
                TRAIN_NODE_CACHE.put(sdDrawNode.getId().toString(), node);
            }
        }
    }

    /**
     * 根据ID删除节点
     * @param id 节点ID
     */
    @Override
    public void delete(Long id) {
        sdDrawNodeMapper.deleteById(id);
        DRAW_NODE_CACHE.remove(id.toString());
        TRAIN_NODE_CACHE.remove(id.toString());
    }

    /**
     * 获取所有绘图节点
     * @return 节点列表
     */
    @Override
    public List<SdDrawNode> getAllDrawNodeList() {
        return new ArrayList<>(DRAW_NODE_CACHE.values());
    }

    /**
     * 获取所有训练节点
     * @return 节点列表
     */
    @Override
    public List<SdDrawNode> getAllTrainNodeList() {
        return new ArrayList<>(TRAIN_NODE_CACHE.values());
    }

    /**
     * 根据ID查询节点
     * @param id 节点ID
     * @return 节点信息
     */
    @Override
    public SdDrawNode findById(Long id) {
        return sdDrawNodeMapper.selectById(id);
    }

    /**
     * comfyui节点健康检查
     */
    @Override
    public void drawNodeHealthCheck() {
        if (CollectionUtil.isEmpty(DRAW_NODE_CACHE)) {
            return;
        }
        DRAW_NODE_CACHE.values().forEach(this::checkDrawNodeHealth);
    }

    /**
     * fluxgym节点健康检查
     */
    @Override
    public void trainNodeHealthCheck() {
        if (CollectionUtil.isEmpty(TRAIN_NODE_CACHE)) {
            return;
        }
        TRAIN_NODE_CACHE.values().forEach(this::checkTrainNodeHealth);
    }

    /** 检查comfyui节点健康状态 **/
    private void checkDrawNodeHealth(SdDrawNode drawNode) {
        try {
            ComfyTaskQueueStatus info = getQueueStatus(drawNode.getBaseUrl());
            if (info!=null) {
                int currentTasks = info.getRunning().size();
                int queueSize = info.getPending().size();
                // 更新节点状态
                Optional.ofNullable(DRAW_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                    node.setCurrentTasks(currentTasks);
                    node.setQueueSize(queueSize);
                    node.setLastHealthCheck(new Date());
                    node.setStatus(NodeStatus.ONLINE.name());
                    DRAW_NODE_CACHE.put(drawNode.getId().toString(), node);
                });
            }
            else {
                Optional.ofNullable(DRAW_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                    node.setStatus(NodeStatus.OFFLINE.name());
                    node.setLastHealthCheck(new Date());
                    DRAW_NODE_CACHE.put(node.getId().toString(), node);
                });
            }
        }
        catch (Exception e) {
            log.warn("绘图节点 {} 健康检查失败: {}", drawNode.getId(), e.getMessage());
            Optional.ofNullable(DRAW_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                node.setStatus(NodeStatus.OFFLINE.name());
                node.setLastHealthCheck(new Date());
                DRAW_NODE_CACHE.put(node.getId().toString(), node);
            });
        }
    }
    /** 检查comfyui节点健康状态 **/
    public ComfyTaskQueueStatus getQueueStatus(String url) {
        HttpRequest request = HttpRequest.get(url + "/queue").timeout(3000);
        String queueStatusInfo = execHttpRequest(request);
        return JsonUtils.toObject(queueStatusInfo, ComfyTaskQueueStatus.class);
    }

    /** 检查fluxgym节点健康状态 **/
    private void checkTrainNodeHealth(SdDrawNode drawNode) {
        try {
            FluxGymHealthInfo info = getHealthStatus(drawNode.getBaseUrl());
            if (info!=null) {
                // 更新节点状态
                Optional.ofNullable(TRAIN_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                    node.setLastHealthCheck(new Date());
                    node.setStatus(NodeStatus.ONLINE.name());
                    TRAIN_NODE_CACHE.put(drawNode.getId().toString(), node);
                });
            }
            else {
                Optional.ofNullable(TRAIN_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                    node.setStatus(NodeStatus.OFFLINE.name());
                    node.setLastHealthCheck(new Date());
                    TRAIN_NODE_CACHE.put(drawNode.getId().toString(), node);
                });
            }
        }
        catch (Exception e) {
            log.warn("训练节点 {} 健康检查失败: {}", drawNode.getId(), e.getMessage());
            Optional.ofNullable(TRAIN_NODE_CACHE.get(drawNode.getId().toString())).ifPresent(node -> {
                node.setStatus(NodeStatus.OFFLINE.name());
                node.setLastHealthCheck(new Date());
                TRAIN_NODE_CACHE.put(drawNode.getId().toString(), node);
            });
        }
    }
    /** 检查fluxgym节点健康状态 **/
    public FluxGymHealthInfo getHealthStatus(String url) {
        HttpRequest request = HttpRequest.get(url + "/api/health").timeout(30000);
        String resp = execHttpRequest(request);
        return JsonUtils.toObject(resp, FluxGymHealthInfo.class);
    }

    /** 执行request 并自动关闭response **/
    private String execHttpRequest(HttpRequest request) {
        try (HttpResponse response = request.execute()) {
            return response.body();
        }
    }


    /**
     * 获取可用comfyui节点
     * @return 可用节点列表
     */
    @Override
    public List<SdDrawNode> getAvailableDrawNodes() {
        return DRAW_NODE_CACHE.values().stream().filter(e->e.isAvailable() && !RedisUtils.hasCacheMapKey(DRAW_NODE_TASK_MAP,e.getId().toString())).collect(Collectors.toList());
    }

    /**
     * 获取可用comfyui节点
     * @return 可用节点列表
     */
    @Override
    public List<SdDrawNode> getAvailableTrainNodes() {
        return TRAIN_NODE_CACHE.values().stream().filter(e->e.isAvailable() && !RedisUtils.hasCacheMapKey(TRAIN_NODE_TASK_MAP,e.getId().toString())).collect(Collectors.toList());
    }


    /**
     * 选择comfyui节点
     * @param strategy 负载均衡策略
     * @param taskId    任务ID
     * @return 选中的节点
     */
    @Override
    public SdDrawNode selectDrawNodeAndLockNodeTask(LoadBalanceStrategy strategy, String taskId) {
        lock.lock();
        try{
            // 默认：加权最少连接
            strategy = strategy==null?LoadBalanceStrategy.WEIGHTED_LEAST_LOAD:strategy;
            List<SdDrawNode> availableNodes = getAvailableDrawNodes();
            if (availableNodes.isEmpty()) {
                return null;
            }
            switch (strategy) {
                case ROUND_ROBIN:
                    return roundRobin(availableNodes,taskId);
                case WEIGHTED_ROUND_ROBIN:
                    return weightedRoundRobin(availableNodes,taskId);
                case LEAST_CONNECTIONS:
                    return leastConnections(availableNodes,taskId);
                case WEIGHTED_LEAST_LOAD:
                default:
                    return weightedLeastLoad(availableNodes,taskId);
            }
        }
        finally{
            lock.unlock();
        }
    }
    /** 轮询选择节点 **/
    private SdDrawNode roundRobin(List<SdDrawNode> nodes, String taskId) {
        int index = ROUND_ROBIN_INDEX.getAndUpdate(i -> (i + 1) % nodes.size());
        // 确保索引在有效范围内
        if (index >= nodes.size()) {
            index = index % nodes.size();
            // 重置原子索引
            ROUND_ROBIN_INDEX.set(index);
        }
        SdDrawNode sdDrawNode = nodes.get(index);
        if (sdDrawNode !=null) {
            RedisUtils.setCacheMapValue(DRAW_NODE_TASK_MAP, sdDrawNode.getId().toString(),taskId);
        }
        return sdDrawNode;
    }
    /** 加权轮询选择节点 **/
    private SdDrawNode weightedRoundRobin(List<SdDrawNode> nodes, String taskId) {
        int totalWeight = nodes.stream().mapToInt(SdDrawNode::getWeight).sum();
        int random = new Random().nextInt(totalWeight);
        int current = 0;

        for (SdDrawNode node : nodes) {
            current += node.getWeight();
            if (random < current) {
                return node;
            }
        }
        SdDrawNode sdDrawNode = nodes.get(0);
        if (sdDrawNode !=null) {
            RedisUtils.setCacheMapValue(DRAW_NODE_TASK_MAP, sdDrawNode.getId().toString(),taskId);
        }
        return sdDrawNode;
    }
    /** 最少连接选择节点 **/
    private SdDrawNode leastConnections(List<SdDrawNode> nodes, String taskId) {
        SdDrawNode sdDrawNode = nodes.stream()
            .min(Comparator.comparingInt(SdDrawNode::getCurrentTasks))
            .orElse(null);
        if (sdDrawNode !=null) {
            RedisUtils.setCacheMapValue(DRAW_NODE_TASK_MAP, sdDrawNode.getId().toString(),taskId);
        }
        return sdDrawNode;
    }
    /** 加权最少负载选择节点 **/
    private SdDrawNode weightedLeastLoad(List<SdDrawNode> nodes, String taskId) {
        SdDrawNode sdDrawNode = nodes.stream()
            .min(Comparator.comparingDouble(SdDrawNode::getLoadScore))
            .orElse(null);
        if (sdDrawNode !=null) {
            RedisUtils.setCacheMapValue(DRAW_NODE_TASK_MAP, sdDrawNode.getId().toString(),taskId);
        }
        return sdDrawNode;
    }


     /**
     * 选择fluxgym节点
     * @param taskId    任务ID
     * @return 选中的节点
     */
    @Override
    public SdDrawNode selectTrainNodeAndLockNodeTask(String taskId) {
        lock.lock();
        try{
            List<SdDrawNode> availableNodes = getAvailableTrainNodes();
            if (availableNodes.isEmpty()) {
                return null;
            }
            int index = ROUND_ROBIN_INDEX.getAndUpdate(i -> (i + 1) % availableNodes.size());
            // 确保索引在有效范围内
            if (index >= availableNodes.size()) {
                index = index % availableNodes.size();
                // 重置原子索引
                ROUND_ROBIN_INDEX.set(index);
            }
            SdDrawNode sdDrawNode = availableNodes.get(index);
            RedisUtils.setCacheMapValue(TRAIN_NODE_TASK_MAP, sdDrawNode.getId().toString(),taskId);
            return sdDrawNode;
        }
        finally{
            lock.unlock();
        }
    }


    /** 获取所有已激活的comfyui节点 **/
    private List<SdDrawNode> getActiveNodeList() {
        LambdaQueryWrapper<SdDrawNode> lwq = buildQueryWrapper(new SdDrawNode().setIsActive(1));
        return sdDrawNodeMapper.selectList(lwq);
    }
    /** 查询条件处理 **/
    private LambdaQueryWrapper<SdDrawNode> buildQueryWrapper(SdDrawNode bo) {
        LambdaQueryWrapper<SdDrawNode> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SdDrawNode::getName, bo.getName());
        lqw.eq(bo.getIsActive()!=null, SdDrawNode::getIsActive, bo.getIsActive());
        lqw.eq(StringUtils.isNotBlank(bo.getCode()), SdDrawNode::getCode, bo.getCode());
        lqw.eq(bo.getType()!=null, SdDrawNode::getType, bo.getType());
        return lqw;
    }
}

