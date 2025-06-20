package com.sutran.sd.ai.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * AI 对话请求体
 * @author zj
 * @date 2024-12-19
 */
@Data
@Accessors(chain = true)
public class AiChatQuestionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 问题
     */
    private String question;
    /**
     * 文件信息集合
     */
    private List<AiChatQuestionFileDto> files;
}
