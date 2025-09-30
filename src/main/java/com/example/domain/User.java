package com.example.domain;

import com.example.domain.base.AuditableAndSoftDeletableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableAndSoftDeletableEntity<Long> {

    public static String DEFAULT_AVATAR = "https://www.gravatar.com/avatar";
    public static String ROLE_DEFAULT = "USER";
    public static String ROLE_ADMIN = "ADMIN";

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 之间")
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column( nullable = false, length = 100)
    private String email;

    /**
     * 密码，存储加密后的密码，建议使用 BCrypt 等强哈希算法
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度至少 8 位")
    @Column(nullable = false)
    private String password;

    /**
     * 头像 URL
     */
    @Size(max = 255, message = "头像 URL 长度不能超过 255 个字符")
    private String avatar = DEFAULT_AVATAR;

    /**
     * 全名，可用做昵称/界面显示名用途
     */
    private String fullName;

    /**
     * 角色，多个角色用逗号分隔
     */
    private String roles = ROLE_DEFAULT;
}