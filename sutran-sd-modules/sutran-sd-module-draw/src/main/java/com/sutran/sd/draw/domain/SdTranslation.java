package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zj
 * @date 2025年09月13日 23:18
 */
@Data
@TableName("sd_translation")
@Accessors(chain = true)
public class SdTranslation implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 中文
     */
    @Schema(name = "zh", description = "中文")
    private String zh;
    /**
     * 英文
     */
    @Schema(name = "en", description = "英文")
    private String en;
    /**
     * 类型[0-提示词英译中,1-tag标签中译英,2-共性词翻译,3-标签词翻译]
     */
    @Schema(name = "type", description = "类型[0-提示词英译中,1-tag标签中译英,2-共性词,3-标签词英译中]")
    private Integer type;
    /**
     * 训练任务ID
     */
    @Schema(name = "trainTaskId", description = "训练任务ID")
    private Long trainTaskId;

}
