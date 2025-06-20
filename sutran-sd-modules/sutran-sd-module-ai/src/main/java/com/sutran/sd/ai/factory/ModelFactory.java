package com.sutran.sd.ai.factory;

import com.sutran.sd.ai.constants.CommonKey;
import com.sutran.sd.ai.entity.AiChatModel;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.utils.redis.RedisUtils;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AI模型工厂处理
 * @author zj
 * @date 2025-03-11
 */
@Slf4j
public class ModelFactory {

    private static final Map<String, StreamingChatLanguageModel> MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 根据AI模型ID获取LangChain4j的bean配置
     * @param modelId   模型ID
     * @return  模型bean实例
     */
    public static StreamingChatLanguageModel getModelInstance(String modelId) {
        StreamingChatLanguageModel model = MODEL_INSTANCE_CACHE.get(modelId);
        if (model == null) {
            AiChatModel chatModel= RedisUtils.getCacheObject(CommonKey.AI_CHAT_MODEL + modelId);
            if (chatModel == null) {
                throw new ServiceException("未找到AI模型配置");
            }
            LOCK.lock();
            try {
                model = MODEL_INSTANCE_CACHE.get(modelId);
                if (model == null) {
                    model = OpenAiStreamingChatModel.builder().apiKey(chatModel.getApiKey())
                            .modelName(chatModel.getModelName()).baseUrl(chatModel.getBaseUrl())
                            // 降低随机性
                            .temperature(0.2)
                            // 降低重复度
                            .frequencyPenalty(0.5)
                            // 增加多样性
                            .presencePenalty(0.3)
                            // 响应格式(强制json格式)
                            //.responseFormat("json_object")
                            // 模型监听器
                            //.listeners(List.of(StreamAssistantChatModelListener.builder().build()))
                            // 超时时间
                            .timeout(chatModel.getTimeOut() == null ? Duration.ofSeconds(60) : Duration.ofSeconds(chatModel.getTimeOut()))
                            .logRequests(true)
                            .logResponses(true)
                            .build();
                    MODEL_INSTANCE_CACHE.put(modelId, model);
                }
            }
            finally {
                LOCK.unlock();
            }
        }
        return model;
    }

}
