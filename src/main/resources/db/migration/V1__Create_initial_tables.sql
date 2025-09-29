-- Flyway database migration for Spring Boot Starter
-- V1__Create_initial_tables.sql

-- Create user table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `full_name` VARCHAR(100) COMMENT '全名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `is_admin` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否管理员',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_by` BIGINT COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Create role table
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '角色描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- Create permission table
CREATE TABLE IF NOT EXISTS `permissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `description` VARCHAR(255) COMMENT '权限描述',
    `resource_type` VARCHAR(50) NOT NULL COMMENT '资源类型: MENU, BUTTON, API',
    `resource_path` VARCHAR(255) COMMENT '资源路径',
    `method` VARCHAR(10) COMMENT 'HTTP方法',
    `parent_id` BIGINT COMMENT '父权限ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- Create user_role table
CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- Create role_permission table
CREATE TABLE IF NOT EXISTS `role_permissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- Create system_config table
CREATE TABLE IF NOT EXISTS `system_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '配置描述',
    `type` VARCHAR(50) NOT NULL DEFAULT 'STRING' COMMENT '配置类型: STRING, NUMBER, BOOLEAN, JSON',
    `is_system` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统配置',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- Insert default roles
INSERT INTO `roles` (`name`, `code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限'),
('普通用户', 'USER', '普通用户，拥有基本权限'),
('访客', 'GUEST', '访客用户，只有查看权限');

-- Insert default permissions
INSERT INTO `permissions` (`name`, `code`, `description`, `resource_type`, `resource_path`, `method`, `parent_id`, `sort_order`) VALUES
('系统管理', 'SYSTEM_MANAGE', '系统管理模块', 'MENU', '/system', NULL, 1),
('用户管理', 'USER_MANAGE', '用户管理', 'MENU', '/system/users', NULL, 2),
('查看用户', 'USER_VIEW', '查看用户列表', 'API', '/api/users', 'GET', NULL, 3),
('创建用户', 'USER_CREATE', '创建用户', 'API', '/api/users', 'POST', NULL, 4),
('更新用户', 'USER_UPDATE', '更新用户', 'API', '/api/users/*', 'PUT', NULL, 5),
('删除用户', 'USER_DELETE', '删除用户', 'API', '/api/users/*', 'DELETE', NULL, 6),
('角色管理', 'ROLE_MANAGE', '角色管理', 'MENU', '/system/roles', NULL, 7),
('权限管理', 'PERMISSION_MANAGE', '权限管理', 'MENU', '/system/permissions', NULL, 8),
('系统配置', 'CONFIG_MANAGE', '系统配置管理', 'MENU', '/system/configs', NULL, 9);

-- Insert default system configurations
INSERT INTO `system_configs` (`config_key`, `config_value`, `description`, `type`, `is_system`) VALUES
('APP_NAME', 'Spring Boot Starter', '应用名称', 'STRING', TRUE),
('APP_VERSION', '1.0.0', '应用版本', 'STRING', TRUE),
('ALLOW_REGISTRATION', 'true', '是否允许用户注册', 'BOOLEAN', TRUE),
('PASSWORD_MIN_LENGTH', '8', '密码最小长度', 'NUMBER', TRUE),
('PASSWORD_MAX_AGE_DAYS', '90', '密码有效期（天）', 'NUMBER', TRUE),
('MAX_LOGIN_ATTEMPTS', '5', '最大登录尝试次数', 'NUMBER', TRUE),
('ACCOUNT_LOCK_DURATION_MINUTES', '30', '账户锁定时长（分钟）', 'NUMBER', TRUE),
('TOKEN_EXPIRE_HOURS', '24', 'Token过期时间（小时）', 'NUMBER', TRUE),
('REFRESH_TOKEN_EXPIRE_DAYS', '7', '刷新Token过期时间（天）', 'NUMBER', TRUE),
('ENABLE_EMAIL_VERIFICATION', 'false', '是否启用邮箱验证', 'BOOLEAN', FALSE),
('ENABLE_TWO_FACTOR', 'false', '是否启用两步验证', 'BOOLEAN', FALSE);

-- Create audit log table
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `resource_type` VARCHAR(50) COMMENT '资源类型',
    `resource_id` VARCHAR(100) COMMENT '资源ID',
    `description` VARCHAR(500) COMMENT '操作描述',
    `ip_address` VARCHAR(45) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `request_url` VARCHAR(255) COMMENT '请求URL',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-成功, 0-失败',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time` BIGINT COMMENT '执行时间（毫秒）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- Insert default admin user (password: admin123)
INSERT INTO `users` (`username`, `email`, `password`, `full_name`, `is_admin`, `status`) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', '系统管理员', TRUE, 1);

-- Assign admin role to admin user
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1);