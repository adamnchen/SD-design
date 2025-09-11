package com.sutran.sd.draw.domain.process;

import com.sutran.sd.draw.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 任务开始
 *
 * @author zj
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComfyTaskStart implements IComfyTaskProcess {

    /**
     * 自定义任务id
     */
    private String taskId;

    /**
     * comfyUI内部任务id
     */
    private String comfyTaskId;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
