package com.example.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Swagger/OpenAPI 配置类
 *
 * 提供以下功能：
 * - API 文档自动生成
 * - JWT 认证支持
 * - 接口分组管理
 * - 环境配置
 * - 联系信息和许可证
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final Environment environment;

    /**
     * OpenAPI 配置
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot Starter API")
                        .version("1.0.0")
                        .description("Spring Boot 脚手架项目 API 文档")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@example.com")
                                .url("https://github.com/username/springboot-starter"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("JWT 认证令牌，格式：Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .servers(Arrays.asList(
                        new Server().url("/").description("默认服务器"),
                        new Server().url("http://localhost:8080").description("开发环境服务器"),
                        new Server().url("https://api.example.com").description("生产环境服务器")
                ));
    }

    /**
     * 默认 API 分组
     */
    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .displayName("默认接口")
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * 用户管理 API 分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("用户管理")
                .pathsToMatch("/api/users/**", "/api/auth/**")
                .build();
    }

    /**
     * 系统管理 API 分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统管理")
                .pathsToMatch("/api/system/**", "/api/config/**")
                .build();
    }

    /**
     * 测试 API 分组
     */
    @Bean
    public GroupedOpenApi testApi() {
        return GroupedOpenApi.builder()
                .group("test")
                .displayName("测试接口")
                .pathsToMatch("/api/test/**")
                .build();
    }
}