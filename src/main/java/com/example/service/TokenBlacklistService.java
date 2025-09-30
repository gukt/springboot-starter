package com.example.service;

import java.time.Duration;
import java.util.Date;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.common.security.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Token 黑名单服务
 * 使用 Redis 管理已登出的 Token，防止被重复使用
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * 将 Token 添加到黑名单
     * 
     * @param token JWT token
     */
    public void addToBlacklist(String token) {
        try {
            // 解析 token 获取过期时间
            Claims claims = Jwts.parser()
                    .verifyWith(jwtService.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            String username = claims.getSubject();

            // 计算剩余有效时间
            long remainingTime = expiration.getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                String key = BLACKLIST_PREFIX + token;
                // 在 Redis 中存储，过期时间设置为 token 的剩余有效时间
                redisTemplate.opsForValue().set(key, username, Duration.ofMillis(remainingTime));

                log.info("Token added to blacklist for user: {}, expires at: {}", username, expiration);
            } else {
                log.debug("Token is already expired, not adding to blacklist");
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param token JWT token
     * @return true 如果在黑名单中
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check token blacklist status: {}", e.getMessage(), e);
            // 如果检查失败，为了安全考虑，返回 false（不阻止访问）
            return false;
        }
    }

    /**
     * 将用户的所有 Token 加入黑名单（踢出所有设备）
     * 
     * @param username 用户名
     */
    public void blacklistAllUserTokens(String username) {
        try {
            // 这里可以扩展为维护用户的活跃 token 列表
            // 目前的简单实现：通过用户维度的标记来实现
            String userKey = BLACKLIST_PREFIX + "user:" + username;
            redisTemplate.opsForValue().set(userKey, "all", jwtProperties.getExpiration());

            log.info("All tokens blacklisted for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to blacklist all tokens for user {}: {}", username, e.getMessage(), e);
        }
    }

    /**
     * 检查用户的所有 Token 是否被黑名单
     * 
     * @param username 用户名
     * @return true 如果用户所有 token 都被黑名单
     */
    public boolean isUserBlacklisted(String username) {
        try {
            String userKey = BLACKLIST_PREFIX + "user:" + username;
            return Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
        } catch (Exception e) {
            log.error("Failed to check user blacklist status for {}: {}", username, e.getMessage(), e);
            return false;
        }
    }
}
