package com.example.common.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.service.JwtService;
import com.example.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 认证过滤器，处理每个请求中的 JWT token
 * OncePerRequestFilter 确保过滤器每次请求只运行一次。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Token 并验证，如果无效则跳过
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response); // TODO 或者直接返回 401
            return;
        }

        // 检查 Token 是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Blacklisted token detected");
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.getUsername(token);
        // 如果 token 有效: 加载用户详情并创建 Authentication 放到 SecurityContext，
        // 后续的过滤器和 Controller 方法都可以通过 SecurityContext 获取用户信息与权限
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    // /**
    // * 从请求中提取 JWT token
    // */
    // private String extractTokenFromRequest(HttpServletRequest request) {
    // String bearerToken = request.getHeader("Authorization");
    // if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
    // return bearerToken.substring(7);
    // }
    // return null;
    // }
    //
    // /**
    // * 检查是否应该跳过 JWT 验证
    // */
    // private boolean shouldSkipFilter(HttpServletRequest request) {
    // String path = request.getRequestURI();
    //
    // // 跳过公开路径
    // return path.startsWith("/api/auth/") ||
    // path.startsWith("/swagger-ui/") ||
    // path.startsWith("/v3/api-docs/") ||
    // path.startsWith("/swagger-resources/") ||
    // path.startsWith("/webjars/") ||
    // path.startsWith("/actuator/") ||
    // path.equals("/error") ||
    // path.equals("/favicon.ico");
    // }
    //
    // /**
    // * 记录认证信息（用于调试）
    // */
    // private void logAuthenticationInfo(String username, HttpServletRequest
    // request) {
    // if (log.isDebugEnabled()) {
    // log.debug("User '{}' authenticated from IP: {}, User-Agent: {}",
    // username,
    // request.getRemoteAddr(),
    // request.getHeader("User-Agent"));
    // }
    // }
    //
    // /**
    // * 检查 token 是否即将过期
    // */
    // private boolean isTokenExpiringSoon(String token) {
    // return jwtUtil.isTokenExpiringSoon(token);
    // }
    //
    // /**
    // * 获取 token 的剩余有效时间
    // */
    // private long getTokenRemainingTime(String token) {
    // return jwtUtil.getRemainingValidity(token);
    // }
}