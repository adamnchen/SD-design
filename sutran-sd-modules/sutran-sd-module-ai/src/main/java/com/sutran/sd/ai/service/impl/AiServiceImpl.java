package com.sutran.sd.ai.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dtflys.forest.Forest;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.file.FileUtils;
import com.sutran.sd.common.utils.redis.RedisUtils;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.factory.OssFactory;
import com.sutran.sd.ai.utils.QwenReplyUtil;
import com.sutran.sd.ai.dto.AiChatQuestionDto;
import com.sutran.sd.ai.dto.AiChatQuestionFileDto;
import com.sutran.sd.ai.entity.AiMsgHistory;
import com.sutran.sd.ai.entity.AiMsgSession;
import com.sutran.sd.ai.enums.QwenModelType;
import com.sutran.sd.ai.enums.SessionType;
import com.sutran.sd.ai.mapper.AiMapper;
import com.sutran.sd.ai.mapper.AiSessionMapper;
import com.sutran.sd.ai.service.AiService;
import com.sutran.sd.common.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.sutran.sd.ai.constants.CommonKey.ALI_QW_CHAT;

/**
 * @author zj
 * @date 2024-12-16
 */
@SuppressWarnings("LoggingSimilarMessage")
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final SseEmitterManager sseEmitterManager;
    private final AiMapper aiMapper;
    private final AiSessionMapper aiSessionMapper;

    /**
     * prompt 用户对话
     * request 用户请求对象
     * identity 用户身份标识
     */
    @Override
    public void chatAi(AiChatQuestionDto questionDto, Long userId, String username) {
        final String sessionId = questionDto.getSessionId();
        if (StrUtil.isBlankIfStr(sessionId)) {
            throw new ServiceException("会话不能为空!");
        }
        else if (StrUtil.isBlankIfStr(questionDto.getQuestion())) {
            throw new ServiceException("问题不能为空!");
        }
        Long time = RedisUtils.getCacheObject(ALI_QW_CHAT + sessionId + ":" + userId);
        if (time != null) {
            throw new ServiceException("当前用户正在进行对话，请等待本轮对话完成!");
        }
        AiMsgSession aiMsgSession = aiSessionMapper.selectById(Long.parseLong(sessionId));
        if (aiMsgSession == null) {
            throw new ServiceException("会话不存在或已被删除，请重新创建!");
        }

        // 设置单个会话单人一次只能进行一次对话，2分钟有效时间
        RedisUtils.setCacheObject(ALI_QW_CHAT + sessionId + ":" + userId, DateUtil.currentSeconds(), Duration.ofMinutes(2));

        // 通过身份标识在缓存中获取历史消息(仅获取最近20轮=40条历史数据)
        List<AiMsgHistory> historyList = aiMapper.selectList(new LambdaQueryWrapper<AiMsgHistory>().eq(AiMsgHistory::getCrtUserId, userId).eq(AiMsgHistory::getSessionId, Long.parseLong(sessionId)).orderByDesc(AiMsgHistory::getCrtTime).last("limit 10"));
        if (CollectionUtil.isNotEmpty(historyList)) {
            // 将historyList按照crtTime进行升序排序
            historyList.sort(Comparator.comparing(AiMsgHistory::getCrtTime));
        }

        // 判断会话类型
        if (aiMsgSession.getType().equals(SessionType.TEXT.getCode())) {
            // 调用通义千问 qwen-long
            textSession(aiMsgSession, questionDto, userId, username, historyList);
        }
        else if (aiMsgSession.getType().equals(SessionType.IMAGE.getCode())) {
            // 调用通义千问 qwen-vl-max
            imageSession(aiMsgSession, questionDto, userId, username, historyList);
        }
    }

    /**
     * 重新生成对话
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    @Override
    public void restartChatAi(String sessionId, Long userId) {
        if (StrUtil.isBlankIfStr(sessionId)) {
            throw new ServiceException("会话不能为空!");
        }
        Long time = RedisUtils.getCacheObject(ALI_QW_CHAT + sessionId + ":" + userId);
        if (time != null) {
            throw new ServiceException("当前用户正在进行对话，请等待本轮对话完成!");
        }
        AiMsgSession aiMsgSession = aiSessionMapper.selectById(Long.parseLong(sessionId));
        if (aiMsgSession == null) {
            throw new ServiceException("会话不存在或已被删除，请重新创建!");
        }

        // 设置单个会话单人一次只能进行一次对话，2分钟有效时间
        RedisUtils.setCacheObject(ALI_QW_CHAT + sessionId + ":" + userId, DateUtil.currentSeconds(), Duration.ofMinutes(2));

        try{
            // 获取最后一次对话的问题和答案
            List<AiMsgHistory> lastMsgList = aiMapper.selectList(new LambdaQueryWrapper<AiMsgHistory>().eq(AiMsgHistory::getSessionId, Long.parseLong(sessionId)).orderByDesc(AiMsgHistory::getId).last("limit 2"));
            // 校验最后一次对话数据是否是答案和问题
            if (CollectionUtil.isEmpty(lastMsgList)) {
                RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
                throw new ServiceException("当前会话无历史对话记录，重新生成失败!");
            }
            else if (lastMsgList.size()==1 && !lastMsgList.get(0).getType().equals(1)) {
                RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
                throw new ServiceException("当前会话无历史对话记录，重新生成失败!");
            }
            else if (lastMsgList.size()==2 && !lastMsgList.get(1).getType().equals(1)) {
                RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
                throw new ServiceException("当前会话无历史对话记录，重新生成失败!");
            }
            // 删除之前的数据
            aiMapper.delete(new LambdaQueryWrapper<AiMsgHistory>().in(AiMsgHistory::getId, lastMsgList.stream().map(AiMsgHistory::getId).collect(Collectors.toList())));

            // 拼接重新生成的请求参数
            String question = lastMsgList.size()==1?lastMsgList.get(0).getContent():lastMsgList.get(1).getContent();
            String username = lastMsgList.size()==1?lastMsgList.get(0).getCrtUserName():lastMsgList.get(1).getCrtUserName();
            String fileUrl = lastMsgList.size()==1?lastMsgList.get(0).getFileUrls():lastMsgList.get(1).getFileUrls();
            List<AiChatQuestionFileDto> files = StrUtil.isBlankIfStr(fileUrl) ? Collections.emptyList() : JSON.parseArray(fileUrl, AiChatQuestionFileDto.class);
            AiChatQuestionDto questionDto = new AiChatQuestionDto().setQuestion(question).setSessionId(sessionId).setFiles(files);

            // 通过身份标识在缓存中获取历史消息(仅获取最近5轮=10条历史数据)
            List<AiMsgHistory> historyList = aiMapper.selectList(new LambdaQueryWrapper<AiMsgHistory>().eq(AiMsgHistory::getCrtUserId, userId).eq(AiMsgHistory::getSessionId, Long.parseLong(sessionId)).orderByDesc(AiMsgHistory::getCrtTime).last("limit 10"));
            if (CollectionUtil.isNotEmpty(historyList)) {
                // 将historyList按照crtTime进行升序排序
                historyList.sort(Comparator.comparing(AiMsgHistory::getCrtTime));
            }

            // 判断会话类型
            if (aiMsgSession.getType().equals(SessionType.TEXT.getCode())) {
                // 调用通义千问 qwen-long
                textSession(aiMsgSession, questionDto, userId, username, historyList);
            }
            else if (aiMsgSession.getType().equals(SessionType.IMAGE.getCode())) {
                // 调用通义千问 qwen-vl-max
                imageSession(aiMsgSession, questionDto, userId, username, historyList);
            }
        }
        catch (Exception e) {
            RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
            throw new ServiceException(e.getMessage());
        }
    }

    /** 文本图片对话 **/
    private void imageSession(AiMsgSession aiMsgSession, AiChatQuestionDto questionDto, Long userId, String username, List<AiMsgHistory> historyList) {
        final String sessionId = questionDto.getSessionId();
        MultiModalConversation conv = new MultiModalConversation();

        // 需要新增的消息集合
        List<AiMsgHistory> entityList = new ArrayList<>();
        // 历史对话消息集合
        List<MultiModalMessage> messages = new ArrayList<>();

        // 存在历史消息
        if (CollectionUtil.isNotEmpty(historyList)){
            for (AiMsgHistory history : historyList) {
                List<Map<String,Object>> contentList = new ArrayList<>();
                // 处理携带图片的数据
                if (StrUtil.isNotBlank(history.getFileUrls())) {
                    OssClient storage = getOssStorage(sessionId,userId);
                    List<AiChatQuestionFileDto> fileUrlList = JSON.parseArray(history.getFileUrls(), AiChatQuestionFileDto.class);
                    for (AiChatQuestionFileDto fileDto : fileUrlList) {
                        if (!fileDto.getFileType().equals(SessionType.IMAGE.getCode()) || StrUtil.isBlankIfStr(fileDto.getFileUrl())) {
                            continue;
                        }
                        contentList.add(Collections.singletonMap("image", fileDto.getFileUrl()));
                    }
                }
                contentList.add(Collections.singletonMap("text",history.getContent()));
                messages.add(MultiModalMessage.builder().role(history.getRole()).content(contentList).build());
            }
        }
        // 第一次提问
        else {
            String roleDesc = StrUtil.isBlankIfStr(aiMsgSession.getRoleDesc())?"请问有什么可以帮到您？":aiMsgSession.getRoleDesc();
            MultiModalMessage systemMsg = MultiModalMessage.builder().role(Role.SYSTEM.getValue()).content(Collections.singletonList(Collections.singletonMap("text", roleDesc))).build();
            messages.add(systemMsg);
            AiMsgHistory firstMessage = new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.SYSTEM.getValue()).setType(0).setContent(roleDesc).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
            entityList.add(firstMessage);
        }
        // 新增的消息
        AiMsgHistory newMsg = new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.USER.getValue()).setType(1).setContent(questionDto.getQuestion()).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
        MultiModalMessage newMessage = MultiModalMessage.builder().role(Role.USER.getValue()).build();
        List<Map<String,Object>> contentList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(questionDto.getFiles())) {
            for (AiChatQuestionFileDto fileDto : questionDto.getFiles()) {
                if (!fileDto.getFileType().equals(SessionType.IMAGE.getCode()) || StrUtil.isBlankIfStr(fileDto.getFileUrl())) {
                    continue;
                }
                contentList.add(Collections.singletonMap("image", fileDto.getFileUrl()));
            }
            newMsg.setFileUrls(JSONObject.toJSONString(questionDto.getFiles()));
        }
        contentList.add(Collections.singletonMap("text",newMsg.getContent()));
        newMessage.setContent(contentList);
        entityList.add(newMsg);
        messages.add(newMessage);

        // 批量插入
        aiMapper.insertBatch(entityList);

        // 构建通义千问参数对象
        try {
            MultiModalConversationParam param = QwenReplyUtil.createMultiModalConversationParam(messages, QwenModelType.QWEN_VL_MAX.getName());
            // 同步信号量
            Semaphore semaphore = new Semaphore(0);
            // 结果拼接对象
            StringBuilder resultBuilder = new StringBuilder();
            // 流式调用
            conv.streamCall(param, new ResultCallback<MultiModalConversationResult>(){
                @Override
                public void onEvent(MultiModalConversationResult multiModalConversationResult) {
                    Object newMessage = multiModalConversationResult.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
                    resultBuilder.append(newMessage);
                    // 通过sse返回消息片段给前端
                    sseEmitterManager.sendMessage(userId,sessionId,String.valueOf(newMessage));
                }
                @Override
                public void onComplete() {
                   semaphore.release();
                    String resString = resultBuilder.toString();
                    sseEmitterManager.sendMessage(userId,sessionId,StrUtil.isBlankIfStr(resString)?"当前问题暂时无法回答您!":resString);
                }
                @Override
                public void onError(Exception e) {
                    semaphore.release();
                    log.error("通义千问运行出错, 报错栈如下");
                    Throwable t = e;
                    while (t != null) {
                        log.error( t.toString());
                        t = e.getCause();
                    }
                }
            });
            semaphore.acquire();

            String resString = resultBuilder.toString();
            // 把返回消息加入历史消息中
            aiMapper.insert(new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.ASSISTANT.getValue()).setType(2).setContent(StrUtil.isBlankIfStr(resString)?"当前问题暂时无法回答您!":resString).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date()));
        }
        catch (NoApiKeyException e) {
            log.error("调用通义千问缺少ApiKey");
            throw new ServiceException("没有ApiKey");
        }
        catch (Exception e) {
            log.error("调用通义千问出现问题：",e);
            throw new ServiceException("出现了一些问题:"+e.getMessage());
        }
        finally {
            RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
        }
    }

    /** 文本文件对话 **/
    private void textSession(AiMsgSession aiMsgSession, AiChatQuestionDto questionDto, Long userId, String username, List<AiMsgHistory> historyList) {
        final String sessionId = questionDto.getSessionId();
        // 需要新增的消息集合
        List<AiMsgHistory> entityList = new ArrayList<>();
        // 模型消息管理器
        List<Message> messageManage = new ArrayList<>();

        // 存在历史消息
        if (CollectionUtil.isNotEmpty(historyList)){
            for (AiMsgHistory history : historyList) {
                // 存在文档
                if (StrUtil.isNotBlank(history.getFileUrls())) {
                    for (AiChatQuestionFileDto fileDto : JSON.parseArray(history.getFileUrls(), AiChatQuestionFileDto.class)) {
                        if (fileDto.getFileType().equals(SessionType.TEXT.getCode()) && StrUtil.isNotBlank(fileDto.getModelFileId())){
                            messageManage.add(Message.builder().role(Role.SYSTEM.getValue()).content("fileid://"+fileDto.getModelFileId()).build());
                        }
                    }
                }
                messageManage.add(Message.builder().role(history.getRole()).content(history.getContent()).build());
            }
        }
        // 第一次提问
        else {
            String roleDesc = StrUtil.isBlankIfStr(aiMsgSession.getRoleDesc())?"请问有什么可以帮到您？":aiMsgSession.getRoleDesc();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(roleDesc).build();
            messageManage.add(systemMsg);
            AiMsgHistory firstMessage = new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.SYSTEM.getValue()).setType(0).setContent(roleDesc).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
            entityList.add(firstMessage);
        }

        // 新增的消息
        AiMsgHistory newMsg = new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.USER.getValue()).setType(1).setContent(questionDto.getQuestion()).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date());
        if (CollectionUtil.isNotEmpty(questionDto.getFiles())){
            OssClient storage = getOssStorage(sessionId, userId);
            // 判断是否有文本数据
            for (AiChatQuestionFileDto fileDto : questionDto.getFiles()) {
                if (!fileDto.getFileType().equals(SessionType.TEXT.getCode()) || StrUtil.isBlankIfStr(fileDto.getFileUrl())){
                    continue;
                }
                Message fileMessage = Message.builder().role(Role.SYSTEM.getValue()).content(fileDto.getFileUrl()).build();
                try {
                    InputStream inputStream = storage.getObjectContent(fileDto.getFileUrl());
                    // inputStream转换成File
                    File file = FileUtils.inputStreamToTempFile(inputStream,fileDto.getFileUrl());
                    Map<String, Object> map = Forest.post("https://dashscope.aliyuncs.com/compatible-mode/v1/files")
                        .addHeader("Authorization", "Bearer " + Constants.apiKey).addFile("file", file)
                        .addBody("purpose", "file-extract").contentTypeMultipartFormData().executeAsMap();
                    if (CollectionUtil.isNotEmpty(map) && map.get("id")!=null){
                        fileMessage.setContent("fileid://"+map.get("id"));
                        fileDto.setModelFileId(String.valueOf(map.get("id")));
                    }
                }
                catch (Exception e) {
                    log.error("文件上传异常：",e);
                }
                messageManage.add(fileMessage);
            }
            newMsg.setFileUrls(JSONObject.toJSONString(questionDto.getFiles()));
        }

        entityList.add(newMsg);
        messageManage.add(Message.builder().role(Role.USER.getValue()).content(questionDto.getQuestion()).build());
        // 批量插入
        aiMapper.insertBatch(entityList);

        // 构建通义千问参数对象
        GenerationParam param = QwenReplyUtil.createGenerationParam(messageManage, QwenModelType.QWEN_LONG.getName());
        Generation generation=null;
        try {
            // 从连接池中获取对象
            generation = QwenReplyUtil.getGenerationPool().borrowObject();
            // 同步信号量
            Semaphore semaphore = new Semaphore(0);
            // 结果拼接对象
            StringBuilder resultBuilder = new StringBuilder();
            // 流式调用
            generation.streamCall(param, new ResultCallback<GenerationResult>(){
                @Override
                public void onEvent(GenerationResult generationResult) {
                    String newMessage = generationResult.getOutput().getChoices().get(0).getMessage().getContent();
                    resultBuilder.append(newMessage);
                    // 通过sse返回消息片段给前端
                    sseEmitterManager.sendMessage(userId,sessionId,newMessage);
                }
                @Override
                public void onComplete() {
                    String resString = resultBuilder.toString();
                    sseEmitterManager.sendMessage(userId,sessionId,StrUtil.isBlankIfStr(resString)?"当前问题暂时无法回答您!":resString);
                    semaphore.release();
                }
                @Override
                public void onError(Exception e) {
                    semaphore.release();
                    log.error("通义千问运行出错, 报错栈如下");
                    Throwable t = e;
                    while (t != null) {
                        log.error( t.toString());
                        t = e.getCause();
                    }
                }
            });
            // 等待信号量
            semaphore.acquire();

            String resString = resultBuilder.toString();
            // 把返回消息加入历史消息中
            aiMapper.insert(new AiMsgHistory().setSessionId(Long.parseLong(sessionId)).setRole(Role.ASSISTANT.getValue()).setType(2).setContent(StrUtil.isBlankIfStr(resString)?"当前问题暂时无法回答您!":resString).setCrtUserId(userId).setCrtUserName(username).setCrtTime(new Date()));
        }
        catch (NoApiKeyException e) {
            log.error("调用通义千问缺少ApiKey");
            throw new ServiceException("没有ApiKey");
        }
        catch (Exception e) {
            log.error("调用通义千问出现问题：",e);
            throw new ServiceException("出现了一些问题:"+e.getMessage());
        }
        finally {
            RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
            if (generation != null) {
                QwenReplyUtil.getGenerationPool().returnObject(generation);
            }
        }
    }

    /** 获取OSS实例 **/
    private OssClient getOssStorage(String sessionId, Long userId) {
        try{
            return OssFactory.instance();
        }
        catch (Exception e) {
            RedisUtils.deleteObject(ALI_QW_CHAT + sessionId + ":" + userId);
            log.error("对象存储初始化异常：",e);
            throw new ServiceException("对象存储初始化异常!");
        }
    }

    @Override
    public List<AiMsgHistory> chatMsgHistory(String sessionId, Long userId, Integer limit, String recentLatestIdStr) {
        Long recentLatestId=null;
        Long sessionIdL=null;
        if (!StrUtil.isBlankIfStr(recentLatestIdStr)) {
            recentLatestId = Long.parseLong(recentLatestIdStr);
        }
        if (!StrUtil.isBlankIfStr(sessionId)) {
            sessionIdL = Long.parseLong(sessionId);
        }
        List<AiMsgHistory> historyList = aiMapper.selectList(
            new LambdaQueryWrapper<AiMsgHistory>()
                .eq(AiMsgHistory::getCrtUserId, userId)
                .in(AiMsgHistory::getType,1,2)
                .eq(sessionIdL!=null,AiMsgHistory::getSessionId, sessionIdL)
                .lt(recentLatestId!=null,AiMsgHistory::getId,recentLatestId)
                .orderByDesc(AiMsgHistory::getCrtTime)
                .last(limit == null ? "limit 10" : "limit " + limit)
        );
        if (CollectionUtil.isEmpty(historyList)) {
            return Collections.emptyList();
        }
        // 按crtTime升序
        historyList.sort(Comparator.comparing(AiMsgHistory::getCrtTime));
        return historyList;
    }
}
