package com.sutran.sd.draw.service;

import com.sutran.sd.draw.domain.SdDrawNode;
import com.sutran.sd.draw.domain.bo.ComfyModelTaskSubmitBo;
import com.sutran.sd.draw.domain.pojo.*;
import com.sutran.sd.draw.domain.vo.ComfyUserModelFileVo;
import com.sutran.sd.draw.domain.vo.ComfyuiImageToolVo;
import com.sutran.sd.draw.domain.vo.ComfyuiProgressVo;
import com.sutran.sd.draw.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 定义ComfyUI接口方法
 * @author zj
 */
public interface SdComfyuiApiService {

    /**
     * 查询固定工作流列表
     * @return 工作流列表
     */
    List<ComfyuiImageToolVo> queryFixedFlowList();

    /**
     * 提交模型生图任务
     * @param modelTaskBo 任务参数
     * @return 任务id
     */
    String submitComfyModelTask(ComfyModelTaskSubmitBo modelTaskBo);

    /**
     * 提交工作流生图任务
     *
     * @param flowId   工作流id
     * @param prompt 描述词(英文)
     * @param promptZh 描述词(中文)
     * @param images 图片列表
     * @throws IOException 文件IO异常
     * @return 任务id
     */
    String submitComfyFlowTask(String flowId, String prompt, String promptZh, MultipartFile[] images) throws IOException;

    /**
     * 查询指定任务的生图列表
     * @param taskId 任务id
     * @return 任务详情
     */
    List<ComfyUserModelFileVo> getComfyImageOutputByTaskId(String taskId);

    /**
     * 获取任务进度
     * @param taskId 任务id
     * @return 任务进度
     */
    Integer getComfyTaskProgress(String taskId);

    /**
     * 获取任务进度V2
     * @param taskId 任务id
     * @return 任务进度
     */
    ComfyuiProgressVo getComfyTaskProgressV2(String taskId);

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
     * @param type 图片存放位置(input | temp | output （默认 input ）)
     * @return 上传后的图片信息
     */
    ComfyUploadImage uploadImage(File file, SdDrawNode node, ImageType type);

    /**
     * api: /upload/image<br>
     * 上传图片到ComfyUI服务器
     *
     * @param bytes 图片字节流
     * @param node 节点信息
     * @param type 图片存放位置(input | temp | output （默认 input ）)
     * @param fileName 文件名称携带后缀
     * @return 上传后的图片信息
     */
    ComfyUploadImage uploadImage(byte[] bytes, SdDrawNode node, String fileName, ImageType type);

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

    /**
     * 自动处理Comfy任务
     * @param nodeId 节点id
     * @param taskId 任务id
     */
    void autoDealComfyTask(String nodeId, String taskId);

}
