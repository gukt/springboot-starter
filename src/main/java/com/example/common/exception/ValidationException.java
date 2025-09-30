package com.example.common.exception;

import java.util.Map;

/**
 * 验证异常类
 * 用于处理数据验证失败的场景
 */
public class ValidationException extends RuntimeException {

    /**
     * 验证错误详情
     */
    private final Map<String, String> errors;

    /**
     * 构造方法
     */
    public ValidationException(String message) {
        super(message);
        this.errors = Map.of();
    }

    /**
     * 构造方法
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? errors : Map.of();
    }

    /**
     * 构造方法
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = Map.of();
    }

    /**
     * 构造方法
     */
    public ValidationException(String message, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors != null ? errors : Map.of();
    }

    /**
     * 创建用户名验证失败异常
     */
    public static ValidationException usernameInvalid(String reason) {
        return new ValidationException("用户名验证失败", Map.of("username", reason));
    }

    /**
     * 创建密码验证失败异常
     */
    public static ValidationException passwordInvalid(String reason) {
        return new ValidationException("密码验证失败", Map.of("password", reason));
    }

    /**
     * 创建邮箱验证失败异常
     */
    public static ValidationException emailInvalid(String reason) {
        return new ValidationException("邮箱验证失败", Map.of("email", reason));
    }

    /**
     * 创建手机号验证失败异常
     */
    public static ValidationException phoneInvalid(String reason) {
        return new ValidationException("手机号验证失败", Map.of("phone", reason));
    }

    /**
     * 创建验证码验证失败异常
     */
    public static ValidationException captchaInvalid(String reason) {
        return new ValidationException("验证码验证失败", Map.of("captcha", reason));
    }

    /**
     * 创建文件验证失败异常
     */
    public static ValidationException fileInvalid(String fieldName, String reason) {
        return new ValidationException("文件验证失败", Map.of(fieldName, reason));
    }

    /**
     * 创建字段验证失败异常
     */
    public static ValidationException fieldInvalid(String fieldName, String reason) {
        return new ValidationException("字段验证失败", Map.of(fieldName, reason));
    }

    /**
     * 创建多个字段验证失败异常
     */
    public static ValidationException fieldsInvalid(Map<String, String> errors) {
        return new ValidationException("字段验证失败", errors);
    }

    /**
     * 创建业务规则验证失败异常
     */
    public static ValidationException businessRuleInvalid(String rule, String reason) {
        return new ValidationException("业务规则验证失败", Map.of(rule, reason));
    }

    /**
     * 获取验证错误详情
     */
    public Map<String, String> getErrors() {
        return errors;
    }

    /**
     * 是否有错误详情
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}