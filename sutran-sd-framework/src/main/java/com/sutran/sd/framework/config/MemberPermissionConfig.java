package com.sutran.sd.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 会员权限配置类
 * 
 * @author SutranSD
 */
@Configuration
@EnableAspectJAutoProxy
public class MemberPermissionConfig {
    // AOP配置已通过@EnableAspectJAutoProxy启用
    // 会员权限切面会自动扫描@RequireMember注解
}
