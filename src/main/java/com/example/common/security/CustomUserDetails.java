package com.example.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 自定义的 UserDetails 实现类，因为考虑到逻辑中需要用到 userId，自定义的 CustomUserDetails 包含了 userId，会让代码更直观易懂。
 * 也可以不用自定义 CustomUserDetails，直接返回 Spring Security 提供的
 * {@link org.springframework.security.core.userdetails.User User} 实现类，
 * 但那样的话，需要将 userId 放到 User 的 username 字段中，取值的时候使用 getUsername() 方法获取 userId（语义不清晰，不推荐）。
 * <p>
 * 也可以让我们的 User 实体类实现 UserDetails 接口，然后直接使用 user 对象。
 * 但不建议这么做，原因是不想使 User 实体类和 Spring Security 强耦合，也不方便测试。
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

}
