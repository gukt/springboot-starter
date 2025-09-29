package com.example.common.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 * 统一处理各种异常并返回标准化的响应格式
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

        private final ObjectMapper objectMapper;

        /**
         * 处理业务异常
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {
                log.warn("Business exception occurred: {} - {} | Path: {} | IP: {}",
                                ex.getErrorCode(), ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                ex.getMessage()));
        }

        /**
         * 处理资源不存在异常
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                log.warn("Resource not found: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(
                                                HttpStatus.NOT_FOUND.value(),
                                                ex.getMessage()));
        }

        /**
         * 处理认证异常
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
                        AuthenticationException ex,
                        HttpServletRequest request) {
                log.warn("Authentication failed: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "认证失败: " + ex.getMessage()));
        }

        /**
         * 处理权限不足异常
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                log.warn("Access denied: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                                HttpStatus.FORBIDDEN.value(),
                                                "权限不足: " + ex.getMessage()));
        }

        /**
         * 处理参数验证异常（@Valid）
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                fieldError -> fieldError.getDefaultMessage() != null
                                                                ? fieldError.getDefaultMessage()
                                                                : "参数验证失败"));

                log.warn("Validation failed: {} | Path: {} | IP: {} | Errors: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "参数验证失败",
                                                errors));
        }

        /**
         * 处理参数绑定异常
         */
        @ExceptionHandler(BindException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
                        BindException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                fieldError -> fieldError.getDefaultMessage() != null
                                                                ? fieldError.getDefaultMessage()
                                                                : "参数绑定失败"));

                log.warn("Bind exception: {} | Path: {} | IP: {} | Errors: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "参数绑定失败",
                                                errors));
        }

        /**
         * 处理约束验证异常
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = ex.getConstraintViolations()
                                .stream()
                                .collect(Collectors.toMap(
                                                violation -> violation.getPropertyPath().toString(),
                                                ConstraintViolation::getMessage));

                log.warn("Constraint violation: {} | Path: {} | IP: {} | Errors: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "参数验证失败",
                                                errors));
        }

        /**
         * 处理缺少请求参数异常
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
                        MissingServletRequestParameterException ex,
                        HttpServletRequest request) {
                log.warn("Missing request parameter: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "缺少必需的请求参数: " + ex.getParameterName()));
        }

        /**
         * 处理参数类型不匹配异常
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                String message = String.format("参数 '%s' 类型不匹配，期望类型: %s",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");

                log.warn("Method argument type mismatch: {} | Path: {} | IP: {}",
                                message, request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                message));
        }

        /**
         * 处理404异常（控制器不存在）
         */
        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
                        NoHandlerFoundException ex,
                        HttpServletRequest request) {
                log.warn("No handler found: {} {} | Path: {} | IP: {}",
                                ex.getHttpMethod(), ex.getRequestURL(), request.getRequestURI(),
                                get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(
                                                HttpStatus.NOT_FOUND.value(),
                                                "请求的资源不存在"));
        }

        /**
         * 处理请求方法不支持异常
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {
                log.warn("Method not supported: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                String message = String.format("请求方法 '%s' 不支持，支持的请求方法: %s",
                                ex.getMethod(),
                                ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.joining(", ")) : "无");

                return ResponseEntity
                                .status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(ApiResponse.error(
                                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                                message));
        }

        /**
         * 处理媒体类型不支持异常
         */
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException ex,
                        HttpServletRequest request) {
                log.warn("Media type not supported: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                String message = String.format("不支持的媒体类型: %s，支持的媒体类型: %s",
                                ex.getContentType(),
                                ex.getSupportedMediaTypes() != null ? ex.getSupportedMediaTypes().stream()
                                                .map(Object::toString)
                                                .collect(Collectors.joining(", ")) : "无");

                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(ApiResponse.error(
                                                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                                                message));
        }

        /**
         * 处理非法参数异常
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                log.warn("Illegal argument: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request));

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "参数错误: " + ex.getMessage()));
        }

        /**
         * 处理空指针异常
         */
        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ApiResponse<Void>> handleNullPointerException(
                        NullPointerException ex,
                        HttpServletRequest request) {
                log.error("Null pointer exception: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "服务器内部错误"));
        }

        /**
         * 处理运行时异常
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
                        RuntimeException ex,
                        HttpServletRequest request) {
                log.error("Runtime exception: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "服务器内部错误: " + ex.getMessage()));
        }

        /**
         * 处理通用异常
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleException(
                        Exception ex,
                        HttpServletRequest request) {
                log.error("Unexpected exception: {} | Path: {} | IP: {}",
                                ex.getMessage(), request.getRequestURI(), get_client_ip(request), ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "服务器内部错误"));
        }

        /**
         * 获取客户端IP地址
         */
        private String get_client_ip(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                        return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                        return xRealIp;
                }

                return request.getRemoteAddr();
        }
}