package com.sutran.sd.ai.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 对话请求体中的文件信息
 * @author zj
 * @date 2024-12-21
 */
@Data
@Accessors(chain = true)
public class AiChatQuestionFileDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件类型[image-图片，text-文本]
     */
    private String fileType;
    /**
     * 模型文件id
     */
    private String modelFileId;

}
