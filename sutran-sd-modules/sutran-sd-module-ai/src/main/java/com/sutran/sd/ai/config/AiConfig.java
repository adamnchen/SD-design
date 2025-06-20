package com.sutran.sd.ai.config;

import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * API_KEY 配置
 * @author zj
 * @date 2024-12-16
 */
@Configuration
public class AiConfig {

    @Value("${ali.apiKey}")
    private String apiKey;

    @PostConstruct
    public void config() {
        // 启动后将API_KEY配置到常量类中
        Constants.apiKey = apiKey;
    }
}
