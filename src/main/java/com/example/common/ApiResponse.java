package com.example.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;

import com.example.common.exception.ErrorEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应格式
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 简单的成功响应，返回字符串 OK */
    public static final ApiResponse<String> OK = ApiResponse.of("OK");

    /**
     * 错误响应体的结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {

        /**
         * 错误代码，用于标识错误类型
         */
        @Schema(description = "错误代码")
        private String code;

        /**
         * 错误描述，用于描述错误的原因
         */
        @Schema(description = "错误描述")
        private String message;

        /**
         * 错误详情（可选），用于描述错误的更具体的信息
         */
        @Schema(description = "错误详情")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private Map<String, Object> details;
    }

    /**
     * 成功时返回的数据，可以是对象、数组或者字符串
     */
    @Schema(description = "成功时返回的数据，可能是对象、数组或者字符串")
    private T data;

    /**
     * 失败时返回的错误信息。
     */
    @Schema(description = "失败时返回的错误信息")
    private Error error = null;

    /**
     * 额外的元数据（可选），比如：包含分页信息、总记录数等。
     */
    @Schema(description = "额外的元数据，比如：包含分页信息、总记录数等")
    private Map<String, Object> meta = null;

    // Static Methods

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> of(T data, Map<String, Object> meta) {
        ApiResponse<T> instance = new ApiResponse<>();
        instance.data = data;
        if (Objects.isNull(data)) {
            instance.data = (T) new HashMap<>();
        }
        instance.meta = meta;
        return instance;
    }

    public static <T> ApiResponse<T> of(T data) {
        return of(data, null);
    }

    public static ApiResponse<Void> of(Error error) {
        ApiResponse<Void> instance = new ApiResponse<>();
        instance.error = error;
        return instance;
    }

    public static ApiResponse<Void> error(String code, String message, Map<String, Object> details) {
        return of(new Error(code, message, details));
    }

    public static ApiResponse<Void> error(String code, String message) {
        return error(code, message, null);
    }

    public static ApiResponse<Void> error(HttpStatus httpStatus, String message, Map<String, Object> details) {
        return of(new Error(String.valueOf(httpStatus.value()), message, details));
    }

    public static ApiResponse<Void> error(HttpStatus httpStatus, String message) {
        return error(String.valueOf(httpStatus.value()), message);
    }

    public static ApiResponse<?> error(ErrorEnum error) {
        return error(error.getCode(), error.getMessage());
    }

}