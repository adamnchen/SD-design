package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 参数配置表 sys_config
 *
 * @author Lion Li
 */

@Data
@TableName("sd_user_model_log")
@Accessors(chain = true)
public class SdUserModelLog implements Serializable {

    @TableId(value = "id")
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 用户ID
     */
    @Schema(name = "userId", description = "用户ID")
    private Long userId;
    /**
     * 用户姓名
     */
    @Schema(name = "userName", description = "用户姓名")
    private String userName;
    /**
     * Lora模型ID
     */
    @Schema(name = "modelName", description = "基础大模型")
    private String modelName;
    /**
     * Lora模型ID
     */
    @Schema(name = "modelId", description = "Lora模型ID")
    private Long loraModelId;
    /**
     * lora模型标题
     */
    @Schema(name = "loraTitle", description = "模型标题")
    private String loraTitle;
    /**
     * 模型强度
     */
    @Schema(name = "modelStrength", description = "模型强度")
    private String modelStrength;
    @Schema(name = "useTimes", description = "使用次数")
    private String useTimes;
    /**
     * 模型创建时间
     */
    @Schema(name = "crtTime", description = "创建时间")
    private Date crtTime;

}
