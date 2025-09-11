package com.sutran.sd.draw.constants;

/**
 * SD API接口路径常量
 * @author zj
 * @date 2024-10-26
 */
public interface DrawApi {

    /** checkpoint大模型列表 **/
    String SD_MODELS_API = "/sdapi/v1/sd-models";
    /** SD设置(可设置模型) **/
    String OPTIONS_API = "/sdapi/v1/options";
    /** 刷新Lora模型 **/
    String REFRESH_LORAS_API = "/sdapi/v1/refresh-loras";
    /** Lora模型列表 **/
    String LORAS_API = "/sdapi/v1/loras";
    /** 文生图 **/
    String TXT_TO_IMG_API = "/sdapi/v1/txt2img";
    /** 图生图 **/
    String IMG_TO_IMG_API = "/sdapi/v1/img2img";

    /**
     * POST:提交任务
     *      body:{“client_id”:"","prompt":""}
     */
    String PROMPT = "/prompt";
    /**
     * POST:上传蒙版图片接口（图片将以二进制格式发送到服务器）
     *      body:{"image":"string(binary)","type":"input","subfolder":"clipspace","original_ref":"{“filename”:”640.png”,”type”:”input”,”subfolder”:”clipspace”}"}
     */
    String UPLOAD_MASK = "/upload/mask";
    /**
     * POST:图片上传（图片将以二进制格式发送到服务器）
     *      body:{"image":“string(binary)”}
     */
    String UPLOAD_IMAGE = "/upload/image";
    /**
     * GET:图片的在线预览接口（上传图像，生图图像，蒙蔽图像，均通过该接口预览）
     *      query: filename、type、subfolder、preview
     */
    String VIEW = "/view";
    /**
     * GET:获取所有详细任务队列信息，正在运行的以及挂起的
     * POST:清除队列，仅能清除队列，执行中的任务无法清除，终止执行中的任务需要/interrupt
     *      删除指定队列：{"delete":["9c0d9ef3-442f-4350-8173-6c13e12caa7c"]}
     *      清除所有队列：{"clear":true}
     */
    String QUEUE = "/queue";
    /**
     * POST:取消当前任务(不需任何参数)
     */
    String INTERRUPT = "/interrupt";
    /**
     * GET:获取历史任务数据(根据任务id获取历史数据)
     */
    String HISTORY = "/history/%s";
    /**
     * GET:获取所有历史任务数据
     */
    String HISTORY_LIST = "/history";
    /**
     * GET:系统统计信息接口
     */
    String SYSTEM_STATS = "/system_stats";
    /**
     * GET:根据组件名称获取系统中组件参数
     */
    String OBJECT_INFO = "/object_info/%s";
}
