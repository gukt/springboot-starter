package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户注册或登录时返回的相应，包括用户基本信息与 Token 相关信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册或登录时返回的相应，包括用户基本信息与 Token 相关信息")
public class UserProfile {

    @Schema(description = "访问Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "访问 Token 过期时间")
    private LocalDateTime tokenExpiresAt;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "用户全名", example = "系统管理员")
    private String fullName;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "是否为管理员", example = "true")
    @Builder.Default
    private Boolean isAdmin = false;

    @Schema(description = "是否需要修改密码", example = "false")
    @Builder.Default
    private Boolean requirePasswordChange = false;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}