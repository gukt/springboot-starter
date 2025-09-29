-- Flyway database migration for Spring Boot Starter
-- V2__Create_app_config_table.sql

-- Create app_config table
CREATE TABLE IF NOT EXISTS `app_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_group` VARCHAR(50) NOT NULL DEFAULT 'default' COMMENT '配置分组',
    `value_type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '值类型：string,number,boolean,json',
    `description` VARCHAR(255) COMMENT '配置描述',
    `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统配置',
    `is_editable` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可编辑',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_by` BIGINT COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_group_key` (`config_group`, `config_key`),
    KEY `idx_config_group` (`config_group`),
    KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用配置表';

-- Insert sample app configurations
INSERT INTO `app_config` (`config_key`, `config_value`, `config_group`, `value_type`, `description`, `is_system`, `is_editable`) VALUES
-- App basic configurations
('app.name', 'Spring Boot Starter', 'app', 'string', '应用名称', true, false),
('app.version', '1.0.0', 'app', 'string', '应用版本', true, false),
('app.description', '企业级 Spring Boot 脚手架项目', 'app', 'string', '应用描述', false, true),
('app.copyright', '© 2024 Spring Boot Starter. All rights reserved.', 'app', 'string', '版权信息', false, true),
('app.logo_url', '/assets/logo.png', 'app', 'string', '应用 Logo 地址', false, true),
('app.favicon_url', '/assets/favicon.ico', 'app', 'string', '应用 Favicon 地址', false, true),

-- UI configurations
('ui.theme', 'system', 'ui', 'string', '主题模式: light, dark, system', false, true),
('ui.primary_color', '#1890ff', 'ui', 'string', '主色调', false, true),
('ui.secondary_color', '#52c41a', 'ui', 'string', '次要色调', false, true),
('ui.layout', 'sidebar', 'ui', 'string', '布局方式: sidebar, topbar', false, true),
('ui.sidebar_collapsed', 'false', 'ui', 'boolean', '侧边栏是否折叠', false, true),
('ui.show_footer', 'true', 'ui', 'boolean', '是否显示页脚', false, true),
('ui.page_size', '20', 'ui', 'number', '每页显示条数', false, true),
('ui.date_format', 'YYYY-MM-DD', 'ui', 'string', '日期格式', false, true),
('ui.time_format', 'HH:mm:ss', 'ui', 'string', '时间格式', false, true),
('ui.enable_animations', 'true', 'ui', 'boolean', '是否启用动画', false, true),

-- Web page configurations
('web.privacy_policy_url', 'https://example.com/privacy', 'web', 'string', '隐私政策地址', false, true),
('web.terms_of_service_url', 'https://example.com/terms', 'web', 'string', '服务条款地址', false, true),
('web.contact_email', 'support@example.com', 'web', 'string', '联系邮箱', false, true),
('web.contact_phone', '+86 400-123-4567', 'web', 'string', '联系电话', false, true),
('web.company_name', 'Spring Boot Starter Inc.', 'web', 'string', '公司名称', false, true),
('web.company_address', '北京市朝阳区xxx街道xxx号', 'web', 'string', '公司地址', false, true),
('web.icp_number', '京ICP备12345678号', 'web', 'string', 'ICP备案号', false, true),
('web.enable_registration', 'true', 'web', 'boolean', '是否开放注册', false, true),
('web.enable_guest_access', 'true', 'web', 'boolean', '是否允许访客访问', false, true),

-- Security configurations
('security.password_min_length', '8', 'security', 'number', '密码最小长度', true, false),
('security.password_max_length', '50', 'security', 'number', '密码最大长度', true, false),
('security.session_timeout', '30', 'security', 'number', '会话超时时间（分钟）', true, false),
('security.max_login_attempts', '5', 'security', 'number', '最大登录尝试次数', true, false),
('security.lockout_duration', '30', 'security', 'number', '账户锁定时长（分钟）', true, false),
('security.enable_two_factor', 'false', 'security', 'boolean', '是否启用两步验证', false, true),
('security.enable_captcha', 'true', 'security', 'boolean', '是否启用验证码', false, true),

-- External service configurations
('external.google_analytics_id', 'G-XXXXXXXXXX', 'external', 'string', 'Google Analytics ID', false, true),
('external.sentry_dsn', 'https://xxxx@sentry.io/xxxxx', 'external', 'string', 'Sentry DSN', false, true),
('external.recaptcha_site_key', '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI', 'external', 'string', 'reCAPTCHA 站点密钥', false, true),
('external.map_api_key', '', 'external', 'string', '地图 API 密钥', false, true),
('external.sms_api_key', '', 'external', 'string', '短信 API 密钥', false, true),
('external.email_api_key', '', 'external', 'string', '邮件 API 密钥', false, true),

-- Email configurations
('email.smtp_host', 'smtp.example.com', 'email', 'string', 'SMTP 服务器地址', false, true),
('email.smtp_port', '587', 'email', 'number', 'SMTP 端口', false, true),
('email.smtp_username', 'noreply@example.com', 'email', 'string', 'SMTP 用户名', false, true),
('email.smtp_password', '', 'email', 'string', 'SMTP 密码', false, true),
('email.from_name', 'Spring Boot Starter', 'email', 'string', '发件人名称', false, true),
('email.from_address', 'noreply@example.com', 'email', 'string', '发件人地址', false, true),
('email.enable_ssl', 'true', 'email', 'boolean', '是否启用 SSL', false, true),

-- Upload configurations
('upload.max_file_size', '10', 'upload', 'number', '最大文件大小（MB）', false, true),
('upload.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 'upload', 'string', '允许的文件类型', false, true),
('upload.image_max_size', '5', 'upload', 'number', '图片最大大小（MB）', false, true),
('upload.video_max_size', '100', 'upload', 'number', '视频最大大小（MB）', false, true),
('upload.storage_path', '/uploads', 'upload', 'string', '文件存储路径', false, true),
('upload.enable_cloud_storage', 'false', 'upload', 'boolean', '是否启用云存储', false, true),

-- Feature flags
('feature.enable_chat', 'false', 'feature', 'boolean', '是否启用聊天功能', false, true),
('feature.enable_notification', 'true', 'feature', 'boolean', '是否启用通知功能', false, true),
('feature.enable_multilingual', 'false', 'feature', 'boolean', '是否启用多语言', false, true),
('feature.enable_dark_mode', 'true', 'feature', 'boolean', '是否启用暗黑模式', false, true),
('feature.enable_mobile_app', 'false', 'feature', 'boolean', '是否启用移动应用', false, true),

-- Business configurations
('business.currency', 'CNY', 'business', 'string', '默认货币', false, true),
('business.timezone', 'Asia/Shanghai', 'business', 'string', '时区', false, true),
('business.language', 'zh-CN', 'business', 'string', '默认语言', false, true),
('business.tax_rate', '0.06', 'business', 'number', '税率', false, true),
('business.order_prefix', 'ORD', 'business', 'string', '订单前缀', false, true),
('business.invoice_prefix', 'INV', 'business', 'string', '发票前缀', false, true),

-- API configurations
('api.rate_limit', '1000', 'api', 'number', 'API 调用频率限制（次/小时）', false, true),
('api.enable_documentation', 'true', 'api', 'boolean', '是否启用 API 文档', false, true),
('api.enable_swagger', 'true', 'api', 'boolean', '是否启用 Swagger', false, true),
('api.cors_origins', '*', 'api', 'string', 'CORS 允许的源', false, true),
('api.request_timeout', '30', 'api', 'number', '请求超时时间（秒）', false, true),

-- Cache configurations
('cache.default_ttl', '3600', 'cache', 'number', '默认缓存时间（秒）', false, true),
('cache.enable_redis', 'true', 'cache', 'boolean', '是否启用 Redis 缓存', false, true),
('cache.enable_local_cache', 'true', 'cache', 'boolean', '是否启用本地缓存', false, true),
('cache.cache_prefix', 'app:', 'cache', 'string', '缓存前缀', false, true);