package com.example.controller;

import java.util.List;
import java.util.Map;

import com.example.common.security.CustomUserDetails;
import com.example.common.view.UserViews;
import com.example.common.view.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

/**
 * 用户控制器
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "Bearer") // TODO Deep into this
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    @JsonView(Views.Admin.class)
    public User getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        long userId = userDetails.getUserId();
        return userService.getById(userId);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新当前用户信息")
    public User updateUserProfile(@Valid @RequestBody User user) {
        long userId = authService.requireCurrentUserId();
        return userService.updateUser(userId, user);
    }

    /**
     * 修改当前用户密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改当前用户密码")
    public void changePassword(@RequestBody Map<String, String> passwordRequest) {
        long userId = authService.requireCurrentUserId();
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("旧密码和新密码不能为空");
        }

        userService.changePassword(userId, oldPassword, newPassword);
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
}