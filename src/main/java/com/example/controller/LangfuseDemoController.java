package com.example.controller;

import com.example.common.service.LangfuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Langfuse 演示控制器
 *
 * 演示如何使用 Langfuse 进行请求追踪
 */
@Slf4j
@RestController
@RequestMapping("/api/langfuse")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "langfuse.enabled", havingValue = "true")
public class LangfuseDemoController {

    private final LangfuseService langfuseService;

    /**
     * 测试端点 - 发送测试事件到 Langfuse
     */
    @PostMapping("/test-event")
    public ResponseEntity<Map<String, String>> testEvent(@RequestBody Map<String, Object> eventData) {
        langfuseService.createEvent("test_event", eventData);

        Map<String, String> response = Map.of(
                "message", "Test event sent to Langfuse",
                "eventId", java.util.UUID.randomUUID().toString()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 测试端点 - 发送测试追踪到 Langfuse
     */
    @PostMapping("/test-trace")
    public ResponseEntity<Map<String, String>> testTrace(@RequestBody Map<String, Object> traceData) {
        String traceId = java.util.UUID.randomUUID().toString();

        langfuseService.createTrace(
                traceId,
                "Test Trace",
                "demo-user",
                traceData,
                Map.of("status", "completed", "timestamp", System.currentTimeMillis()),
                Map.of("source", "demo-controller", "environment", "development")
        );

        Map<String, String> response = Map.of(
                "message", "Test trace sent to Langfuse",
                "traceId", traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取 Langfuse 配置状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
                "langfuseEnabled", true,
                "timestamp", System.currentTimeMillis(),
                "endpoint", "/api/langfuse/test-event",
                "description", "Langfuse integration is active"
        );

        return ResponseEntity.ok(status);
    }
}