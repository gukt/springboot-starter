package com.example.common.aspect;

import com.example.common.service.LangfuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Langfuse 切面
 *
 * 用于方法级别的追踪和监控
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "langfuse.enabled", havingValue = "true")
public class LangfuseAspect {

    private final LangfuseService langfuseService;

    /**
     * 追踪所有 Service 层的方法
     */
    @Around("execution(* com.example.service.*.*(..))")
    public Object traceServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String traceId = java.util.UUID.randomUUID().toString();

        long startTime = System.currentTimeMillis();

        try {
            // 记录方法调用开始
            Map<String, Object> input = new HashMap<>();
            input.put("method", methodName);
            input.put("class", className);
            input.put("parameters", joinPoint.getArgs());

            Object result = joinPoint.proceed();

            // 记录方法调用成功
            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> output = new HashMap<>();
            output.put("result", result);
            output.put("duration", duration);
            output.put("status", "SUCCESS");

            langfuseService.createTrace(traceId, className + "." + methodName, null, input, output, null);

            return result;
        } catch (Exception e) {
            // 记录方法调用失败
            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> output = new HashMap<>();
            output.put("duration", duration);
            output.put("status", "ERROR");
            output.put("error", e.getMessage());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exception", e.getClass().getSimpleName());
            metadata.put("stackTrace", getStackTrace(e));

            langfuseService.createTrace(traceId, className + "." + methodName, null, Map.of("parameters", joinPoint.getArgs()), output, metadata);

            throw e;
        }
    }

    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 1000) { // 限制堆栈信息长度
                sb.append("...");
                break;
            }
        }
        return sb.toString();
    }
}