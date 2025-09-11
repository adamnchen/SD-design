package com.sutran.sd.draw.domain.process;

import com.sutran.sd.draw.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 任务进度预览图
 *
 * @author zj
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComfyTaskProgressPreview implements IComfyTaskProcess {

    /**
     * 自定义任务id
     */
    private String taskId;

    /**
     * comfyUI内部任务id
     */
    private String comfyTaskId;

    /**
     * 图片二进制数组
     */
    private byte[] data;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
