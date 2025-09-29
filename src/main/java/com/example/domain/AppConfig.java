package com.example.domain;

import com.example.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用配置实体类
 */
@Entity
@Table(name = "app_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends BaseEntity<Long> {

    /**
     * 配置键
     */
    @Column(nullable = false, length = 100)
    private String configKey;

    /**
     * 配置值
     */
    @Column(columnDefinition = "TEXT")
    private String configValue;

    /**
     * 配置分组
     */
    @Column(nullable = false, length = 50)
    private String configGroup = "default";

    /**
     * 值类型: string/number/boolean/array/object
     */
    @Column(nullable = false, length = 20)
    private String valueType = "string";

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否系统配置
     */
    @Column(nullable = false)
    private Boolean isSystem = false;

    /**
     * 是否可编辑
     */
    @Column(nullable = false)
    private Boolean isEditable = true;
}