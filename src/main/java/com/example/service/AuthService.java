package com.example.service;

import com.example.domain.User;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.JwtResponse;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;

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
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @Transactional
    public JwtResponse login(LoginRequest request) {
        String username = request.getUsername();

        log.info("User login attempt: {}", username);

        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 生成JWT Token
            String accessToken = jwtUtil.generateToken(userDetails, user.getId(), user.getFullName());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // 更新用户最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 构建响应
            JwtResponse response = JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .accessTokenExpiresAt(calculateExpiryTime(jwtUtil.getExpiration()))
                    .refreshTokenExpiresAt(calculateExpiryTime(jwtUtil.getRefreshExpiration()))
                    .isAdmin(user.getIsAdmin())
                    .loginTime(LocalDateTime.now())
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
    public JwtResponse register(RegisterRequest request) {
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
            user.setStatus(true);
            user.setIsAdmin(false);

            // 保存用户
            userRepository.save(user);

            // 生成 JWT Token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String accessToken = jwtUtil.generateToken(userDetails, user.getId(), user.getFullName());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // 构建响应
            JwtResponse response = JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .accessTokenExpiresAt(calculateExpiryTime(jwtUtil.getExpiration()))
                    .refreshTokenExpiresAt(calculateExpiryTime(jwtUtil.getRefreshExpiration()))
                    .isAdmin(user.getIsAdmin())
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
     * 刷新Token
     */
    @Transactional
    public JwtResponse refreshToken(String refreshToken) {
        log.debug("Token refresh attempt");

        try {
            // 验证刷新Token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                log.warn("Invalid refresh token type");
                throw new RuntimeException("无效的刷新Token");
            }

            if (!jwtUtil.validateToken(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                throw new RuntimeException("刷新Token已过期或无效");
            }

            // 从Token中提取用户名
            String username = jwtUtil.extractUsername(refreshToken);
            if (username == null) {
                log.warn("Cannot extract username from refresh token");
                throw new RuntimeException("无效的刷新Token");
            }

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 生成新的Token
            String newAccessToken = jwtUtil.generateToken(userDetails, user.getId(), user.getFullName());
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            // 构建响应
            JwtResponse response = JwtResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .accessTokenExpiresAt(calculateExpiryTime(jwtUtil.getExpiration()))
                    .refreshTokenExpiresAt(calculateExpiryTime(jwtUtil.getRefreshExpiration()))
                    .isAdmin(user.getIsAdmin())
                    .build();

            log.debug("Token refresh successful for user: {}", username);
            return response;

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            throw new RuntimeException("Token刷新失败: " + e.getMessage());
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
                String username = jwtUtil.extractUsername(token);
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

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 计算过期时间
     */
    private LocalDateTime calculateExpiryTime(Long expirationMs) {
        return LocalDateTime.now().plusSeconds(expirationMs / 1000);
    }
}