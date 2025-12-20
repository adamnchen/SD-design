package com.sutran.sd.framework.config;

import com.sutran.sd.common.utils.StringUtils;
import com.sutran.sd.framework.config.properties.SpringDocProperties;
import com.sutran.sd.framework.handler.OpenApiHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Swagger 文档配置
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Configuration
@AutoConfigureBefore(SpringDocConfiguration.class)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class SpringDocConfig {

    private final ServerProperties serverProperties;
    private static final String TOKEN_HEADER = "Authorization";

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openApi(SpringDocProperties properties) {
        OpenAPI openApi = new OpenAPI();
        // 文档基本信息
        SpringDocProperties.InfoProperties infoProperties = properties.getInfo();
        Info info = convertInfo(infoProperties);
        openApi.info(info);
        // 扩展文档信息
        openApi.externalDocs(properties.getExternalDocs());
        openApi.tags(properties.getTags());
        openApi.paths(properties.getPaths());
        openApi.components(properties.getComponents());
        Set<String> keySet = properties.getComponents().getSecuritySchemes().keySet();
        List<SecurityRequirement> list = new ArrayList<>();
        SecurityRequirement securityRequirement = new SecurityRequirement();
        keySet.forEach(securityRequirement::addList);
        list.add(securityRequirement);
        openApi.security(list);

        return openApi;
    }

    private Info convertInfo(SpringDocProperties.InfoProperties infoProperties) {
        Info info = new Info();
        info.setTitle(infoProperties.getTitle());
        info.setDescription(infoProperties.getDescription());
        info.setContact(infoProperties.getContact());
        info.setLicense(infoProperties.getLicense());
        info.setVersion(infoProperties.getVersion());
        return info;
    }

    /**
     * 自定义 openapi 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openApi,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomisers, Optional<JavadocProvider> javadocProvider) {
        return new OpenApiHandler(openApi, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomisers, serverBaseUrlCustomisers, javadocProvider);
    }

    /**
     * 对已经生成好的 OpenApi 进行自定义操作
     */
    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        String contextPath = serverProperties.getServlet().getContextPath();
        String finalContextPath;
        if (StringUtils.isBlank(contextPath) || "/".equals(contextPath)) {
            finalContextPath = "";
        } else {
            finalContextPath = contextPath;
        }
        // 对所有路径增加前置上下文路径
        return openApi -> {
            Paths oldPaths = openApi.getPaths();
            if (oldPaths instanceof PlusPaths) {
                return;
            }
            PlusPaths newPaths = new PlusPaths();
            oldPaths.forEach((k,v) -> newPaths.addPathItem(finalContextPath + k, v));
            openApi.setPaths(newPaths);
        };
    }

    /**
     * 单独使用一个类便于判断 解决springdoc路径拼接重复问题
     *
     * @author Lion Li
     */
    static class PlusPaths extends Paths {
        public PlusPaths() {
            super();
        }
    }

    /**
     * 代码生成接口
     */
    @Bean
    public GroupedOpenApi generatorServiceApi() {
        return GroupedOpenApi.builder()
            // 组名
            .group("代码生成接口")
            // 扫描的路径，支持通配符
            .pathsToMatch("/tool/gen/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            // 扫描的包
            .packagesToScan("com.sutran.sd.controller.generator")
            .build();
    }

    /**
     * 系统接口
     */
    @Bean
    public GroupedOpenApi sysServiceApi() {
        return GroupedOpenApi.builder()
            .group("WEB系统接口")
            .pathsToMatch("/system/**","/monitor/**","/wx-mp-bind","/wx-mp-unbind","/getInfo","/getRouters","/close-guide","/notice/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.web")
            .build();
    }

    /**
     * 认证接口
     */
    @Bean
    public GroupedOpenApi authServiceApi() {
        return GroupedOpenApi.builder()
            .group("认证接口")
            .pathsToMatch("/captchaSms","/v2/captchaSms","/captchaEmail","/captchaImage","/login","/bs-login","/pre-login","/sms-login","/bs-sms-login","/email-login","/bs-email-login","/xcx-login","/wx-mp-login","/logout","/getLoggerLevel","/changeLoggerLevel","/register","/mp/js-ticket")
            .packagesToScan("com.sutran.sd.controller.web")
            .build();
    }

    /**
     * SD接口
     */
    @Bean
    public GroupedOpenApi sdServiceApi() {
        return GroupedOpenApi.builder()
            .group("SD绘图接口")
            .pathsToMatch("/sd/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.sdapi")
            .build();
    }

    /**
     * AI接口
     */
    @Bean
    public GroupedOpenApi aiServiceApi() {
        return GroupedOpenApi.builder()
            .group("AI接口")
            .pathsToMatch("/ai/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.ai")
            .build();
    }

    /**
     * 微信接口
     */
    @Bean
    public GroupedOpenApi wxServiceApi() {
        return GroupedOpenApi.builder()
            .group("微信接口")
            .pathsToMatch("/wx/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.wx")
            .build();
    }

    /**
     * 支付宝接口
     */
    @Bean
    public GroupedOpenApi payServiceApi() {
        return GroupedOpenApi.builder()
            .group("支付接口")
            .pathsToMatch("/pay/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.pay")
            .build();
    }


    /**
     * 设计接口
     */
    @Bean
    public GroupedOpenApi designServiceApi() {
        return GroupedOpenApi.builder()
            .group("设计接口")
            .pathsToMatch("/design/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.design")
            .build();
    }

    /**
     * 用户端接口
     */
    @Bean
    public GroupedOpenApi userServiceApi() {
        return GroupedOpenApi.builder()
            .group("用户端接口")
            .pathsToMatch("/user/**")
            // 添加自定义配置，这里添加了一个用户认证的 header，否则 knife4j 里会没有 header
            .addOperationCustomizer((operation, handlerMethod) -> operation.security(
                Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER)))
            )
            .packagesToScan("com.sutran.sd.controller.profile")
            .build();
    }

}
