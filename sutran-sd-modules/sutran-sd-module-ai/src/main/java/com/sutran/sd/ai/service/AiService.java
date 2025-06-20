package com.sutran.sd.ai.service;

import com.sutran.sd.ai.dto.AiChatQuestionDto;
import com.sutran.sd.ai.entity.AiMsgHistory;

import java.util.List;

/**
 * @author zj
 * @date 2024-12-16
 */
public interface AiService {
    void chatAi(AiChatQuestionDto questionDto, Long userId, String username);
    void restartChatAi(String sessionId, Long userId);
    List<AiMsgHistory> chatMsgHistory(String sessionId, Long userId, Integer limit, String recentLatestId);
}
