package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.domain.User;
import com.example.dto.UserDto;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, null);
    }

    @Test
    void createUser_Success() {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setFullName("Test User");
        savedUser.setStatus("ACTIVE");
        savedUser.setCreatedAt(LocalDateTime.now());

        // Mock 依赖
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 执行测试
        User result = userService.createUser(userDto);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        assertEquals("ACTIVE", result.getStatus());

        // 验证调用
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists() {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();

        // Mock 依赖
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.createUser(userDto));

        // 验证调用
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists() {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("password123")
                .build();

        // Mock 依赖
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.createUser(userDto));

        // 验证调用
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Mock 依赖
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 执行测试
        User result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());

        // 验证调用
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        // Mock 依赖
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.getUserById(1L));

        // 验证调用
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .username("updateduser")
                .email("updated@example.com")
                .fullName("Updated User")
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");
        existingUser.setFullName("Old User");
        existingUser.setPassword("encoded_password");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFullName("Updated User");
        updatedUser.setPassword("encoded_password");
        updatedUser.setUpdatedAt(LocalDateTime.now());

        // Mock 依赖
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // 执行测试
        User result = userService.updateUser(userId, userDto);

        // 验证结果
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated User", result.getFullName());

        // 验证调用
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Mock 依赖
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // 验证调用
        verify(userRepository).findById(userId);
        verify(userRepository).deleteUserRolesByUserId(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_AdminUser() {
        // 准备测试数据
        Long userId = 1L;
        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");

        // Mock 依赖
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.deleteUser(userId));

        // 验证调用
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void existsByUsername_True() {
        // Mock 依赖
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 执行测试
        boolean result = userService.existsByUsername("existinguser");

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(userRepository).existsByUsername("existinguser");
    }

    @Test
    void existsByUsername_False() {
        // Mock 依赖
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // 执行测试
        boolean result = userService.existsByUsername("newuser");

        // 验证结果
        assertFalse(result);

        // 验证调用
        verify(userRepository).existsByUsername("newuser");
    }

    @Test
    void existsByEmail_True() {
        // Mock 依赖
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // 执行测试
        boolean result = userService.existsByEmail("existing@example.com");

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    void existsByEmail_False() {
        // Mock 依赖
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // 执行测试
        boolean result = userService.existsByEmail("new@example.com");

        // 验证结果
        assertFalse(result);

        // 验证调用
        verify(userRepository).existsByEmail("new@example.com");
    }
}