package com.example.service;

import java.util.Objects;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.common.exception.BusinessException;
import com.example.common.security.CustomUserDetails;
import com.example.common.security.JwtProperties;
import com.example.domain.User;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.repository.UserRepository;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务类
 * 处理用户登录、注册、Token管理等认证相关业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        log.info("User login attempt: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        try {
            // 执行认证
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(auth);
            // 加载用户详情
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            // 生成 JWT Token
            String accessToken = jwtService.generateToken(userDetails);

            userRepository.save(user);

            // 构建响应
            LoginResponse response = LoginResponse.builder()
                    .token(accessToken)
                    .expiresIn(jwtProperties.getExpiration().toSeconds())
                    .user(user)
                    .build();

            log.info("用户登录成功: {}", username);
            return response;

        } catch (BadCredentialsException e) {
            log.warn("登录失败 - 用户名或密码错误: {}", username);
            throw new RuntimeException("用户名或密码错误");
        } catch (DisabledException e) {
            log.warn("登录失败 - 用户账号已被禁用: {}", username);
            throw new RuntimeException("用户账号已被禁用");
        } catch (AuthenticationException e) {
            log.error("登录失败 - 用户 {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        log.info("User registration attempt: {}", username);

        // 验证用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            log.warn("注册失败 - 用户名已存在: {}", username);
            throw new RuntimeException("用户名已存在");
        }

        // 验证邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            log.warn("注册失败 - 邮箱已存在: {}", email);
            throw new RuntimeException("邮箱已存在");
        }

        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("注册失败 - 密码确认不匹配: {}", username);
            throw new RuntimeException("密码确认不匹配");
        }

        try {
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(Objects.requireNonNullElse(request.getFullName(), username));
            userRepository.save(user);

            // 构建响应
            LoginResponse response = LoginResponse.builder()
                    .token(jwtService.generateToken(username)) // 生成 JWT Token
                    .expiresIn(jwtProperties.getExpiration().toSeconds())
                    .user(user)
                    .build();

            log.info("注册成功: {}", username);
            return response;

        } catch (Exception e) {
            log.error("注册失败 - 用户 {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @Transactional
    public void logout(User user) {
        try {
            // 从请求头中获取当前的 token
            String token = getCurrentToken();
            if (token != null) {
                // 将 token 添加到黑名单
                tokenBlacklistService.addToBlacklist(token);
            }
            log.info("用户成功登出 - {}#{}", user.getUsername(), user.getId());
        } catch (Exception e) {
            log.error("用户登出失败 - {}#{}: {}", user.getUsername(), user.getId(), e.getMessage(), e);
        } finally {
            // 不管成功还是失败，总是清除当前线程的安全上下文
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 尝试获取当前登录用户 ID，如果用户已登录且验证通过，返回其 userId，反之返回 {@code null}
     *
     * @return 当前登录用户 ID，如果用户未登录，返回 {@code null}
     */
    @Nullable
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 没有认证信息或认证未通过，返回 null
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // 如果用户已登录且验证通过，返回其 userId
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }

    /**
     * 获取当前登录用户 ID，如果用户未登录，抛出 {@link BusinessException}
     */
    public long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return userId;
    }

    /**
     * 从请求上下文中提取 Token
     */
    private String getCurrentToken() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting token from request: {}", e.getMessage());
        }
        return null;
    }
}