package com.example.common.security;

import com.example.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * REST 认证入口点
 * 处理未认证的请求
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        log.error("Authentication failed for request {}: {}",
                 request.getRequestURI(), authException.getMessage());

        // 设置响应头
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 创建错误响应
        ApiResponse<Void> errorResponse = ApiResponse.error(
                HttpServletResponse.SC_UNAUTHORIZED,
                "认证失败: " + authException.getMessage()
        );

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}