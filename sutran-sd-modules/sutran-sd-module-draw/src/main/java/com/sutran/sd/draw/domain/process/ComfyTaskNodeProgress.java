package com.sutran.sd.draw.domain.process;
import com.sutran.sd.draw.domain.pojo.ComfyWorkFlowNode;
import com.sutran.sd.draw.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 当前执行的节点和节点执行进度
 *
 * @author zj
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComfyTaskNodeProgress implements IComfyTaskProcess {

    /**
     * 自定义任务id
     */
    private String taskId;

    /**
     * comfyUI内部任务id
     */
    private String comfyTaskId;

    /**
     *当前进度
     */
    private int currentProgress;

    /**
     *总进度
     */
    private int maxProgress;

    /**
     *进度百分比
     */
    private int percent;

    /**
     *当前任务节点id 例如 1, 2, 3
     */
    private ComfyWorkFlowNode node;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
