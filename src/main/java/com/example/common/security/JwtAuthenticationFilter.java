package com.example.common.security;

import com.example.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 处理每个请求中的 JWT token
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 从请求中提取 JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查是否应该跳过 JWT 验证
     */
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 跳过公开路径
        return path.startsWith("/api/auth/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/actuator/") ||
               path.equals("/error") ||
               path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 检查是否应该跳过过滤
            if (shouldSkipFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 提取 JWT token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 验证 token 格式和签名
            if (!jwtUtil.validateTokenSignature(token)) {
                log.warn("Invalid JWT token signature for request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 检查 token 是否过期
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Expired JWT token for request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 检查是否是刷新 token（刷新 token 不能用于访问受保护的资源）
            if (jwtUtil.isRefreshToken(token)) {
                log.warn("Refresh token used for access to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 从 token 中提取用户名
            String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 验证 token
                if (jwtUtil.validateToken(token, userDetails)) {
                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 设置认证详情
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 将认证信息设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    log.debug("Set authentication for user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error processing JWT authentication for request to {}: {}",
                     request.getRequestURI(), e.getMessage(), e);

            // 清除安全上下文
            SecurityContextHolder.clearContext();

            // 继续过滤器链，让后续的异常处理器处理
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 记录认证信息（用于调试）
     */
    private void logAuthenticationInfo(String username, HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("User '{}' authenticated from IP: {}, User-Agent: {}",
                     username,
                     request.getRemoteAddr(),
                     request.getHeader("User-Agent"));
        }
    }

    /**
     * 检查 token 是否即将过期
     */
    private boolean isTokenExpiringSoon(String token) {
        return jwtUtil.isTokenExpiringSoon(token);
    }

    /**
     * 获取 token 的剩余有效时间
     */
    private long getTokenRemainingTime(String token) {
        return jwtUtil.getRemainingValidity(token);
    }
}