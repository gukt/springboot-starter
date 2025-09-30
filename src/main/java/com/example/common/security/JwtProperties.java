package com.example.common.security;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * 用于签名的密钥，至少 32 个字符
     */
    private String secret;

    /**
     * Token 过期时间，默认 1 天
     */
    private Duration expiration;

    /**
     * 签发者，默认为 ${spring.application.name}
     */
    private String issuer;

    // Helper Methods

    public Date getExpirationDateSince(Instant now) {
        return Date.from(now.plus(expiration.toDays(), ChronoUnit.DAYS));
    }

}
