package com.example.common.service;

import com.example.common.config.LangfuseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Langfuse 服务类
 *
 * 负责向 Langfuse 发送追踪数据
 */
@Slf4j
@RequiredArgsConstructor
public class LangfuseService {

    private final LangfuseConfig config;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    /**
     * 创建追踪记录
     */
    public void createTrace(String traceId, String name, String userId, Map<String, Object> input, Map<String, Object> output, Map<String, Object> metadata) {
        if (!shouldTrace()) {
            return;
        }

        try {
            Map<String, Object> traceData = new HashMap<>();
            traceData.put("id", traceId);
            traceData.put("name", name);
            traceData.put("userId", userId != null ? userId : config.getUserId());
            traceData.put("sessionId", config.getSessionId());
            traceData.put("tags", config.getTags());
            traceData.put("input", input);
            traceData.put("output", output);
            traceData.put("metadata", metadata);
            traceData.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(traceData);
            sendToLangfuse("/api/public/traces", json);
        } catch (Exception e) {
            log.error("Failed to create trace in Langfuse: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建事件记录
     */
    public void createEvent(String eventName, Map<String, Object> properties) {
        if (!shouldTrace()) {
            return;
        }

        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventName", eventName);
            eventData.put("properties", properties);
            eventData.put("userId", config.getUserId());
            eventData.put("sessionId", config.getSessionId());
            eventData.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(eventData);
            sendToLangfuse("/api/public/events", json);
        } catch (Exception e) {
            log.error("Failed to create event in Langfuse: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录 API 请求
     */
    public void logApiRequest(String requestId, String method, String path, Map<String, String> headers, Object requestBody, Object responseBody, int statusCode, long duration) {
        if (!shouldTrace()) {
            return;
        }

        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("requestId", requestId);
            requestData.put("method", method);
            requestData.put("path", path);
            requestData.put("statusCode", statusCode);
            requestData.put("duration", duration);

            if (config.getTrace().isIncludeRequestHeaders()) {
                requestData.put("requestHeaders", filterHeaders(headers));
            }

            if (config.getTrace().isIncludeRequestBody() && requestBody != null) {
                String bodyString = objectMapper.writeValueAsString(requestBody);
                if (bodyString.length() <= config.getTrace().getMaxBodySize() * 1024) {
                    requestData.put("requestBody", requestBody);
                } else {
                    requestData.put("requestBody", "[BODY_TOO_LARGE]");
                }
            }

            if (responseBody != null) {
                String bodyString = objectMapper.writeValueAsString(responseBody);
                if (bodyString.length() <= config.getTrace().getMaxBodySize() * 1024) {
                    requestData.put("responseBody", responseBody);
                } else {
                    requestData.put("responseBody", "[BODY_TOO_LARGE]");
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("userAgent", headers.get("user-agent"));
            metadata.put("clientIp", headers.get("x-forwarded-for"));

            createTrace(requestId, method + " " + path, config.getUserId(), requestData, Map.of("statusCode", statusCode), metadata);
        } catch (Exception e) {
            log.error("Failed to log API request to Langfuse: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送数据到 Langfuse
     */
    private void sendToLangfuse(String endpoint, String json) throws IOException {
        String url = config.getHost() + endpoint;

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey());

        if (config.getSecretKey() != null && !config.getSecretKey().isEmpty()) {
            requestBuilder.header("X-API-Secret", config.getSecretKey());
        }

        Request request = requestBuilder.build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Langfuse API request failed: {} - {}", response.code(), responseBody);
            } else {
                log.debug("Successfully sent data to Langfuse: {}", endpoint);
            }
        }
    }

    /**
     * 判断是否应该追踪
     */
    private boolean shouldTrace() {
        return config.getTrace().isEnabled() &&
               config.getTrace().getSampleRate() > 0 &&
               random.nextDouble() <= config.getTrace().getSampleRate();
    }

    /**
     * 过滤敏感请求头
     */
    private Map<String, String> filterHeaders(Map<String, String> headers) {
        Map<String, String> filtered = new HashMap<>();
        Set<String> sensitiveHeaders = Set.of("authorization", "cookie", "set-cookie", "x-api-key", "x-auth-token");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (sensitiveHeaders.contains(key)) {
                filtered.put(entry.getKey(), "[FILTERED]");
            } else {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        return filtered;
    }
}