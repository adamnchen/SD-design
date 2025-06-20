package com.sutran.sd.ai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI对话 || 对话模型对象 chat_model
 *
 * @author Lion Li
 * @date 2025-01-11
 */
@Data
@TableName("ai_chat_model")
public class AiChatModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型Key
     */
    private String apiKey;

    /**
     * 模型地址
     */
    private String baseUrl;

    /**
     * 请求超时时间(秒)
     */
    private Long timeOut;

    /**
     * 最大历史消息数量
     */
    private Long maxMessages;

    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;
    private Long createDept;

    private String remark;

}
