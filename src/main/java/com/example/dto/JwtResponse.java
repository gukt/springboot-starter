package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * JW 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT响应")
public class JwtResponse {

    @Schema(description = "访问Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "刷新Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Token类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "用户全名", example = "系统管理员")
    private String fullName;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "用户角色列表")
    private Set<String> roles;

    @Schema(description = "用户权限列表")
    private Set<String> permissions;

    @Schema(description = "访问Token过期时间")
    private LocalDateTime accessTokenExpiresAt;

    @Schema(description = "刷新Token过期时间")
    private LocalDateTime refreshTokenExpiresAt;

    @Schema(description = "是否为管理员", example = "true")
    @Builder.Default
    private Boolean isAdmin = false;

    @Schema(description = "是否需要修改密码", example = "false")
    @Builder.Default
    private Boolean requirePasswordChange = false;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}