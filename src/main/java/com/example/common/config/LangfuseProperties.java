package com.example.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Langfuse 配置属性
 *
 * 封装 Langfuse 相关的配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "langfuse")
public class LangfuseProperties {

    private boolean enabled = false;
    private String host = "https://cloud.langfuse.com";
    private String apiKey;
    private String secretKey;
    private String project = "default";
    private String userId = "system";
    private String sessionId;
    private String tags = "api";

    private TraceConfig trace = new TraceConfig();

    @Data
    public static class TraceConfig {
        private boolean enabled = true;
        private double sampleRate = 1.0;
        private boolean includeRequestHeaders = true;
        private boolean includeResponseHeaders = true;
        private boolean includeRequestBody = true;
        private boolean includeResponseBody = true;
        private int maxBodySize = 1024;
    }
}