package com.example.common.config;

import com.example.common.service.LangfuseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Langfuse 配置类
 *
 * 提供 Langfuse 追踪和监控功能的配置
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "langfuse")
@RequiredArgsConstructor
@Slf4j
public class LangfuseConfig {

    @NotBlank(message = "Langfuse API 密钥不能为空")
    private String apiKey;

    private String secretKey;

    @NotNull(message = "Langfuse 服务器地址不能为空")
    private String host = "https://cloud.langfuse.com";

    private String project = "default";
    private String userId = "system";
    private String sessionId;
    private List<String> tags = List.of("api");

    private TraceConfig trace = new TraceConfig();

    @Data
    public static class TraceConfig {
        private boolean enabled = true;
        @Min(0)
        @Max(1)
        private double sampleRate = 1.0;
        private boolean includeRequestHeaders = true;
        private boolean includeResponseHeaders = true;
        private boolean includeRequestBody = true;
        private boolean includeResponseBody = true;
        @Min(1)
        @Max(10240)
        private int maxBodySize = 1024; // KB
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public LangfuseService langfuseService(OkHttpClient okHttpClient) {
        return new LangfuseService(this, okHttpClient);
    }
}