package com.example.common.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 审计员感知实现类
 * 用于自动记录创建人和更新人
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // 从 Security Context 中获取当前用户ID
        // 这里假设用户主体中包含用户ID，实际实现需要根据你的用户详情对象调整
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return Optional.of((Long) principal);
        }

        // 如果你的用户详情对象不同，需要相应调整
        // 例如：return Optional.of(((UserDetails) principal).getId());

        return Optional.empty();
    }
}