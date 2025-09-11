package com.sutran.sd.draw.domain.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 用于标识ComfyUI WebSocket消息对象
 * @author zj
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface IComfyTaskProcess {
}
