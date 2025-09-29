package com.example.domain;

import com.example.domain.base.AuditableAndSoftDeletableEntity;
import com.example.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableAndSoftDeletableEntity<Long> {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 之间")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column( nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度至少 8 位")
    @Column(nullable = false)
    private String password;

    @Size(max = 255, message = "头像 URL 长度不能超过 255 个字符")
    private String avatar;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Boolean status = true;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private boolean isAdmin = false; // FIXME: 使用 boolean 是为了使用 isAdmin() Getter 名称

    private LocalDateTime lastLoginAt;
}