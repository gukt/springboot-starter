package com.example.repository;

import com.example.domain.AppConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用配置 Repository
 */
@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long>, JpaSpecificationExecutor<AppConfig> {

    /**
     * 根据配置键查找配置
     */
    Optional<AppConfig> findByConfigKey(String configKey);

    /**
     * 根据配置组和配置键查找配置
     */
    Optional<AppConfig> findByConfigGroupAndConfigKey(String configGroup, String configKey);

    /**
     * 根据配置组查找所有配置
     */
    List<AppConfig> findByConfigGroupOrderByConfigKey(String configGroup);

    /**
     * 根据配置组分页查找配置
     */
    Page<AppConfig> findByConfigGroup(String configGroup, Pageable pageable);

    /**
     * 查找系统配置
     */
    List<AppConfig> findByIsSystemTrue();

    /**
     * 查找可编辑的配置
     */
    List<AppConfig> findByIsEditableTrue();

    /**
     * 根据值类型查找配置
     */
    List<AppConfig> findByValueType(String valueType);

    /**
     * 根据配置键模糊搜索
     */
    @Query("SELECT c FROM AppConfig c WHERE " +
           "LOWER(c.configKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AppConfig> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 检查配置键是否存在于指定组
     */
    boolean existsByConfigGroupAndConfigKey(String configGroup, String configKey);

    /**
     * 批量更新配置组
     */
    @Query("UPDATE AppConfig c SET c.configGroup = :newGroup WHERE c.configGroup = :oldGroup")
    int updateConfigGroup(@Param("oldGroup") String oldGroup, @Param("newGroup") String newGroup);

    /**
     * 根据配置组和值类型查找配置
     */
    List<AppConfig> findByConfigGroupAndValueType(String configGroup, String valueType);

    /**
     * 删除指定配置组中的配置
     */
    void deleteByConfigGroup(String configGroup);

    /**
     * 获取所有配置组列表
     */
    @Query("SELECT DISTINCT c.configGroup FROM AppConfig c ORDER BY c.configGroup")
    List<String> findAllConfigGroups();
}