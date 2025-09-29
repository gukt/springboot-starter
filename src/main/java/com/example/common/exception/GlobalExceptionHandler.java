package com.example.common.exception;

import com.example.common.ApiResponse;
import com.example.util.HttpRequestUtils;
import com.fairyland.common.exception.*;
import com.fairyland.common.util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 捕获 Spring Data JPA 包装的异常
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ApiResponse<?> handleInvalidDataAccessApiUsage(InvalidDataAccessApiUsageException ex) {
        return ApiResponse.error(ErrorEnum.UNAUTHORIZED);
    }
    /**
     * 处理其他类型的异常。
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常: {}", HttpRequestUtils.generateCurlCommand(request), e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * 全局处理业务单元抛出的 BusinessException 异常。
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常", e);
        // FIXME
//        return ApiResponse.error(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理 NoResourceFoundException 异常。
     * NOTE：别导错了包，此处需要的是 ...mvc.NoResourceFoundException 而非 reactive/NoResourceFoundException。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("资源未找到: {}", e.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * 处理 HttpRequestMethodNotSupportedException 异常。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = "请求方法错误: %s for '%s'".formatted(e.getMessage(), request.getRequestURI());
        log.warn("{}", message);
        return ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.info("请求参数缺失 - {}, 参数名: {}, 参数类型: {}, URI: {}",
                getHandlerMethodInfo(request),
                e.getParameterName(),
                e.getParameterType(),
                request.getRequestURI()
        );

        return ApiResponse.error(
                HttpStatus.BAD_REQUEST,
                String.format("缺少必需的参数: %s(%s)", e.getParameterName(), e.getParameterType())
        );
    }

    /**
     * 当对带有 @Valid 注释的参数的验证失败时。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, Object> errorDetails = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorDetails.put(fieldName, errorMessage);
        });
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "Validation failed", errorDetails);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("处理方法参数校验异常: ", e);
        Map<String, Object> errorDetails = new HashMap<>();
        try {
            e.getAllErrors().forEach((error) -> {
                String fieldName = ((DefaultMessageSourceResolvable) (Objects.requireNonNull(error.getArguments())[0]))
                        .getDefaultMessage();
                String msg = error.getDefaultMessage();
                errorDetails.put(fieldName, msg);
            });
        } catch (Exception ignored) {
            log.error("组装处理方法参数校验异常信息失败: ", e);
        }
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "处理方法参数校验异常", errorDetails);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleConstraintViolationExceptions(ConstraintViolationException e, HttpServletRequest request) {
        // 使用 WARN 级别，因为这是预期内的验证失败
        log.warn("处理方法的参数校验异常 - {}, {}", getHandlerMethodInfo(request), e.getMessage());

        // 将约束违反信息转换为 Map，便于以友好格式返回给前端
        Map<String, Object> details = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            // 只获取参数名称，去掉方法名
            String paramName = path.contains(".") ?
                    path.substring(path.lastIndexOf('.') + 1) : path;
            details.put(paramName, violation.getMessage());
        });

        // 以用户友好的方式返回错误信息
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "请求参数有误，请检查", details);
    }

    /**
     * 获取处理方法的详细信息。
     * 包括 Controller 类名和方法名。
     *
     * @param request HTTP 请求对象
     * @return 处理方法信息字符串
     */
    private String getHandlerMethodInfo(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod handlerMethod) {
            return String.format("controller: %s, method: %s",
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName());
        }
        return "handler: unknown";
    }
}
