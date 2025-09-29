package com.example.common;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局响应体处理器。
 * <p>
 * <a href="https://stackoverflow.com/a/64940739/21335614">@ControllerAdvice does not allow Swagger UI to be displayed</a>
 */
@RestControllerAdvice(basePackages = "com.fairyland")
@Slf4j
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 前置检查，以决定 beforeBodyWrite 方法是否要被执行。
     *
     * @param returnType    the return type
     * @param converterType the selected converter type
     * @return true if the beforeBodyWrite method should be executed; false otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getGenericParameterType().toString().contains("springfox")) {
            return false;
        }

        // 如果是 ResponseEntity 类型，则不处理。
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        //        return true; // for testing only.

        Method method = (Method) returnType.getExecutable();
        boolean isBasisErrorHandler = "error".equals(method.getName())
                && method.getDeclaringClass().equals(BasicErrorController.class);
        return !isBasisErrorHandler;
    }

    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {
        // 以下类型，直接返回不做处理。
        if (body instanceof ApiResponse
                || body instanceof Resource
                // 用于直接返回的结果，比如 byte[] 类型（直接返回响应流）。
                // || body instanceof byte[]
                || body instanceof MappingJacksonValue) {
            return body;
        }
        if (selectedContentType.isCompatibleWith(MediaType.TEXT_PLAIN)) {
            return body;
        }
        // 如果返回值是 Page 类型，则包装成 JsonPage 返回。
        if (body instanceof Page<?> paging) {
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", paging.getNumber());
            pagination.put("pageSize", paging.getSize());
            pagination.put("pageCount", paging.getTotalPages());
            pagination.put("total", paging.getTotalElements());

            return ApiResponse.success(paging.getContent(), Map.of("pagination", pagination));
        }
        // 如果方法上有 @JsonView 注解，则根据查询字符串中的 view 参数的值，动态设置序列化视图。
        JsonView jsonView = returnType.getMethodAnnotation(JsonView.class);
        if (jsonView != null) {
            // 从查询字符串中获取 view 参数的值。
            String viewName = getParameterValue(request.getURI().getQuery(), "view");
            if (viewName != null) {
                return JsonViews.wrap(body, viewName);
            }
        }
        // 由于前面已经判断了如果是 ApiResult 就直接返回，因此这里需要将所有返回值包装成 ApiResult 类型。
        return ApiResponse.success(body);
    }

    // Helpers

    /**
     * 从查询字符串中解析出参数值。
     *
     * @param query     the query string
     * @param paramName the parameter name
     * @return the parameter value
     */
    private String getParameterValue(String query, String paramName) {
        if (query == null || paramName == null) {
            return null;
        }
        Pattern regex = Pattern.compile(".*" + paramName + "=([^&]+)&?.*");
        Matcher m = regex.matcher(query);
        return m.find() ? m.group(1) : null;
    }
}
