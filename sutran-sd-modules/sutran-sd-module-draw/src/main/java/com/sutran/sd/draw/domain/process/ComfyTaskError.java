package com.sutran.sd.draw.domain.process;

import com.sutran.sd.draw.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 任务失败
 *
 * @author zj
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComfyTaskError implements IComfyTaskProcess {

    /**
     * 自定义任务id
     */
    private String taskId;

    /**
     * comfyUI内部任务id
     */
    private String comfyTaskId;

    /**
     * 错误信息
     */
    private String errorInfo;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
