# Langfuse 集成使用指南

## 概述

本项目已经集成了 Langfuse，可以对 Spring Boot 应用进行全面的请求追踪和监控。

## 功能特性

- **请求追踪**: 自动捕获 HTTP 请求和响应
- **方法追踪**: 追踪 Service 层方法执行
- **事件记录**: 支持自定义事件记录
- **配置灵活**: 支持采样率和数据过滤
- **安全考虑**: 自动过滤敏感信息

## 配置说明

### 基础配置

在 `application.yml` 中添加以下配置：

```yaml
langfuse:
  enabled: true  # 启用 Langfuse 追踪
  host: https://cloud.langfuse.com  # Langfuse 服务器地址
  api-key: your-api-key  # Langfuse API 密钥
  secret-key: your-secret-key  # Langfuse 密钥（可选）
  project: your-project-name  # 项目名称
  user-id: system  # 用户 ID
  session-id: session-id  # 会话 ID（可选）
  tags: api  # 标签
```

### 追踪配置

```yaml
langfuse:
  trace:
    enabled: true  # 启用请求追踪
    sample-rate: 1.0  # 采样率 (0.0-1.0)
    include-request-headers: true  # 包含请求头
    include-response-headers: true  # 包含响应头
    include-request-body: true  # 包含请求体
    include-response-body: true  # 包含响应体
    max-body-size: 1024  # 最大记录 body 大小 (KB)
```

## 使用方法

### 1. 自动 HTTP 请求追踪

启动应用后，所有 HTTP 请求都会被自动追踪并发送到 Langfuse。

### 2. 手动事件记录

```java
@Autowired
private LangfuseService langfuseService;

// 记录自定义事件
Map<String, Object> eventData = new HashMap<>();
eventData.put("action", "user_login");
eventData.put("userId", "123");
eventData.put("success", true);

langfuseService.createEvent("user_login", eventData);
```

### 3. 手动追踪记录

```java
// 记录自定义追踪
String traceId = UUID.randomUUID().toString();
Map<String, Object> input = Map.of("userId", "123", "action", "get_profile");
Map<String, Object> output = Map.of("status", "success", "profile", userProfile);

langfuseService.createTrace(traceId, "Get User Profile", "123", input, output, null);
```

### 4. 测试端点

项目提供了测试端点来验证 Langfuse 集成：

```bash
# 测试事件记录
curl -X POST http://localhost:8080/api/langfuse/test-event \
  -H "Content-Type: application/json" \
  -d '{"action": "test", "data": "example"}'

# 测试追踪记录
curl -X POST http://localhost:8080/api/langfuse/test-trace \
  -H "Content-Type: application/json" \
  -d '{"input": "test data", "metadata": {"source": "test"}}'

# 检查状态
curl http://localhost:8080/api/langfuse/status
```

## 环境变量配置

可以通过环境变量来配置 Langfuse：

```bash
export LANGFUSE_ENABLED=true
export LANGFUSE_HOST=https://cloud.langfuse.com
export LANGFUSE_API_KEY=your-api-key
export LANGFUSE_SECRET_KEY=your-secret-key
export LANGFUSE_PROJECT=your-project
export LANGFUSE_TRACE_SAMPLE_RATE=0.1
```

## 注意事项

1. **性能影响**: 追踪功能会对性能产生一定影响，建议在生产环境中设置合适的采样率
2. **敏感信息**: 系统会自动过滤敏感的请求头（如 Authorization、Cookie 等）
3. **数据大小**: 大型请求/响应体会被截断，避免传输过多数据
4. **网络连接**: 确保 Langfuse 服务器可以正常访问

## 故障排除

### 1. 追踪数据没有发送到 Langfuse

检查以下配置：
- `langfuse.enabled` 是否设置为 `true`
- `langfuse.api-key` 是否正确
- 网络连接是否正常
- 查看应用日志中的错误信息

### 2. 性能问题

- 降低采样率：`langfuse.trace.sample-rate=0.1`
- 禁用请求/响应体记录：`langfuse.trace.include-request-body=false`
- 减小最大 body 大小：`langfuse.trace.max-body-size=512`

### 3. 数据格式问题

- 确保 `application.yml` 配置格式正确
- 检查 Langfuse 服务器配置是否匹配

## 扩展功能

### 自定义过滤器

可以通过实现自定义过滤器来扩展追踪功能：

```java
@Component
public class CustomLangfuseFilter {

    @Autowired
    private LangfuseService langfuseService;

    public void traceCustomEvent(String eventName, Map<String, Object> data) {
        // 自定义追踪逻辑
        langfuseService.createEvent(eventName, data);
    }
}
```

### 集成其他监控工具

Langfuse 可以与其他监控工具（如 Prometheus、Grafana）集成，提供更全面的监控解决方案。