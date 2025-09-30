package com.example.service;

import java.util.Arrays;
import java.util.Optional;

import com.example.common.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.domain.User;
import com.example.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 自定义的 UserDetailsService 实现类，用于加载用户详情 (UserDetails)
 * 更简单的办法是让 {@link UserService} 实现 {@link UserDetailsService} 接口，
 * 分开到此单独类处理 Spring Security 的 UserDetails 相关逻辑，是为了职责单一，关注点分离
 */
@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                var authorities = Arrays.stream(Optional.ofNullable(user.getRoles())
                                .orElse("")
                                .split(","))
                                .filter(s -> !s.isBlank())
                                .map(String::trim)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), authorities);
        }

}
