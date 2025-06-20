package com.sutran.sd.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sutran.sd.ai.dto.AiMsgSessionDto;
import com.sutran.sd.ai.entity.AiMsgSession;
import com.sutran.sd.ai.mapper.AiSessionMapper;
import com.sutran.sd.ai.service.AiSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author zj
 * @date 2024-12-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiSessionServiceImpl implements AiSessionService {

    private final AiSessionMapper aiSessionMapper;

    @Override
    public void createSession(AiMsgSessionDto aiMsgSession, Long userId, String userName) {
        aiSessionMapper.insert(new AiMsgSession().setName(aiMsgSession.getName()).setType(aiMsgSession.getType()).setRoleDesc(aiMsgSession.getRoleDesc()).setOrderNum(aiMsgSession.getOrderNum()).setCrtUserId(userId).setCrtUserName(userName).setCrtTime(new Date()).setOrderNum(1));
    }

    @Override
    public void modifySession(String id, String name, Integer orderNum, Long userId) {
        aiSessionMapper.updateSession(id,name,orderNum,userId);
    }

    @Override
    public void deleteSession(String id) {
        aiSessionMapper.deleteById(id);
    }

    @Override
    public List<AiMsgSession> getSessions(Long userId) {
        LambdaQueryWrapper<AiMsgSession> queryWrapper = new LambdaQueryWrapper<AiMsgSession>().eq(AiMsgSession::getCrtUserId, userId).orderByAsc(AiMsgSession::getOrderNum, AiMsgSession::getId);
        return aiSessionMapper.selectList(queryWrapper);
    }
}
