package com.sutran.sd.pay.config;

import com.sutran.sd.pay.interceptor.AliPayInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zj
 * @date 2025年08月19日 17:46
 */
@Configuration
public class AliPayMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(new AliPayInterceptor()).addPathPatterns("/pay/ali/**","/sys/order/syncStatus");
    }
}
