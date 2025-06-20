package com.sutran.sd.ai.service;

import com.sutran.sd.ai.dto.AiMsgSessionDto;
import com.sutran.sd.ai.entity.AiMsgSession;

import java.util.List;

/**
 * @author zj
 * @date 2024-12-21
 */
public interface AiSessionService {
    void createSession(AiMsgSessionDto aiMsgSession, Long userId, String userName);
    void modifySession(String id, String name, Integer orderNum, Long userId);
    void deleteSession(String id);
    List<AiMsgSession> getSessions(Long userId);
}
