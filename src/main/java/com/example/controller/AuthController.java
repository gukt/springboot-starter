package com.example.controller;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserProfile;
import com.example.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器。
 * 处理用户登录、注册、Token刷新等认证相关操作
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取 JWT Token")
    public UserProfile login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        UserProfile response = authService.login(request);

        log.info("User login successful: {}", request.getUsername());

        return response;
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("User logout attempt");

        // 提取 Token
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        authService.logout(token);

        log.info("User logout successful");
    }
}