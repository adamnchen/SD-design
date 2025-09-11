package com.sutran.sd.draw.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * @author zj
 * @date 2025年06月22日 10:13
 */
@Data
@Accessors(chain=true)
public class TrainPreImgTaskVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 预处理图片数据集合
     */
    private List<TrianImgDataVo> imgList = new ArrayList<>();
    /**
     * 预处理任务id
     */
    private String preTaskId;
    /**
     * 任务状态[0-预处理队列中,1-预处理中,2-未训练,3-训练队列中,4-训练中,5-训练完成,6-训练失败]
     */
    private Integer newStatus;
    /**
     * 旧任务状态
     */
    @Deprecated
    private Integer status;
    /**
     * 训练任务ID
     */
    private String taskId;
    /**
     * 共性词组
     */
    private Set<String> additionTags = new HashSet<>();
    /**
     * 共性词翻译map
     */
    private Map<String,String> translateTagMap = new HashMap<>();
}
