package com.example.controller;

import com.example.common.ApiResponse;
import com.example.common.view.Views;
import com.example.domain.User;
import com.example.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
// @PreAuthorize("hasRole('ADMIN')")
@Tag(name = "用户管理", description = "管理员用户管理接口")
public class AdminUserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    @JsonView(Views.Admin.class)
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情")
    @JsonView(Views.Admin.class)
    public User getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @JsonView(Views.Admin.class)
    public Page<User> getUsers(@PageableDefault Pageable page) {
        return userService.getUsers(page);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public User updateUser(@Parameter(description = "用户ID") @PathVariable Long id, @Valid @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    public void deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public String resetPassword(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.resetPassword(id);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量删除多个用户")
    public void batchDeleteUsers(@RequestBody List<Long> userIds) {
        userService.batchDeleteUsers(userIds);
    }
}