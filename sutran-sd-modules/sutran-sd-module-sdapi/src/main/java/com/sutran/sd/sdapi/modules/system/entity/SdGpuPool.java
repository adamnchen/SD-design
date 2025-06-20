package com.sutran.sd.sdapi.modules.system.entity;

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
@TableName("sd_gpu_pool")
@Accessors(chain = true)
public class SdGpuPool implements Serializable {
    /**
     * 数据ID
     */
    @TableId
    @Schema(name = "id", description = "数据ID")
    private Long id;
    /**
     * GPU卡池IP地址
     */
    @Schema(name = "host", description = "SD服务IP地址")
    private String host;
    /**
     * GPU卡池端口
     */
    @Schema(name = "port", description = "SD服务端口")
    private Integer port;
    /**
     * GPU序号
     */
    @Schema(name = "device_id", description = "GPU序号")
    private Integer deviceId;
    /**
     * 类型[0-SD，1-TRAIN]
     */
    @Schema(name = "type", description = "类型[0-SD，1-TRAIN]")
    private Integer type;
    /**
     * 模型测试图生图grid目录
     */
    @Schema(name = "imgGridDir", description = "模型测试图生图grid目录")
    private String imgGridDir;
    /**
     * 模型测试文生图grid目录
     */
    @Schema(name = "txtGridDir", description = "模型测试文生图grid目录")
    private String txtGridDir;
    /**
     * 是否启用[0-否,1-是]
     */
    @Schema(name = "isEnable", description = "是否启用[0-否,1-是]")
    private Integer isEnable;


}

