package com.sutran.sd.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常
 *
 * @author ruoyi
 */
public final class TaskErrorException extends RuntimeException {
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
    public TaskErrorException() {
    }

    public TaskErrorException(String taskId, String message) {
        this.message = message + ", 错误的任务id:" + taskId;
        this.code = 500;
    }

    public TaskErrorException(String message) {
        this.message = message;
        this.code = 500;
    }

    public TaskErrorException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public TaskErrorException(String message, Integer code, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public TaskErrorException setMessage(String message) {
        this.message = message;
        return this;
    }

    public TaskErrorException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

}
