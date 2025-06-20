package com.sutran.sd.common.sse;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zj
 * @date 2024-11-22
 */
@Data
public class SseMessageDto implements Serializable {

    /**
     * 需要推送到的session key 列表
     */
    private List<Long> userIds;

    /**
     * 需要发送的消息
     */
    private String message;
}
