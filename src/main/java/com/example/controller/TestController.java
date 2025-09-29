package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 *
 * 用于测试各种功能的简单接口
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "测试接口", description = "功能测试接口")
public class TestController {

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    @Operation(summary = "测试接口", description = "简单的测试接口")
    public String hello() {
        return "Hello, Spring Boot Starter!";
    }

    /**
     * 获取当前时间
     */
    @GetMapping("/time")
    public Map<String, Object> getCurrentTime() {
        return Map.of("time", LocalDateTime.now());
    }

    /**
     * 需要认证的测试接口
     */
    @GetMapping("/auth")
    @Operation(summary = "认证测试", description = "需要 JWT 认证的测试接口")
    @SecurityRequirement(name = "Bearer")
    public String testAuth() {
        return "Authentication successful!";
    }

    /**
     * 测试健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "系统健康检查")
    public Map<String, Object> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("version", "1.0.0");
        healthStatus.put("javaVersion", System.getProperty("java.version"));
        healthStatus.put("serverTime", System.currentTimeMillis());

        return healthStatus;
    }
}