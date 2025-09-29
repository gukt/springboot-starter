package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.domain.User;
import com.example.repository.UserRepository;
import com.example.service.base.AbstractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends AbstractService<User, Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 获取用户详情
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        if (!existingUser.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        if (!existingUser.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 分页获取用户列表
     */
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户
     */
    public Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        if (StringUtils.hasText(keyword) && StringUtils.hasText(status)) {
            return userRepository.findByUsernameContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (StringUtils.hasText(keyword)) {
            return userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        } else if (StringUtils.hasText(status)) {
            return userRepository.findByStatus(status, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public User updateUserStatus(Long id, String status) {
        User user = getUserById(id);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 重置密码
     */
    @Transactional
    public String resetPassword(Long id) {
        User user = getUserById(id);
        String newPassword = "123456"; // 实际应用中应该生成随机密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return newPassword;
    }

    /**
     * 批量删除用户
     */
    @Transactional
    public void batchDeleteUsers(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new BusinessException("部分用户不存在");
        }
        userRepository.deleteAll(users);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}