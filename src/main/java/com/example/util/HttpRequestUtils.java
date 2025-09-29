package com.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HttpRequestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> BINARY_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/", "audio/", "video/", "application/octet-stream",
            "application/pdf", "application/zip", "application/x-rar-compressed"
    ));

    /**
     * 提取请求的详细信息
     */
    public static String extractDetails(HttpServletRequest request) {
        StringBuilder details = new StringBuilder();

        // 请求基本信息
        details.append(String.format("""
                        \n
                        ⚠️ REQUEST DETAILS
                        ===========================================================
                        - DateTime    : %s
                        - Method      : %s
                        - URL         : %s
                        - RemoteAddr  : %s
                        - User Agent  : %s
                        """,
                new Date(),
                request.getMethod(),
                getFullURL(request),
                getClientIp(request),
                request.getHeader("User-Agent")
        ));

        // Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            details.append("\n[Headers]\n");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String value = isSensitiveHeader(headerName)
                        ? "[SENSITIVE_CONTENT]"
                        : request.getHeader(headerName);
                details.append("  %s: %s\n".formatted(headerName, value));
            }
        }

        // Parameters
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            details.append("\n[Parameters]\n");
            parameterMap.forEach((key, values) -> {
                String value = isSensitiveParameter(key)
                        ? "[SENSITIVE_CONTENT]"
                        : String.join(",", values);
                details.append("  %s: %s\n".formatted(key, value));
            });
        }

        // 请求体（非 GET 请求）
        if (isNotGet(request)) {
            try {
                String body = getRequestBody(request);
                if (StringUtils.hasText(body)) {
                    details.append("\n[Request Body]\n");
                    try {
                        Object json = objectMapper.readValue(body, Object.class);
                        details.append("  ")
                                .append(objectMapper.writerWithDefaultPrettyPrinter()
                                        .writeValueAsString(json).replace("\n", "\n  "));
                    } catch (Exception e) {
                        details.append("  ").append(body);
                    }
                }
            } catch (Exception e) {
                details.append("\n[Request Body]\n  Failed to read: ").append(e.getMessage());
            }
        }

        details.append("\n============================================================\n");

        // 最后添加 curl 命令
        // details.append(generateCurlCommand(request)).append("\n");

        return details.toString();
    }

    /**
     * 获取完整的请求 URL，该方法主要解决以下问题：
     * <ol>
     * <li>在本地开发环境，如果使用 8080 端口，则会显示 http://localhost:8080/api/xxx。</li>
     * <li>在外网环境，通过 Nginx 代理后，会显示为 https://www.domain.com/api/xxx。</li>
     * <li>如果 Nginx 设置了路径重写，也能正确处理路径，比如：https://www.domain.com/api/xxx ->
     * https://api.domain.com/xxx</li>
     * <li>只在非默认端口（非 80/443）时才显示端口号</li>
     * <li>当有 X-Forwarded-Host 时不显示端口号（因为通常代理后的地址不需要显示端口）</li>
     * </ol>
     *
     * <p>
     * 主要实现细节：
     * <ul>
     * <li>通过 X-Forwarded-Proto 获取正确的协议（http/https）</li>
     * <li>通过 X-Forwarded-Host 获取正确的域名</li>
     * <li>通过 X-Forwarded-Prefix 处理 Nginx 转发时的路径前缀</li>
     * <li>只在非默认端口（非 80/443）时才显示端口号</li>
     * <li>当有 X-Forwarded-Host 时不显示端口号（因为通常代理后的地址不需要显示端口）</li>
     * </ul>
     *
     * <p>
     * 注意：要确保你的 Nginx 配置正确设置了这些 header：
     *
     * <pre>{@code
     * proxy_set_header X-Forwarded-Proto $scheme;
     * proxy_set_header X-Forwarded-Host $host;
     * proxy_set_header X-Forwarded-Prefix /api; # 如果有路径前缀的话
     * }</pre>
     */
    private static String getFullURL(HttpServletRequest request) {
        // 获取请求头中的信息
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedPrefix = request.getHeader("X-Forwarded-Prefix");

        // 构建基础 URL
        String scheme = forwardedProto != null ? forwardedProto : request.getScheme();
        String host = forwardedHost != null ? forwardedHost : request.getServerName();
        int port = request.getServerPort();

        // 只有当端口不是默认端口时才添加端口号
        boolean isDefaultPort = (scheme.equals("http") && port == 80)
                || (scheme.equals("https") && port == 443);

        StringBuilder requestURL = new StringBuilder();
        requestURL.append(scheme).append("://").append(host);
        if (!isDefaultPort && forwardedHost == null) {
            requestURL.append(":").append(port);
        }

        // 处理路径
        String path = request.getRequestURI();
        if (forwardedPrefix != null && !forwardedPrefix.equals("/")) {
            // 如果存在 X-Forwarded-Prefix，需要将其添加到路径中
            if (!path.startsWith(forwardedPrefix)) {
                path = forwardedPrefix + path;
            }
        }
        requestURL.append(path);

        // 添加查询参数
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }

        return requestURL.toString();
    }

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String[] IP_HEADERS = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 如果是多个代理，获取第一个IP
                return ip.contains(",") ? ip.split(",")[0] : ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取 Request Body 内容。
     */
    private static String getRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && isBinaryContent(contentType)) {
            return "[BINARY_CONTENT]";
        }

        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, StandardCharsets.UTF_8);
            }
        } else {
            // 如果不是 ContentCachingRequestWrapper，则需要手动读取
            try {
                return request.getReader()
                        .lines()
                        .collect(Collectors.joining("\n"));
            } catch (IOException e) {
                log.warn("Failed to read request body", e);
                return "[Failed to read request body]";
            }
        }
        return "";
    }

    /**
     * 检查是否是二进制内容。
     */
    private static boolean isBinaryContent(String contentType) {
        contentType = contentType.toLowerCase();
        return BINARY_CONTENT_TYPES.stream().anyMatch(contentType::startsWith);
    }

    /**
     * 检查是否是敏感的请求头。
     */
    private static boolean isSensitiveHeader(String headerName) {
        Set<String> sensitiveHeaders = new HashSet<>(Arrays.asList(
                "Authorization",
                "Cookie",
                "Proxy-Authorization",
                "X-API-Key"
                // 可以添加更多敏感请求头
        ));
        return sensitiveHeaders.contains(headerName.toLowerCase());
    }

    /**
     * 检查是否是敏感的参数。
     */
    private static boolean isSensitiveParameter(String paramName) {
        Set<String> sensitiveParams = new HashSet<>(Arrays.asList(
                "password",
                "pwd",
                "secret",
                "token",
                "access_token",
                "refresh_token"
                // 可以添加更多敏感参数
        ));
        return sensitiveParams.contains(paramName.toLowerCase());
    }

    /**
     * 生成方便调试的 curl 命令。
     */
    public static String generateCurlCommand(HttpServletRequest request) {
        StringBuilder curl = new StringBuilder("\n\ncurl '%s' \\\n".formatted(getFullURL(request)));

        // 添加请求方法
        if (isNotGet(request)) {
            // 这里的 \\ 用于命令行换行标记，\n 用于文本换行，以下同
            curl.append("  -X '%s' \\\n".formatted(request.getMethod()));
        }

        // 添加请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 跳过一些不需要的请求头
            if (!"Content-Length".equalsIgnoreCase(headerName)
                    && !"Host".equalsIgnoreCase(headerName)) {
                curl.append("  -H '%s: %s' \\\n".formatted(headerName, headerValue));
            }
        }

        // 添加请求体（非 GET 请求）
        if (isNotGet(request)) {
            try {
                String body = getRequestBody(request);
                if (StringUtils.hasText(body) && !body.equals("[BINARY_CONTENT]")) {
                    // 添加缩进，第一行不缩进
                    String[] lines = body.split("\n");
                    StringBuilder indentedBody = new StringBuilder(lines[0]); // 第一行不缩进
                    for (int i = 1; i < lines.length; i++) {
                        indentedBody.append("\n  ").append(lines[i]); // 后续行添加缩进
                    }
                    curl.append("  --data-binary '%s' \\\n".formatted(indentedBody));
                }
            } catch (Exception ignored) {
                // 如果无法读取请求体，则忽略
            }
        }

        // 添加压缩选项
        curl.append("  --compressed");

        return curl.toString();
    }

    private static boolean isGet(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod());
    }

    private static boolean isNotGet(HttpServletRequest request) {
        return !isGet(request);
    }
}
