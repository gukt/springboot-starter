package com.example.controller;

import com.example.common.ApiResponse;
import com.example.domain.User;
import com.example.service.AuthService;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public User getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public User updateUser(@Parameter(description = "用户ID") @PathVariable Long id, @Valid @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getUsers(@Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
                               @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                               @Parameter(description = "排序字段") @RequestParam(required = false) String sort,
                               @Parameter(description = "排序方向") @RequestParam(required = false) String direction) {

        Sort.Direction sortDirection = direction != null && direction.equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = sort != null ?
            PageRequest.of(page, size, Sort.by(sortDirection, sort)) :
            PageRequest.of(page, size);

        return userService.getUsers(pageable);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchUsers(@Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
                                  @Parameter(description = "用户状态") @RequestParam(required = false) String status,
                                  @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
                                  @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return userService.searchUsers(keyword, status, pageable);
    }

    /**
     * 更新用户状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "更新用户状态")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserStatus(@Parameter(description = "用户ID") @PathVariable Long id, @Parameter(description = "用户状态") @RequestParam String status) {
        return userService.updateUserStatus(id, status);
    }

    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public void changePassword(@Parameter(description = "用户ID") @PathVariable Long id, @RequestBody Map<String, String> passwordRequest) {
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("旧密码和新密码不能为空");
        }

        userService.changePassword(id, oldPassword, newPassword);
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return ApiResponse.success(newPassword);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量删除多个用户")
    @PreAuthorize("hasRole('ADMIN')")
    public void batchDeleteUsers(@RequestBody List<Long> userIds) {
        userService.batchDeleteUsers(userIds);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public Boolean checkUsername(@Parameter(description = "用户名") @RequestParam String username) {
        return userService.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    public Boolean checkEmail(@Parameter(description = "邮箱") @RequestParam String email) {
        return userService.existsByEmail(email);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    @Operation(summary = "更新当前用户", description = "更新当前登录用户信息")
    public User updateCurrentUser(@Valid @RequestBody User user) {
        User currentUser = authService.getCurrentUser();
        return userService.updateUser(currentUser.getId(), user);
    }

    /**
     * 修改当前用户密码
     */
    @PostMapping("/me/change-password")
    @Operation(summary = "修改当前用户密码", description = "修改当前登录用户密码")
    public void changeCurrentUserPassword(@RequestBody Map<String, String> passwordRequest) {
        User currentUser = authService.getCurrentUser();
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("旧密码和新密码不能为空");
        }

        userService.changePassword(currentUser.getId(), oldPassword, newPassword);
    }
}