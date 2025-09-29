package com.example.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 构造方法
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * 构造方法
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造方法
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * 构造方法
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 创建业务异常（静态工厂方法）
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 创建业务异常（带错误代码）
     */
    public static BusinessException of(String errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    /**
     * 创建用户不存在异常
     */
    public static BusinessException userNotFound(String username) {
        return new BusinessException("USER_NOT_FOUND", "用户不存在: " + username);
    }

    /**
     * 创建用户已存在异常
     */
    public static BusinessException userAlreadyExists(String username) {
        return new BusinessException("USER_ALREADY_EXISTS", "用户已存在: " + username);
    }

    /**
     * 创建邮箱已存在异常
     */
    public static BusinessException emailAlreadyExists(String email) {
        return new BusinessException("EMAIL_ALREADY_EXISTS", "邮箱已存在: " + email);
    }

    /**
     * 创建密码错误异常
     */
    public static BusinessException passwordError() {
        return new BusinessException("PASSWORD_ERROR", "密码错误");
    }

    /**
     * 创建账号已禁用异常
     */
    public static BusinessException accountDisabled(String username) {
        return new BusinessException("ACCOUNT_DISABLED", "账号已禁用: " + username);
    }

    /**
     * 创建账号已锁定异常
     */
    public static BusinessException accountLocked(String username) {
        return new BusinessException("ACCOUNT_LOCKED", "账号已锁定: " + username);
    }

    /**
     * 创建验证码错误异常
     */
    public static BusinessException captchaError() {
        return new BusinessException("CAPTCHA_ERROR", "验证码错误");
    }

    /**
     * 创建验证码过期异常
     */
    public static BusinessException captchaExpired() {
        return new BusinessException("CAPTCHA_EXPIRED", "验证码已过期");
    }

    /**
     * 创建令牌无效异常
     */
    public static BusinessException invalidToken() {
        return new BusinessException("INVALID_TOKEN", "令牌无效");
    }

    /**
     * 创建令牌过期异常
     */
    public static BusinessException tokenExpired() {
        return new BusinessException("TOKEN_EXPIRED", "令牌已过期");
    }

    /**
     * 创建权限不足异常
     */
    public static BusinessException insufficientPermission() {
        return new BusinessException("INSUFFICIENT_PERMISSION", "权限不足");
    }

    /**
     * 创建操作失败异常
     */
    public static BusinessException operationFailed(String operation) {
        return new BusinessException("OPERATION_FAILED", operation + "失败");
    }

    /**
     * 创建参数错误异常
     */
    public static BusinessException invalidParameter(String parameter) {
        return new BusinessException("INVALID_PARAMETER", "参数错误: " + parameter);
    }

    /**
     * 创建配置错误异常
     */
    public static BusinessException configurationError(String config) {
        return new BusinessException("CONFIGURATION_ERROR", "配置错误: " + config);
    }

    /**
     * 创建外部服务异常
     */
    public static BusinessException externalServiceError(String service) {
        return new BusinessException("EXTERNAL_SERVICE_ERROR", "外部服务错误: " + service);
    }

    /**
     * 创建文件上传异常
     */
    public static BusinessException fileUploadError(String reason) {
        return new BusinessException("FILE_UPLOAD_ERROR", "文件上传失败: " + reason);
    }

    /**
     * 创建文件下载异常
     */
    public static BusinessException fileDownloadError(String reason) {
        return new BusinessException("FILE_DOWNLOAD_ERROR", "文件下载失败: " + reason);
    }

    /**
     * 创建数据验证异常
     */
    public static BusinessException dataValidationError(String field) {
        return new BusinessException("DATA_VALIDATION_ERROR", "数据验证失败: " + field);
    }

    /**
     * 创建并发操作异常
     */
    public static BusinessException concurrentOperation(String operation) {
        return new BusinessException("CONCURRENT_OPERATION", "并发操作冲突: " + operation);
    }

    /**
     * 创建系统维护异常
     */
    public static BusinessException systemMaintenance() {
        return new BusinessException("SYSTEM_MAINTENANCE", "系统正在维护，请稍后再试");
    }

    /**
     * 创建配额超限异常
     */
    public static BusinessException quotaExceeded(String quotaType) {
        return new BusinessException("QUOTA_EXCEEDED", quotaType + "配额已用完");
    }

    /**
     * 创建频率限制异常
     */
    public static BusinessException rateLimitExceeded() {
        return new BusinessException("RATE_LIMIT_EXCEEDED", "请求频率超限，请稍后再试");
    }
}