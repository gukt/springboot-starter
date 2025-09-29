package com.example.common.exception;

import lombok.Getter;

/**
 * 外部服务异常
 * 用于处理调用外部服务失败的场景
 */
@Getter
public class ExternalServiceException extends RuntimeException {

    /**
     * 服务名称
     */
    private final String serviceName;

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * HTTP状态码
     */
    private final Integer httpStatus;

    /**
     * 构造方法
     */
    public ExternalServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = "EXTERNAL_SERVICE_ERROR";
        this.httpStatus = null;
    }

    /**
     * 构造方法
     */
    public ExternalServiceException(String serviceName, String message, String errorCode) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.httpStatus = null;
    }

    /**
     * 构造方法
     */
    public ExternalServiceException(String serviceName, String message, Integer httpStatus) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = "EXTERNAL_SERVICE_ERROR";
        this.httpStatus = httpStatus;
    }

    /**
     * 构造方法
     */
    public ExternalServiceException(String serviceName, String message, String errorCode, Integer httpStatus) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * 构造方法
     */
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = "EXTERNAL_SERVICE_ERROR";
        this.httpStatus = null;
    }

    /**
     * 构造方法（兼容旧的API）
     */
    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = "EXTERNAL_SERVICE_ERROR";
        this.httpStatus = statusCode;
    }

    /**
     * 创建邮件服务异常
     */
    public static ExternalServiceException emailService(String message) {
        return new ExternalServiceException("EmailService", message);
    }

    /**
     * 创建短信服务异常
     */
    public static ExternalServiceException smsService(String message) {
        return new ExternalServiceException("SmsService", message);
    }

    /**
     * 创建支付服务异常
     */
    public static ExternalServiceException paymentService(String message) {
        return new ExternalServiceException("PaymentService", message);
    }

    /**
     * 创建文件存储服务异常
     */
    public static ExternalServiceException fileStorageService(String message) {
        return new ExternalServiceException("FileStorageService", message);
    }

    /**
     * 创建推送服务异常
     */
    public static ExternalServiceException pushNotificationService(String message) {
        return new ExternalServiceException("PushNotificationService", message);
    }

    /**
     * 创建第三方登录服务异常
     */
    public static ExternalServiceException oauthService(String provider, String message) {
        return new ExternalServiceException("OAuthService[" + provider + "]", message);
    }

    /**
     * 创建地图服务异常
     */
    public static ExternalServiceException mapService(String message) {
        return new ExternalServiceException("MapService", message);
    }

    /**
     * 创建翻译服务异常
     */
    public static ExternalServiceException translationService(String message) {
        return new ExternalServiceException("TranslationService", message);
    }

    /**
     * 创建AI服务异常
     */
    public static ExternalServiceException aiService(String message) {
        return new ExternalServiceException("AIService", message);
    }

    /**
     * 创建CDN服务异常
     */
    public static ExternalServiceException cdnService(String message) {
        return new ExternalServiceException("CDNService", message);
    }

    /**
     * 创建数据库服务异常
     */
    public static ExternalServiceException databaseService(String message) {
        return new ExternalServiceException("DatabaseService", message);
    }

    /**
     * 创建缓存服务异常
     */
    public static ExternalServiceException cacheService(String message) {
        return new ExternalServiceException("CacheService", message);
    }

    /**
     * 创建消息队列服务异常
     */
    public static ExternalServiceException messageQueueService(String message) {
        return new ExternalServiceException("MessageQueueService", message);
    }

    /**
     * 创建搜索引擎服务异常
     */
    public static ExternalServiceException searchEngineService(String message) {
        return new ExternalServiceException("SearchEngineService", message);
    }

    /**
     * 创建日志服务异常
     */
    public static ExternalServiceException logService(String message) {
        return new ExternalServiceException("LogService", message);
    }

    /**
     * 创建监控服务异常
     */
    public static ExternalServiceException monitoringService(String message) {
        return new ExternalServiceException("MonitoringService", message);
    }

    /**
     * 创建API网关服务异常
     */
    public static ExternalServiceException apiGatewayService(String message) {
        return new ExternalServiceException("APIGatewayService", message);
    }

    /**
     * 创建HTTP请求异常
     */
    public static ExternalServiceException httpRequest(String url, Integer httpStatus, String message) {
        return new ExternalServiceException("HttpRequest[" + url + "]", message, httpStatus);
    }

    /**
     * 创建超时异常
     */
    public static ExternalServiceException timeout(String serviceName) {
        return new ExternalServiceException(serviceName, "请求超时", "TIMEOUT_ERROR");
    }

    /**
     * 创建连接异常
     */
    public static ExternalServiceException connectionError(String serviceName) {
        return new ExternalServiceException(serviceName, "连接失败", "CONNECTION_ERROR");
    }

    /**
     * 创建限流异常
     */
    public static ExternalServiceException rateLimited(String serviceName) {
        return new ExternalServiceException(serviceName, "请求频率超限", "RATE_LIMITED");
    }

    /**
     * 创建服务不可用异常
     */
    public static ExternalServiceException serviceUnavailable(String serviceName) {
        return new ExternalServiceException(serviceName, "服务不可用", "SERVICE_UNAVAILABLE", 503);
    }

    /**
     * 创建认证失败异常
     */
    public static ExternalServiceException authenticationFailed(String serviceName) {
        return new ExternalServiceException(serviceName, "认证失败", "AUTHENTICATION_FAILED", 401);
    }

    /**
     * 创建权限不足异常
     */
    public static ExternalServiceException authorizationFailed(String serviceName) {
        return new ExternalServiceException(serviceName, "权限不足", "AUTHORIZATION_FAILED", 403);
    }
}