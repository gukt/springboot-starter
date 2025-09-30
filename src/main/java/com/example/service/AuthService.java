package com.example.service;

import java.time.LocalDateTime;

import com.example.common.security.JwtProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.User;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.UserProfile;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;

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
    private final JpaUserDetailsService jpaUserDetailsService;
    private final JwtProperties jwtProperties;

    /**
     * 用户登录
     */
    @Transactional
    public UserProfile login(LoginRequest request) {
        String username = request.getUsername();

        log.info("User login attempt: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        try {
            // 执行认证
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 加载用户详情
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

             // 生成 JWT Token
             String accessToken = jwtService.generateToken(userDetails);

            // 更新用户最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 构建响应
            UserProfile response = UserProfile.builder()
                    .token(accessToken)
                    .tokenType("Bearer")
                    .tokenExpiresAt(LocalDateTime.now().plusDays(jwtProperties.getExpiration().toDays()))
                    .userId(user.getId())
                    .username(user.getUsername())
                    .avatar(user.getAvatar())
                    .email(user.getEmail())
                    .build();

            log.info("User login successful: {}", username);
            return response;

        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for user: {}", username);
            throw new RuntimeException("用户名或密码错误");
        } catch (DisabledException e) {
            log.warn("Login failed - account disabled for user: {}", username);
            throw new RuntimeException("用户账号已被禁用");
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @Transactional
    public UserProfile register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        log.info("User registration attempt: {}", username);

        // 验证用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed - username already exists: {}", username);
            throw new RuntimeException("用户名已存在");
        }

        // 验证邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - email already exists: {}", email);
            throw new RuntimeException("邮箱已存在");
        }

        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration failed - password mismatch for user: {}", username);
            throw new RuntimeException("密码确认不匹配");
        }

        try {
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            // 保存用户
            userRepository.save(user);

            // 生成 JWT Token
             UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(username);
             String accessToken = jwtService.generateToken(userDetails);

            // 构建响应
            UserProfile response = UserProfile.builder()
                    .token(accessToken)
                    .tokenType("Bearer")
                    .tokenExpiresAt(LocalDateTime.now().plusDays(jwtProperties.getExpiration().toDays()))
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    // FIXME
                    // .tokenExpiresAt(enExpiresAt(calculateExpiryTime(jwtUtil.getExpiration()))
                    .loginTime(LocalDateTime.now())
                    .build();

            log.info("User registration successful: {}", username);
            return response;

        } catch (Exception e) {
            log.error("Registration failed for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @Transactional
    public void logout(String token) {
        try {
            if (token != null && !token.isEmpty()) {
                // 从 Token 中提取用户信息
//                String username = jwtService.extractUsername(token);
                String username = "FIXME";
                if (username != null) {
                    // 清除安全上下文
                    SecurityContextHolder.clearContext();

                    log.info("User logout successful: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            // 登出失败不影响用户体验，只记录日志
        }
    }

    public User getCurrentUser() {
        // 从 SecurityContext 中获取当前登录的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // 没有认证信息，返回 null
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user; // 如果 principal 是 User 类型，直接返回
        }
        // 如果 principal 不是 User 类型，可以根据实际情况处理，比如返回 null 或抛出异常
        return null;
    }

}