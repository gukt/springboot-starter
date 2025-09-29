package com.example.service;

import com.example.common.exception.BusinessException;
import com.example.domain.AppConfig;
import com.example.dto.UpdateAppConfigRequest;
import com.example.repository.AppConfigRepository;
import com.example.service.base.AbstractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppConfigService extends AbstractService<AppConfig, Long> {

    private final AppConfigRepository appConfigRepository;

    /**
     * 创建配置
     */
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public AppConfig createConfig(AppConfig request) {
        if (appConfigRepository.existsByConfigGroupAndConfigKey(request.getConfigGroup(), request.getConfigKey())) {
            throw new BusinessException("配置键已存在: " + request.getConfigKey());
        }

        AppConfig config = new AppConfig();
        BeanUtils.copyProperties(request, config);
        return appConfigRepository.save(config);
    }

    /**
     * 更新配置
     */
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public AppConfig updateConfig(Long id, UpdateAppConfigRequest request) {
        AppConfig config = getById(id);

        if (config.getIsSystem()) {
            throw new BusinessException("系统配置不允许修改");
        }

        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getIsEditable() != null) {
            config.setIsEditable(request.getIsEditable());
        }

        return appConfigRepository.save(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void deleteConfig(Long id) {
        AppConfig config = getById(id);

        if (config.getIsSystem()) {
            throw new BusinessException("系统配置不允许删除");
        }

        appConfigRepository.delete(config);
    }

    /**
     * 根据 ID 获取配置
     */
    @Cacheable(value = "config", key = "'id:' + #id")
    public AppConfig getConfigById(Long id) {
        return getById(id);
    }

    /**
     * 根据配置键获取配置
     */
    @Cacheable(value = "config", key = "'key:' + #configKey")
    public AppConfig getConfigByKey(String configKey) {
        return appConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException("配置不存在: " + configKey));
    }

    /**
     * 根据配置组和配置键获取配置
     */
    @Cacheable(value = "config", key = "'group:' + #configGroup + ':key:' + #configKey")
    public AppConfig getConfigByGroupAndKey(String configGroup, String configKey) {
        return appConfigRepository.findByConfigGroupAndConfigKey(configGroup, configKey)
                .orElseThrow(() -> new BusinessException("配置不存在: " + configGroup + "." + configKey));
    }

    /**
     * 获取配置值
     */
    @Cacheable(value = "config", key = "'value:' + #configKey")
    public String getConfigValue(String configKey) {
        return appConfigRepository.findByConfigKey(configKey)
                .map(AppConfig::getConfigValue)
                .orElseThrow(() -> new BusinessException("配置不存在: " + configKey));
    }

    /**
     * 根据配置组获取所有配置
     */
    @Cacheable(value = "config", key = "'group:' + #configGroup")
    public List<AppConfig> getConfigsByGroup(String configGroup) {
        return appConfigRepository.findByConfigGroupOrderByConfigKey(configGroup);
    }

    /**
     * 获取配置组映射
     */
    @Cacheable(value = "config", key = "'groupMap:' + #configGroup")
    public Map<String, String> getConfigGroupMap(String configGroup) {
        return appConfigRepository.findByConfigGroupOrderByConfigKey(configGroup)
                .stream()
                .collect(Collectors.toMap(
                        AppConfig::getConfigKey,
                        AppConfig::getConfigValue
                ));
    }

    /**
     * 获取所有配置组
     */
    public List<String> getAllConfigGroups() {
        return appConfigRepository.findAllConfigGroups();
    }

    /**
     * 分页获取配置列表
     */
    public Page<AppConfig> getConfigsPage(String configGroup, Pageable pageable) {
        if (configGroup != null && !configGroup.isEmpty()) {
            return appConfigRepository.findByConfigGroup(configGroup, pageable);
        } else {
            return appConfigRepository.findAll(pageable);
        }
    }

    /**
     * 搜索配置
     */
    public Page<AppConfig> searchConfigs(String keyword, Pageable pageable) {
        return appConfigRepository.searchByKeyword(keyword, pageable);
    }
}