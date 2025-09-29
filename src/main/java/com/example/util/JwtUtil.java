package com.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 负责JWT Token的生成、解析、验证等操作
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥 - 从配置文件读取
     */
    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    /**
     * JWT过期时间（毫秒） - 默认24小时
     * -- GETTER --
     *  获取JWT过期时间

     */
    @Getter
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 刷新Token过期时间（毫秒） - 默认7天
     * -- GETTER --
     *  获取刷新Token过期时间

     */
    @Getter
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中获取所有claims
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 验证token签名
     */
    public Boolean validateTokenSignature(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token signature validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查token是否即将过期（剩余时间小于5分钟）
     */
    public Boolean isTokenExpiringSoon(String token) {
        try {
            Long remainingTime = getRemainingTimeInSeconds(token);
            return remainingTime > 0 && remainingTime < 300; // 5分钟
        } catch (Exception e) {
            log.error("Failed to check if token is expiring soon: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取剩余有效时间（毫秒）
     */
    public Long getRemainingValidity(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (Exception e) {
            log.error("Failed to get remaining validity: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        try {
            return getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 为用户生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 生成刷新token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 生成带有自定义声明的token
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.debug("Generated JWT token for user: {}, expires at: {}", subject, expiryDate);
        return token;
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token是否有效（不检查用户详情）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为刷新token
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取剩余有效时间（秒）
     */
    public Long getRemainingTimeInSeconds(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 刷新token
     */
    public String refreshToken(String refreshToken) {
        try {
            if (!isRefreshToken(refreshToken) || isTokenExpired(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            String username = getUsernameFromToken(refreshToken);
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, username, expiration);
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh token", e);
        }
    }
}