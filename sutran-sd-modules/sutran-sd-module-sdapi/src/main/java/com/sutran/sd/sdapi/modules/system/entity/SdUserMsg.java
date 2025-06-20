package com.sutran.sd.sdapi.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * SD绘图 || 用户任务执行记录(SdUserTask)实体类
 *
 * @author zj
 * @since 2024-03-03
 */
@Data
@TableName("sd_user_msg")
@Accessors(chain = true)
public class SdUserMsg implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private String id;
    /**
     * 任务ID
     */
    @Schema(name = "wxOpenId", description = "微信公众号openId")
    private String wxOpenId;

    @Schema(name = "templateId", description = "微信公众号消息模板ID")
    private String templateId;

    @Schema(name = "title", description = "消息标题")
    private String title;

    @Schema(name = "msgContent", description = "消息内容")
    private String msgContent;

    @Schema(name = "msgBody", description = "消息体")
    private Object msgBody;

    @Schema(name = "crtTime", description = "创建时间")
    private Date crtTime;

    @Schema(name = "userId", description = "被通知人ID")
    private Long userId;

    @Schema(name = "preTaskId", description = "被通知人ID")
    private String preTaskId;

    @Schema(name = "isRead", description = "是否已读")
    private Integer isRead;

    @Schema(name = "readTime", description = "读取时间")
    private Date readTime;


}

