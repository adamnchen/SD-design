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
@TableName("sd_channel_data")
@Accessors(chain = true)
public class SdChannelData implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 用户ID
     */
    @Schema(name = "userId", description = "用户ID")
    private String userId;
    /**
     * 是否发送成功[0-否,1-是]
     */
    @Schema(name = "isSend", description = "是否发送成功[0-否,1-是]")
    private Integer isSend;
    /**
     * 第三方渠道用户ID
     */
    @Schema(name = "channelUserId", description = "第三方渠道用户ID")
    private String channelUserId;
    /**
     * 请求API地址
     */
    @Schema(name = "apiUrl", description = "请求API地址")
    private String apiUrl;
    /**
     * 请求API签名
     */
    @Schema(name = "apiSign", description = "请求API签名")
    private String apiSign;
    /**
     * 发送图片数组
     */
    @Schema(name = "picUrlList", description = "发送图片数组")
    private String picUrlList;
    /**
     * 发送时间
     */
    @Schema(name = "sendTime", description = "发送时间")
    private Date sendTime;
    /**
     * 重试次数
     */
    @Schema(name = "retryTimes", description = "重试次数")
    private Integer retryTimes;
    /**
     * 请求错误原因
     */
    @Schema(name = "errorMsg", description = "请求错误原因")
    private String errorMsg;
}

