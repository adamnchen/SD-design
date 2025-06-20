package com.sutran.sd.ai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zj
 * @since 2024-03-03
 */
@Data
@TableName("ai_msg_session")
@Accessors(chain = true)
public class AiMsgSession implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 会话名称
     */
    @Schema(name = "name", description = "会话名称")
    private String name;
    /**
     * 角色描述
     */
    @Schema(name = "roleDesc", description = "角色描述")
    private String roleDesc;
    /**
     * 会话类型
     */
    @Schema(name = "type", description = "会话类型[text-文本,image-图片]")
    private String type;
    /**
     * 归属人
     */
    @Schema(name = "crtUserId", description = "归属人")
    private Long crtUserId;
    /**
     * 归属人名称
     */
    @Schema(name = "crtUserName", description = "归属人名称")
    private String crtUserName;
    /**
     * 添加时间
     */
    @Schema(name = "crtTime", description = "添加时间")
    private Date crtTime;
    /**
     * 排序值
     */
    @Schema(name = "orderNum", description = "排序值")
    private Integer orderNum;

}

