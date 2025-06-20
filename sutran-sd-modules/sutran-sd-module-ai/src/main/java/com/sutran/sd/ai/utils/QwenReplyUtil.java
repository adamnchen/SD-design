package com.sutran.sd.ai.utils;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.sutran.sd.ai.factory.PooledDashScopeObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.List;
import java.util.Objects;

/**
 * @author zj
 * @date 2024-12-20
 */
public class QwenReplyUtil {

    /**
     * 对象池
     */
    private static GenericObjectPool<Generation> generationPool;

    /**
     * 设置参数
     * @param messages 消息
     * @return 参数实例
     */
    public static GenerationParam createGenerationParam(List<Message> messages,String model) {
        return GenerationParam.builder()
            .model(model)
            .messages(messages)
            .enableSearch(true)
            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
            .topP(0.8)
            .incrementalOutput(true)
            .build();
    }

    /**
     * 设置参数
     * @param messages 消息
     * @return 参数实例
     */
    public static MultiModalConversationParam createMultiModalConversationParam(List<MultiModalMessage> messages, String model) {
        return MultiModalConversationParam.builder()
            .model(model)
            .messages(messages)
            .enableSearch(true)
            .topP(0.8)
            .incrementalOutput(true)
            .build();
    }

    /**
     * 开启连接池（这里我们使用的是单例模式）
     */
    public static GenericObjectPool<Generation> getGenerationPool() {
        if (Objects.isNull(generationPool)) {
            //开启连接池
            PooledDashScopeObjectFactory pooledDashScopeObjectFactory = new PooledDashScopeObjectFactory();
            GenericObjectPoolConfig<Generation> config = new GenericObjectPoolConfig<>();
            // 对于语音服务，websocket协议，保持下面值相同
            config.setMaxTotal(32);
            config.setMaxIdle(32);
            config.setMinIdle(32);
            generationPool = new GenericObjectPool<>(pooledDashScopeObjectFactory, config);
        }
        return generationPool;
    }


    /**
     * 生成式文本敏感词过滤（大家根据自己实际情况去编写）
     */
    public static String filterSensitiveWords(String text) {
        // TODO
        return text;
    }

}
