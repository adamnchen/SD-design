package com.sutran.sd.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常
 *
 * @author ruoyi
 */
public final class TaskTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    @Getter
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 数据
     */
    @Setter
    @Getter
    private Object data;

    /**
     * 错误明细，内部调试错误
     */
    @Getter
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public TaskTimeoutException() {
    }

    public TaskTimeoutException(String taskId, String message) {
        this.message = message + ", 错误的任务id:" + taskId;
        this.code = 500;
    }

    public TaskTimeoutException(String taskId) {
        this.message = "绘图任务超时, 超时的任务id:" + taskId;
        this.code = 500;
    }

    public TaskTimeoutException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public TaskTimeoutException(String message, Integer code, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public TaskTimeoutException setMessage(String message) {
        this.message = message;
        return this;
    }

    public TaskTimeoutException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

}
