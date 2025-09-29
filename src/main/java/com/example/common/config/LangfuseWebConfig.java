package com.example.common.config;

import com.example.common.interceptor.LangfuseTraceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Langfuse Web 配置类
 *
 * 注册 Langfuse 追踪拦截器
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "langfuse.enabled", havingValue = "true")
public class LangfuseWebConfig implements WebMvcConfigurer {

    private final LangfuseTraceInterceptor langfuseTraceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(langfuseTraceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                );
    }
}