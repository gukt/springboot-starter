package com.example.controller;

import com.example.common.ApiResponse;
import com.example.domain.User;
import com.example.dto.PageRequest;
import com.example.dto.PageResponse;
import com.example.dto.UserDto;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器测试类
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("testuser");
        createdUser.setEmail("test@example.com");
        createdUser.setFullName("Test User");
        createdUser.setStatus("ACTIVE");
        createdUser.setCreatedAt(LocalDateTime.now());

        // Mock 依赖
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        // 执行测试
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_InvalidData() throws Exception {
        // 准备测试数据（无效数据）
        UserDto userDto = UserDto.builder()
                .username("")  // 空用户名
                .email("invalid-email")  // 无效邮箱
                .password("123")  // 密码太短
                .build();

        // 执行测试
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setStatus("ACTIVE");

        // Mock 依赖
        when(userService.getUserById(1L)).thenReturn(user);

        // 执行测试
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound() throws Exception {
        // Mock 依赖
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        // 执行测试
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_Success() throws Exception {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        PageResponse<User> pageResponse = PageResponse.of(
                Collections.singletonList(user),
                1,
                10,
                1L
        );

        // Mock 依赖
        when(userService.getUsers(any(PageRequest.class))).thenReturn(pageResponse);

        // 执行测试
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success() throws Exception {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("updateduser")
                .email("updated@example.com")
                .fullName("Updated User")
                .build();

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFullName("Updated User");
        updatedUser.setStatus("ACTIVE");

        // Mock 依赖
        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(updatedUser);

        // 执行测试
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_Forbidden() throws Exception {
        // 准备测试数据
        UserDto userDto = UserDto.builder()
                .username("updateduser")
                .email("updated@example.com")
                .build();

        // 执行测试（普通用户尝试更新其他用户信息）
        mockMvc.perform(put("/api/users/2")  // 尝试更新其他用户
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMe_Success() throws Exception {
        // 准备测试数据
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setEmail("test@example.com");
        currentUser.setFullName("Test User");

        // Mock 依赖
        when(userService.getUserById(1L)).thenReturn(currentUser);

        // 执行测试
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void checkUsername_Success() throws Exception {
        // Mock 依赖
        when(userService.existsByUsername("testuser")).thenReturn(true);

        // 执行测试
        mockMvc.perform(get("/api/users/check-username")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void checkEmail_Success() throws Exception {
        // Mock 依赖
        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        // 执行测试
        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_Success() throws Exception {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        PageResponse<User> pageResponse = PageResponse.of(
                Collections.singletonList(user),
                1,
                10,
                1L
        );

        // Mock 依赖
        when(userService.searchUsers(any(), any(), any(PageRequest.class))).thenReturn(pageResponse);

        // 执行测试
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "test")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserStatistics_Success() throws Exception {
        // 准备测试数据
        UserService.UserStatistics statistics = new UserService.UserStatistics(
                100L, 80L, 20L, 5L, 25L
        );

        // Mock 依赖
        when(userService.getUserStatistics()).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(100))
                .andExpect(jsonPath("$.data.activeCount").value(80))
                .andExpect(jsonPath("$.data.inactiveCount").value(20));
    }
}