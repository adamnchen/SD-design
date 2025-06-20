package com.sutran.sd.sdapi.modules.system.vo;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * SD的基础大模型
 * @author zj
 * @date 2024-03-01
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
@Accessors(chain = true)
@Schema(name = "SdUserModelVo", description = "SD-Lora模型")
public class SdUserModelVo implements Serializable {

    @Schema(name = "id", description = "模型ID")
    private String id;
    /**
     * 模型标题
     */
    @Schema(name = "title", description = "模型标题")
    private String title;
    /**
     * 模型分类ID
     */
    @Schema(name = "classifyId", description = "模型分类ID")
    private String classifyId;
    /**
     * 模型分类ID
     */
    @Schema(name = "classifyName", description = "模型分类名称")
    private String classifyName;
    /**
     * 模型名称
     */
    @Schema(name = "modelName", description = "模型名称")
    private String modelName;
    /**
     * 模型名称
     */
    @Schema(name = "modelNameZh", description = "模型别名")
    private String modelNameZh;
    /**
     * 模型强度
     */
    @Schema(name = "modelStrength", description = "模型强度")
    private String modelStrength;
    /**
     * 模型共性词
     */
    @Schema(name = "additionTag", description = "模型共性词")
    private Object additionTag;
    /**
     * 模型hash值
     */
    @Schema(name = "hash", description = "模型hash值")
    private String hash;
    /**
     * 模型存储位置
     */
    @Schema(name = "fileName", description = "模型存储位置")
    private String fileName;
    /**
     * 模型配置
     */
    @Schema(name = "config", description = "模型配置")
    private Object config;
    /**
     * 模型封面地址
     */
    @Schema(name = "url", description = "模型封面地址")
    private String url;
    /**
     * 是否是被分享的模型
     */
    @Schema(name = "shareModel", description = "是否是被分享的模型")
    private boolean shareModel = false;
    /**
     * 模型描述
     */
    @Schema(name = "remark", description = "模型描述")
    private String remark;
    /**
     * 模型归属类型[0-系统,1-个人]
     */
    @Schema(name = "type", description = "模型归属类型[0-系统,1-个人]")
    private Integer type;
    /**
     * 模型是否公开[0-否,1-是]
     */
    @Schema(name = "isOpen", description = "模型是否公开[0-否,1-是]")
    private Integer isOpen;
    /**
     * 模型发布状态[0-未发布,1-已发布]
     */
    @Schema(name = "publishStatus", description = "模型发布状态[0-未发布,1-已发布]")
    private Integer publishStatus;
    /**
     * 模型归属人ID
     */
    @Schema(name = "belongUserId", description = "模型归属人ID")
    private String belongUserId;
    /**
     * 模型归属人ID
     */
    @Schema(name = "belongUserName", description = "模型归属人名称")
    private String belongUserName;
    /**
     * 模型创建时间
     */
    @Schema(name = "crtTime", description = "模型创建时间")
    private Date crtTime;
    /**
     * 模型创建时间
     */
    @Schema(name = "taskList", description = "模型关联测试任务数据")
    private List<JSONObject> taskList;
    /**
     * 模型训练时的预处理任务ID
     */
    @Schema(name = "preTaskId", description = "模型训练时的预处理任务ID")
    private String preTaskId;
    /**
     * 用户是否已删除该模型[0-否,1-是]
     */
    @Schema(name = "isUserDel", description = "用户是否已删除该模型[0-否,1-是]")
    private Integer isUserDel;

}
