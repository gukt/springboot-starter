package com.example.common.exception;

import com.example.common.ApiResponse;
import lombok.Getter;

/**
 * 错误枚举。
 */
@Getter
public enum ErrorEnum {

    // 系统级错误 (1000-1999)

    INTERNAL_SERVER_ERROR("1000", "服务器内部错误"),
    SERVICE_UNAVAILABLE("1001", "服务不可用"),
    NOT_IMPLEMENTED("1002", "功能暂未实现"),
    REQUEST_TIMEOUT("1003", "请求超时"),
    TOO_MANY_REQUESTS("1004", "请求过于频繁，请稍后再试"),
    NETWORK_ERROR("1005", "网络连接异常"),
    DATABASE_ERROR("1006", "数据库操作异常"),
    REDIS_ERROR("1007", "缓存服务异常"),

    // 请求相关错误 (1100-1199)

    BAD_REQUEST("1100", "请求格式错误"),
    METHOD_NOT_ALLOWED("1101", "请求方法不支持"),
    UNSUPPORTED_MEDIA_TYPE("1102", "不支持的媒体类型"),
    REQUEST_ENTITY_TOO_LARGE("1103", "请求实体过大"),

    // 数据验证错误 (1200-1299)

    INVALID_PARAM("1200", "参数格式错误"),
    MISSING_PARAM("1201", "缺少必需参数"),
    VALIDATION_FAILED("1202", "参数验证失败"),
    ILLEGAL_ARGUMENT("1203", "参数值不合法"),
    DATA_NOT_FOUND("1204", "数据不存在"),
    DATA_ALREADY_EXISTS("1205", "数据已存在"),
    DATA_CONFLICT("1206", "数据冲突"),
    VERSION_CONFLICT("1207", "数据版本冲突"),

    // 认证授权错误 (2000-2999)

    UNAUTHORIZED("2000", "未授权访问"),
    FORBIDDEN("2001", "禁止访问"),
    INVALID_TOKEN("2002", "无效的访问令牌"),
    TOKEN_EXPIRED("2003", "访问令牌已过期"),
    TOKEN_MISSING("2004", "缺少访问令牌"),
    REFRESH_TOKEN_EXPIRED("2005", "刷新令牌已过期"),
    INSUFFICIENT_PRIVILEGES("2006", "权限不足"),
    ACCOUNT_DISABLED("2007", "账户已被禁用"),
    ACCOUNT_LOCKED("2008", "账户已被锁定"),
    SESSION_EXPIRED("2009", "会话已过期"),

    // 用户账号相关错误 (2100-2199)

    USER_NOT_FOUND("2100", "用户不存在"),
    USER_NOT_LOGIN("2101", "用户未登录"),
    USER_ALREADY_EXISTS("2102", "用户已存在"),
    USER_INVALID_CREDENTIALS("2103", "用户名或密码错误"),
    USER_PASSWORD_EXPIRED("2104", "密码已过期"),
    USER_EMAIL_NOT_VERIFIED("2106", "邮箱未验证"),
    USER_PHONE_NOT_VERIFIED("2107", "手机号未验证"),

    // 第三方服务错误 (5000-5999)

    THIRD_PARTY_SERVICE_ERROR("5000", "第三方服务异常"),
    THIRD_PARTY_API_LIMIT_EXCEEDED("5001", "第三方 API 调用次数超限"),
    THIRD_PARTY_AUTHENTICATION_FAILED("5002", "第三方服务认证失败")

    // Add more...
    ;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误描述
     */
    private final String message;

    ErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

}