package com.sutran.sd.draw.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.sutran.sd.draw.enums.ComfyWebSocketMessageType;
import com.sutran.sd.draw.handle.ComfyWebSocketMessageHandler;
import com.sutran.sd.draw.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zj
 * @date 2025年09月11日 23:00
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Slf4j
@Service
@RequiredArgsConstructor
public class ComfyWebsocketClient {
    private final ComfyWebSocketMessageHandler messageHandler;
    private final static Map<String, WebSocketClient> NODE_WS_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 连接comfyui的websocket获取进度
     *
     * @param wsUrl 节点信息
     * @param promptId comfyUI内部任务id
     * @param taskId 自定义的任务id
     */
    public void createComfyUiWebSocket(String wsUrl, String promptId, String taskId) {
        try{
            WebSocketClient webSocketClient = NODE_WS_CLIENT_MAP.get(taskId);
            if (!Objects.isNull(webSocketClient)) {
                return;
            }
            // 连接comfyui的websocket
            webSocketClient = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.warn("[ComfUI][开始任务]>>>>>>>>>连接到任务：{}，内部任务ID：{}",taskId,promptId);
                }
                @Override
                public void onMessage(String message) {
                    try {
                        //解析websocket消息
                        JsonNode messageNode = JsonUtils.toJsonNode(message);
                        JsonNode dataNode = messageNode.get("data");
                        JsonNode type = messageNode.get("type");
                        log.info("websocket消息-type:{}，dataNode：{}",type,dataNode);
                        //获取消息类型
                        // status=TASK_NUMBER：系统队列任务数量更新
                        // executing=EXECUTING：当前任务节点更新
                        // progress_state=PROGRESS_STATE：当前运行的耗时节点执行进度更新
                        ComfyWebSocketMessageType msgType = ComfyWebSocketMessageType.fromType(type.asText());
                        if (msgType == ComfyWebSocketMessageType.EXECUTING) {
                            messageHandler.handleMessage(msgType, dataNode);
//                            log.warn("[ComfUI][任务节点执行中]>>>>>>>>>当前节点：{}",dataNode);
                        }
                        else if (msgType == ComfyWebSocketMessageType.MONITOR) {
                            log.info("[ComfUI][系统性能状态更新]>>>>>>>>>{}",dataNode);
                        }
                        else if (msgType == ComfyWebSocketMessageType.TASK_NUMBER || Objects.equals(dataNode.get("prompt_id").asText(), promptId)) {
                            messageHandler.handleMessage(msgType, dataNode);
                        }
                    }
                    catch (Exception e) {
                        if (log != null) {
                            log.error("[ComfUI][任务执行异常]>>>>>>>>>comfyui的websocket异常,异常信息: ", e);
                        }
                    }
                }

                @Override
                public void onError(Exception ex) {
                    log.error("[ComfUI][Websocket连接错误]>>>>>>>>>comfyui的websocket异常,异常信息: {}", ex.getMessage());
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("[ComfUI][结束任务]>>>>>>>>>完成任务：{}",taskId);
                }
            };
            webSocketClient.connect();
            NODE_WS_CLIENT_MAP.put(taskId,webSocketClient);
        }
        catch (Exception e) {
            log.error("连接comfyui的websocket异常,异常信息: ", e);
        }
    }

    /**
     * 关闭comfyui的websocket
     *
     * @param taskId 自定义的任务id
     */
    public void closeComfyUiWebSocket(String taskId) {
        WebSocketClient webSocketClient = NODE_WS_CLIENT_MAP.get(taskId);
        try{
            if (Objects.nonNull(webSocketClient)){
                webSocketClient.close();
            }
        }
        catch (Exception e) {
            log.error("关闭comfyui的websocket异常,异常信息: ", e);
        }
        finally {
            NODE_WS_CLIENT_MAP.remove(taskId);
        }
    }
}
