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
@TableName("ai_msg_history")
@Accessors(chain = true)
public class AiMsgHistory implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 会话ID
     */
    @Schema(name = "sessionId", description = "会话ID")
    private Long sessionId;
    /**
     * 角色
     */
    @Schema(name = "role", description = "角色[system,user,assistant]")
    private String role;
    /**
     * 类型[0-问题,1-答案]
     */
    @Schema(name = "type", description = "类型[0-系统,1-问题,2-答案]")
    private Integer type;
    /**
     * 内容
     */
    @Schema(name = "content", description = "内容")
    private String content;
    /**
     * 内容
     */
    @Schema(name = "fileUrls", description = "文件地址集合")
    private String fileUrls;
    /**
     * 创建人ID
     */
    @Schema(name = "crtUserId", description = "创建人ID")
    private Long crtUserId;
    /**
     * 创建人名称
     */
    @Schema(name = "crtUserName", description = "创建人名称")
    private String crtUserName;
    /**
     * 创建时间
     */
    @Schema(name = "crtTime", description = "任务创建时间")
    private Date crtTime;

}

