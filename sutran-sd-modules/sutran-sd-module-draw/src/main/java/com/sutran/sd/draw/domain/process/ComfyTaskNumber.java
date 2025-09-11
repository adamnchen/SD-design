package com.sutran.sd.draw.domain.process;

import com.sutran.sd.draw.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 绘图队列任务个数更新
 *
 * @author zj
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComfyTaskNumber implements IComfyTaskProcess {

    /**
     * 新的任务个数
     */
    private int newTaskNumber;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
