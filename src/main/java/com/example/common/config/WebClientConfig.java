package com.example.common.config;

import com.example.common.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static reactor.util.retry.Retry.backoff;

/**
 * WebClient 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    /**
     * 连接超时
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    /**
     * 响应超时
     */
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 读取超时
     */
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 写入超时
     */
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 标准缓冲区大小
     */
    private static final int DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024; // 2MB
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    /**
     * 初始重试延迟
     */
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(1);
    /**
     * 最大重试延迟
     */
    private static final Duration MAX_RETRY_DELAY = Duration.ofSeconds(10);
    /**
     * 重试倍数
     */
    private static final double RETRY_MULTIPLIER = 2.0;
    /**
     * 慢请求阈值 - 超过 3 秒的请求需要特别关注
     */
    private static final Duration SLOW_REQUEST_THRESHOLD = Duration.ofSeconds(3);

    /**
     * 配置 WebClient 专用的 ObjectMapper
     */
    @Bean("webClientObjectMapper")
    public ObjectMapper webClientObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .registerModule(new JavaTimeModule());
    }

    /**
     * 配置 HttpClient
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(RESPONSE_TIMEOUT)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS)))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
    }

    /**
     * 通用 WebClient 配置
     */
    @Bean
    public WebClient webClient(HttpClient httpClient, ObjectMapper webClientObjectMapper) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(buildExchangeStrategies(webClientObjectMapper))
                .defaultHeader("User-Agent", "Java/SpringBoot-WebClient")
                .filter(this::loggingFilter)
                .filter(this::retryFilter)
                .filter(this::globalErrorHandler)
                .build();
    }

    private ExchangeStrategies buildExchangeStrategies(ObjectMapper objectMapper) {
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(objectMapper, MimeTypeUtils.APPLICATION_JSON);
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(objectMapper, MimeTypeUtils.APPLICATION_JSON);

        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(WebClientConfig.DEFAULT_BUFFER_SIZE);
                    configurer.defaultCodecs().jackson2JsonEncoder(encoder);
                    configurer.defaultCodecs().jackson2JsonDecoder(decoder);
                })
                .build();
    }

    /**
     * WebClient 全局重试过滤器
     */
    private Mono<ClientResponse> retryFilter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .retryWhen(backoff(MAX_RETRY_ATTEMPTS, INITIAL_RETRY_DELAY)
                        .maxBackoff(MAX_RETRY_DELAY)
                        .multiplier(RETRY_MULTIPLIER)
                        .filter(this::shouldRetry)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.warn("重试次数已用尽，请求失败: {}", request.url());
                            return retrySignal.failure();
                        }));
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            // 5xx 错误重试
            if (ex.getStatusCode().is5xxServerError()) {
                return true;
            }
            // 429 限流错误重试，其他 4xx 错误不重试
            // NOTE Binance API 返回 429 限流错误码不要重试，否则会遭 IP 封禁
            // return ex.getStatusCode().value() == 429;
        }

        // 连接异常重试
        return throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof TimeoutException;
    }

    /**
     * 记录请求日志过滤器
     */
    private Mono<ClientResponse> loggingFilter(ClientRequest request, ExchangeFunction next) {
        logRequest(request);
        long startTime = System.currentTimeMillis();

        return next.exchange(request)
                .doOnNext(response -> {
                    long duration = System.currentTimeMillis() - startTime;

                    // 记录响应日志
                    logResponse(request, response, duration);

                    // 慢请求警告
                    if (duration > SLOW_REQUEST_THRESHOLD.toMillis()) {
                        log.warn("Slow request detected: {} {} - Duration: {}ms",
                                request.method(), request.url(), duration);
                    }
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Request failed: {} {} - Duration: {}ms - Error: {}",
                            request.method(), request.url(), duration, error.getMessage());
                });
    }

    /**
     * 记录请求日志
     */
    private void logRequest(ClientRequest request) {
        // 如果日志级别为 DEBUG，则记录详细的日志，否则记录简短的日志
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n========== WebClient Request ==========\n");
            sb.append(String.format("%s %s\n", request.method(), request.url()));

            request.headers().forEach((name, values) -> {
                if (isSensitiveHeader(name)) {
                    sb.append(String.format("%s: [Hidden]\n", name));
                } else {
                    sb.append(String.format("%s: %s\n", name, String.join(",", values)));
                }
            });

            sb.append("=======================================");
            log.debug(sb.toString());
        } else {
            log.info("Request: {} {}", request.method(), request.url());
        }
    }

    /**
     * 记录响应日志
     */
    private void logResponse(ClientRequest request, ClientResponse response, long duration) {
        // 如果日志级别为 DEBUG，则记录详细的日志，否则记录简短的日志
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n========== WebClient Response ==========\n");
            sb.append(String.format("Status: %s\n", response.statusCode()));
            sb.append(String.format("Duration: %dms\n", duration));

            response.headers().asHttpHeaders().forEach((name, values) -> {
                sb.append(String.format("%s: %s\n", name, String.join(",", values)));
            });

            sb.append("========================================");
            log.debug(sb.toString());
        } else {
            log.info("Response: {} {} - {}ms", response.statusCode().value(), request.url(), duration);
        }
    }

    /**
     * 全局错误处理
     */
    private Mono<ClientResponse> globalErrorHandler(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .flatMap(response -> {
                    String serviceName = extractServiceName(request.url());

                    if (response.statusCode().is4xxClientError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("Unknown client error")
                                .flatMap(errorBody -> Mono.error(new ExternalServiceException(
                                        serviceName,
                                        response.statusCode().value(),
                                        errorBody)));
                    } else if (response.statusCode().is5xxServerError()) {
                        return Mono.error(new ExternalServiceException(
                                serviceName,
                                response.statusCode().value(),
                                "Server error from " + serviceName));
                    }
                    return Mono.just(response);
                })
                .onErrorMap(WebClientException.class, ex -> new ExternalServiceException(
                        extractServiceName(request.url()),
                        0,
                        "Network error: " + ex.getMessage()));
    }

    /**
     * 提取服务名称
     */
    private String extractServiceName(URI uri) {
        String host = uri.getHost();
        if (host != null) {
            if (host.contains("binance"))
                return "Binance";
            // Add other service names
        }
        return "Unknown";
    }

    /**
     * 判断是否为敏感头
     */
    private boolean isSensitiveHeader(String headerName) {
        return headerName.toLowerCase().contains("authorization") ||
                headerName.toLowerCase().contains("apikey") ||
                headerName.toLowerCase().contains("token") ||
                headerName.toLowerCase().contains("secret");
    }
}