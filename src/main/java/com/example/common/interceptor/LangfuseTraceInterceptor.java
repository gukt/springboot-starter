package com.example.common.interceptor;

import com.example.common.service.LangfuseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Langfuse 追踪拦截器
 *
 * 拦截 HTTP 请求，捕获请求参数和响应内容，并发送到 Langfuse
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangfuseTraceInterceptor implements HandlerInterceptor {

    private final LangfuseService langfuseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成请求 ID
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("langfuseRequestId", requestId);
        request.setAttribute("langfuseStartTime", System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            Long startTime = (Long) request.getAttribute("langfuseStartTime");
            if (startTime == null) {
                return;
            }

            String requestId = (String) request.getAttribute("langfuseRequestId");
            long duration = System.currentTimeMillis() - startTime;

            // 捕获请求信息
            Map<String, String> requestHeaders = getRequestHeaders(request);
            Object requestBody = getRequestBody(request);

            // 捕获响应信息
            Object responseBody = getResponseBody(response);
            int statusCode = response.getStatus();

            // 记录到 Langfuse
            langfuseService.logApiRequest(
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    requestHeaders,
                    requestBody,
                    responseBody,
                    statusCode,
                    duration
            );

        } catch (Exception e) {
            log.error("Failed to trace request in Langfuse: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    /**
     * 获取请求体
     */
    private Object getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    String requestBody = new String(buf, request.getCharacterEncoding());
                    return parseJsonOrReturnString(requestBody);
                } catch (Exception e) {
                    log.warn("Failed to parse request body: {}", e.getMessage());
                    // FIXME
                    // return requestBody;
                }
            }
        }
        return null;
    }

    /**
     * 获取响应体
     */
    private Object getResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
            byte[] buf = responseWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    String responseBody = new String(buf, response.getCharacterEncoding());
                    return parseJsonOrReturnString(responseBody);
                } catch (Exception e) {
                    log.warn("Failed to parse response body: {}", e.getMessage());
                    // FIXME
                    // return requestBody;
                }
            }
        }
        return null;
    }

    /**
     * 尝试解析 JSON，如果失败则返回原始字符串
     */
    private Object parseJsonOrReturnString(String content) {
        try {
            return objectMapper.readValue(content, Object.class);
        } catch (Exception e) {
            return content;
        }
    }
}