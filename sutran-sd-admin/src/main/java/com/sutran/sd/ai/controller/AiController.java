package com.sutran.sd.ai.controller;

import cn.hutool.core.util.StrUtil;
import com.sutran.sd.ai.dto.AiChatQuestionDto;
import com.sutran.sd.ai.dto.AiMsgSessionDto;
import com.sutran.sd.ai.entity.AiMsgHistory;
import com.sutran.sd.ai.entity.AiMsgSession;
import com.sutran.sd.ai.enums.SessionType;
import com.sutran.sd.ai.service.AiService;
import com.sutran.sd.ai.service.AiSessionService;
import com.sutran.sd.common.core.controller.BaseController;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.helper.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI-聊天会话
 * @author zj
 * @date 2024-05-28
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final AiService aiService;
    private final AiSessionService aiSessionService;

    /**
     * [AI] 对话(SSE返回对话结果)
     * @param questionDto 问题实体
     */
    @PostMapping("/chat")
    public R<Void> chatAi(@RequestBody AiChatQuestionDto questionDto) {
        aiService.chatAi(questionDto, LoginHelper.getUserId(), LoginHelper.getUsername());
        return R.ok("对话完成!");
    }

    /**
     * [AI] 对话重新生成(最后一条问题的回答)(SSE返回对话结果)
     * @param sessionId 会话ID
     */
    @PostMapping("/chat/restart")
    public R<Void> restartChatAi(@RequestParam String sessionId) {
        aiService.restartChatAi(sessionId, LoginHelper.getUserId());
        return R.ok("对话完成!");
    }

    /**
     * [AI] 对话历史查询
     * @param sessionId 会话ID
     * @param limit 查询历史消息条数(默认最近40条)
     * @param recentLatestId 最近最早消息的id(用于分页查询，例如：查询前20条，最近最早消息的id为100，那么limit=20，recentLatestId=100)
     */
    @GetMapping("/chat/history")
    public R<List<AiMsgHistory>> chatMsgHistory(@RequestParam String sessionId,@RequestParam(required = false) Integer limit,@RequestParam(required = false) String recentLatestId) {
        return R.ok(aiService.chatMsgHistory(sessionId,LoginHelper.getUserId(),limit,recentLatestId));
    }

    /**
     * [AI] 会话管理新增
     * @param aiMsgSession 参数
     * @return 结果
     */
    @PostMapping("/chat/session")
    public R<Void> chatSession(@RequestBody AiMsgSessionDto aiMsgSession) {
        if (StrUtil.isBlankIfStr(aiMsgSession.getName())) {
            return R.fail("会话名称不能为空!");
        }
        if (StrUtil.isBlankIfStr(aiMsgSession.getType())) {
            return R.fail("会话类型不能为空!");
        }
        if (!aiMsgSession.getType().equals(SessionType.TEXT.getCode()) && !aiMsgSession.getType().equals(SessionType.IMAGE.getCode())) {
            return R.fail("无效的会话类型!");
        }
        aiSessionService.createSession(aiMsgSession,LoginHelper.getUserId(),LoginHelper.getUsername());
        return R.ok("新增成功!");
    }

    /**
     * [AI] 会话管理列表
     * @return 结果
     */
    @GetMapping("/chat/session")
    public R<List<AiMsgSession>> getSession() {
        return R.ok(aiSessionService.getSessions(LoginHelper.getUserId()));
    }

    /**
     * [AI] 会话管理修改
     * @param aiMsgSession 参数
     * @return 结果
     */
    @PutMapping("/chat/session")
    public R<Void> updateSession(@RequestBody AiMsgSessionDto aiMsgSession) {
        aiSessionService.modifySession(aiMsgSession.getId(),aiMsgSession.getName(),aiMsgSession.getOrderNum(),LoginHelper.getUserId());
        return R.ok("修改成功!");
    }

    /**
     * [AI] 会话管理删除
     * @param id 会话ID
     * @return 结果
     */
    @DeleteMapping("/chat/session")
    public R<Void> delSession(@RequestParam String id) {
        aiSessionService.deleteSession(id);
        return R.ok("删除成功!");
    }

}
