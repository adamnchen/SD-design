package com.sutran.sd.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.common.sse.SseEmitterManager;
import com.sutran.sd.common.sse.SseMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;

/**
 * AI-SSE控制器
 * @author zj
 * @date 2024-05-28
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterManager sseEmitterManager;

    /**
     * [SSE] 建立 SSE 连接
     * @param sessionId 会话ID
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(value = "sessionId", required = false) String sessionId) {
        String tokenValue = StrUtil.isNotBlank(sessionId)?sessionId: StpUtil.getTokenValue();
        Long userId = LoginHelper.getUserId();
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * [SSE] 关闭 SSE 连接
     * @param sessionId 会话ID
     */
    @SaIgnore
    @GetMapping(value = "/sse/close")
    public R<Void> close(@RequestParam(value = "sessionId", required = false) String sessionId) {
        String tokenValue = StrUtil.isNotBlank(sessionId)?sessionId:StpUtil.getTokenValue();
        Long userId = LoginHelper.getUserId();
        sseEmitterManager.disconnect(userId, tokenValue);
        return R.ok();
    }

    /**
     * [SSE] 向特定用户发送消息
     *
     * @param userId 目标用户的 ID
     * @param msg    要发送的消息内容
     */
    @GetMapping(value = "sse/send")
    public R<Void> send(Long userId, String msg) {
        SseMessageDto dto = new SseMessageDto();
        dto.setUserIds(Collections.singletonList(userId));
        dto.setMessage(msg);
        sseEmitterManager.publishMessage(dto);
        return R.ok();
    }

    /**
     * [SSE] 向所有用户发送消息
     *
     * @param msg 要发送的消息内容
     */
    @GetMapping(value = "sse/sendAll")
    public R<Void> send(String msg) {
        sseEmitterManager.publishAll(msg);
        return R.ok();
    }
}
