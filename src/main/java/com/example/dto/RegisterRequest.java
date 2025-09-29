package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Schema(description = "用户名", example = "newuser")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "newuser@example.com")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度至少8位")
    @Schema(description = "密码", example = "password123")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "password123")
    private String confirmPassword;

    @Size(max = 100, message = "全名长度不能超过100个字符")
    @Schema(description = "全名", example = "新用户")
    private String fullName;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "验证码", example = "123456")
    private String captcha;

    @Schema(description = "验证码Key", example = "captcha_key_123")
    private String captchaKey;
}