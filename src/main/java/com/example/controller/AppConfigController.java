package com.example.controller;

import com.example.domain.AppConfig;
import com.example.dto.CreateAppConfigRequest;
import com.example.dto.UpdateAppConfigRequest;
import com.example.service.AppConfigService;
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

/**
 * 应用配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "应用配置管理", description = "应用配置管理相关接口")
@SecurityRequirement(name = "Bearer")
public class AppConfigController {

    private final AppConfigService appConfigService;

    /**
     * 创建配置
     */
    @PostMapping
    @Operation(summary = "创建配置", description = "创建新的应用配置")
    @PreAuthorize("hasRole('ADMIN')")
    public AppConfig createConfig(@Valid @RequestBody CreateAppConfigRequest request) {
        return appConfigService.createConfig(request);
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新配置", description = "更新指定的应用配置")
    @PreAuthorize("hasRole('ADMIN')")
    public AppConfig updateConfig(@Parameter(description = "配置ID") @PathVariable Long id, @Valid @RequestBody UpdateAppConfigRequest request) {
        return appConfigService.updateConfig(id, request);
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置", description = "删除指定的应用配置")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        appConfigService.deleteConfig(id);
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情", description = "根据配置ID获取配置详情")
    @PreAuthorize("hasRole('ADMIN')")
    public AppConfig getConfigById(@Parameter(description = "配置ID") @PathVariable Long id) {
        return appConfigService.getConfigById(id);
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键获取配置", description = "根据配置键获取配置信息")
    public AppConfig getConfigByKey(@Parameter(description = "配置键") @PathVariable String configKey) {
        return appConfigService.getConfigByKey(configKey);
    }

    /**
     * 根据配置组和配置键获取配置
     */
    @GetMapping("/group/{configGroup}/key/{configKey}")
    @Operation(summary = "获取指定组的配置", description = "根据配置组和配置键获取配置")
    public AppConfig getConfigByGroupAndKey(@Parameter(description = "配置组") @PathVariable String configGroup, @Parameter(description = "配置键") @PathVariable String configKey) {
        return appConfigService.getConfigByGroupAndKey(configGroup, configKey);
    }

    /**
     * 获取配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "获取配置值", description = "获取指定配置键的配置值")
    public String getConfigValue(@Parameter(description = "配置键") @PathVariable String configKey) {
        return appConfigService.getConfigValue(configKey);
    }

    /**
     * 获取配置组的所有配置
     */
    @GetMapping("/group/{configGroup}")
    @Operation(summary = "获取配置组", description = "获取指定配置组的所有配置")
    public List<AppConfig> getConfigsByGroup(@Parameter(description = "配置组") @PathVariable String configGroup) {
        return appConfigService.getConfigsByGroup(configGroup);
    }

    /**
     * 获取配置组映射
     */
    @GetMapping("/group/{configGroup}/map")
    @Operation(summary = "获取配置组映射", description = "获取指定配置组的键值对映射")
    public Map<String, String> getConfigGroupMap(@Parameter(description = "配置组") @PathVariable String configGroup) {
        return appConfigService.getConfigGroupMap(configGroup);
    }

    /**
     * 获取所有配置组
     */
    @GetMapping("/groups")
    @Operation(summary = "获取配置组列表", description = "获取所有配置组名称")
    public List<String> getAllConfigGroups() {
        return appConfigService.getAllConfigGroups();
    }

    /**
     * 分页获取配置列表
     */
    @GetMapping
    @Operation(summary = "获取配置列表", description = "分页获取配置列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AppConfig> getConfigsPage(@Parameter(description = "配置组") @RequestParam(required = false) String configGroup, @PageableDefault(sort = "id") Pageable pageable) {
        return appConfigService.getConfigsPage(configGroup, pageable);
    }

    /**
     * 搜索配置
     */
    @GetMapping("/search")
    @Operation(summary = "搜索配置", description = "根据关键词搜索配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AppConfig> searchConfigs(@Parameter(description = "搜索关键词") @RequestParam String keyword, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return appConfigService.searchConfigs(keyword, pageable);
    }

    /**
     * 获取应用基础信息（公开接口）
     */
    @GetMapping("/app/info")
    @Operation(summary = "获取应用信息", description = "获取应用基础配置信息")
    public Map<String, String> getAppInfo() {
        return appConfigService.getConfigGroupMap("app");
    }

    /**
     * 获取 UI 配置（公开接口）
     */
    @GetMapping("/ui/settings")
    @Operation(summary = "获取 UI 配置", description = "获取界面相关配置")
    public Map<String, String> getUiSettings() {
        return appConfigService.getConfigGroupMap("ui");
    }
}