package com.sutran.sd.comfyapi.service;

import com.alibaba.fastjson.JSONObject;
import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.domain.pojo.*;

import java.io.File;
import java.util.Map;

/**
 * 定义ComfyUI接口方法
 * @author zj
 */
public interface ComfyApiService {

    /**
     * api: /prompt<br>
     * 提交图片生成任务
     *
     * @param taskId 自定义的任务id
     * @param flow   工作流
     * @param node   节点信息
     * @return ComfyUI内部任务id
     */
    String submitDrawTask(String taskId, JSONObject flow, SdDrawNode node);

    /**
     * api: /prompt<br>
     * 获取服务器当前剩余任务列队的数量
     *
     * @param node 节点信息
     * @return ComfyUI内部任务id
     */
    ComfyTaskQueueRemaining getQueueRemaining(SdDrawNode node);

    /**
     * api: /history<br>
     * 获得全部历史任务信息
     *
     * @param node 节点信息
     * @return 历史任务信息 key为任务id value为任务信息
     */
    Map<String, ComfyTaskHistoryInfo> getTaskInfo(SdDrawNode node);

    /**
     * api: /history/{promptId}<br>
     * 获得某一个任务信息
     *
     * @param promptId comfyUI内部任务id
     * @param node 节点信息
     * @return 历史任务信息
     */
    ComfyTaskHistoryInfo getTaskInfoById(String promptId, SdDrawNode node);

    /**
     * api: /queue
     * 获得当前队列状态
     *
     * @param node 节点信息
     * @return 当前队列状态
     */
    ComfyTaskQueueStatus getQueueStatus(SdDrawNode node);

    /**
     * api: /queue<br>
     * 取消一个绘画任务
     *
     * @param promptId comfyUI内部任务id
     * @param node 节点信息
     */
    void cancelDrawTask(String promptId, SdDrawNode node);

    /**
     * api: /interrupt<br>
     * 取消当前运行的任务
     *
     * @param node 节点信息
     */
    void cancelRunningTask(SdDrawNode node);

    /**
     * api: /system_stats<br>
     * 获取系统运行信息
     *
     * @param node 节点信息
     * @return 系统运行环境 硬件设备
     */
    ComfySystemEnvironment getSystemDeviceInfo(SdDrawNode node);

    /**
     * api: /upload/image<br>
     * 上传图片到ComfyUI服务器
     *
     * @param file 图片对象
     * @param node 节点信息
     * @return 上传后的图片信息
     */
    ComfyTaskImage uploadImage(File file, SdDrawNode node);

    /**
     * api: /view
     * 获取输入的图片文件
     *
     * @param imageName 图片文件名
     * @param node 节点信息
     * @return 图片文件二进制数组
     */
    byte[] getInputImageFile(String imageName, SdDrawNode node);

    /**
     * api: /view
     * 获取生成的图片文件
     *
     * @param imageName 图片文件名
     * @param node 节点信息
     * @return 图片文件二进制数组
     */
    byte[] getOutputImageFile(String imageName, SdDrawNode node);
}
