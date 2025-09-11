package com.sutran.sd.draw.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * SD绘图 || 用户任务执行记录(SdUserTask)实体类
 *
 * @author zj
 * @since 2024-03-03
 */
@Data
@TableName("sd_common_config")
@Accessors(chain = true)
public class SdCommonConfig implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * 任务ID
     */
    @Schema(name = "host", description = "SD服务IP地址")
    private Integer preImgMinNum;
    /**
     * 执行状态[0-排队等待中,1-执行中,2-执行成功,3-执行失败]
     */
    @Schema(name = "status", description = "SD服务端口")
    private Integer preImgMaxNum;
    /**
     * 渠道推送API地址
     */
    @Schema(name = "channelSendApiUrl", description = "渠道推送API地址")
    private String channelSendApiUrl;
    /**
     * 渠道推送API签名
     */
    @Schema(name = "channelSendApiSign", description = "渠道推送API签名")
    private String channelSendApiSign;
}

