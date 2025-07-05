package com.sutran.sd.common.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
public interface CacheConstants {

    /**
     * 在线用户 redis key
     */
    String ONLINE_TOKEN_KEY = "online_tokens:";

    /**
     * 验证码 redis key
     */
    String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * Sd基础大模型 redis key
     */
    String CHECK_POINT = "CHECK_POINT";

    /**
     * 图片预处理总数redis key
     */
    String PRE_IMG_PROGRESS_TOTAL = "PRE_IMG_PROGRESS:TOTAL:";

    /**
     * 图片预处理已完成数redis key
     */
    String PRE_IMG_PROGRESS_COMPLETE = "PRE_IMG_PROGRESS:COMPLETE:";

    /**
     * 图片预处理进度
     */
    String PRE_IMG_PROCESS = "interrogator_task";

    /**
     * 图片预处理任务队列
     */
    String PRE_IMG_TASK_QUEUE_LIST = "PRE_IMG_TASK_QUEUE_LIST";

    /**
     * 标签词翻译redis key
     */
    String TRAIN_TAG_TRANSLATE_MAP = "TRAIN_TAG_TRANSLATE_MAP:";

    /**
     * 共性词数组redis key
     */
    String TRAIN_ADDITION_LIST = "TRAIN_ADDITION_LIST:";

    /**
     * 预处理任务进度
     */
    String PRE_IMG_PROGRESS_TASK_MAP = "PRE_IMG_PROGRESS_TASK_MAP";

    /**
     * 训练任务进度
     */
    String TRAIN_MODEL_PROGRESS_TASK_MAP = "TRAIN_MODEL_PROGRESS_TASK_MAP";
    /**
     * 训练任务进度
     */
    String TRAIN_PROCESS = "train_task";

    /**
     * 英文翻译成中文
     */
    String TRANSLATE_EN_TO_ZH_MAP = "TRANSLATE_EN_TO_ZH_MAP";

    /**
     * 英文翻译成中文
     */
    String TRANSLATE_ZH_TO_EN_MAP = "TRANSLATE_ZH_TO_EN_MAP";

    /**
     * 绘图任务在队列中存在的时间
     */
    String DRAW_TASK_TIME_IN_QUEUE_MAP = "DRAW_TASK_TIME_IN_QUEUE_MAP";

    /**
     * 训练任务队列排序
     */
    String TRAIN_TASK_QUEUE_LIST = "TRAIN_TASK_QUEUE_LIST";

    /**
     * 训练卡池
     */
    String TRAIN_GPU_POOL = "TRAIN_GPU_POOL";
    String TRAIN_GPU_USAGE_FREQUENCY = "TRAIN_GPU_USAGE_FREQUENCY";
    String TRAIN_GPU_TASK = "TRAIN_GPU_TASK:";
    /**
     * 绘图卡池
     */
    String DRAW_GPU_POOL = "DRAW_GPU_POOL";
    String DRAW_GPU_USAGE_FREQUENCY = "DRAW_GPU_USAGE_FREQUENCY";
    String DRAW_GPU_TASK = "DRAW_GPU_TASK:";

}
