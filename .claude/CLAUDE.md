# Spring Boot 脚手架项目 - Claude 上下文

## 项目识别

**项目类型**: Spring Boot 企业级脚手架项目

**识别特征**:
- 构建文件: `build.gradle` (Gradle Groovy DSL)
- Spring Boot 版本: 3.5.6
- Java 版本: JDK 17
- 包结构: `com.example.demo`
- 启动类: `DemoApplication.java`

## 技术栈背景

@~/.claude/tech-stacks/springboot-profile.md

## 通用开发准则

@~/.claude/tech-stacks/common-principles.md

## 项目特定信息

### 项目概述
这是一个精简但功能完整的 Spring Boot 脚手架项目，专注于高频场景，避免过度设计。适合快速启动单体应用开发。

### 核心特性
- ✅ Spring Boot 3.5.6 + JDK 17 + Gradle
- ✅ MySQL + JPA + HikariCP + Flyway
- ✅ Spring Security + JWT 认证
- ✅ Redis + Caffeine 缓存
- ✅ 全局异常处理 + 统一响应格式
- ✅ Swagger/OpenAPI 文档
- ✅ DevTools 热更新
- ✅ 完整的测试框架

### 项目架构
- **分层架构**: Controller → Service → Repository → Domain
- **包结构**: 遵循 Spring 官方推荐
- **命名规范**: domain 包替代传统的 entity 包
- **配置分离**: 多环境配置支持

### 关键配置
- **数据库**: MySQL 8+ with Flyway 迁移
- **缓存**: Redis 主缓存 + Caffeine 本地缓存
- **认证**: JWT 双 Token 机制
- **序列化**: StringRedisSerializer + Jackson2JsonRedisSerializer

### 开发约定
- **异常处理**: 使用 GlobalExceptionHandler 统一处理
- **响应格式**: 标准化的 ApiResponse 结构
- **数据验证**: Spring Validation 注解
- **日志规范**: Logback 配置

### 测试策略
- **单元测试**: JUnit 5 + Mockito
- **集成测试**: Spring Boot Test + H2 内存数据库
- **API 测试**: MockMvc + WebTestClient
- **测试覆盖率**: 目标 80%+

### 部署方式
- **Docker**: 多阶段构建 + docker-compose
- **JVM 优化**: G1GC + 内存配置
- **健康检查**: Spring Boot Actuator
- **监控**: Micrometer + 指标收集

## 当前任务状态

### 已完成 ✅
- [x] 全局 memory 系统建设
- [x] 项目技术栈文档
- [x] README.md 项目文档
- [x] 项目级 CLAUDE.md 配置

### 待实现 🔄
- [ ] 完善的配置文件 (application.yml 等)
- [ ] 基础实体类和 CRUD 操作
- [ ] JWT 认证完整实现
- [ ] 全局异常处理机制
- [ ] Redis 缓存配置
- [ ] Swagger 文档配置
- [ ] 示例 Controller 和 Service
- [ ] 完整的测试用例
- [ ] Docker 配置文件

## 开发注意事项

### 代码规范
- 严格遵守 Spring Boot 官方最佳实践
- 使用 Lombok 减少样板代码，优先使用 @Data 注解，根据需要使用 @Getter、@Setter
- 保持简洁的包结构
- 避免过度工程化

### Lombok 使用规范
- 实体类优先使用 @Data 注解（包含 @Getter、@Setter、@ToString、@EqualsAndHashCode、@RequiredArgsConstructor）
- DTO 类根据需求使用 @Data 或单独的 @Getter、@Setter
- Service 和 Controller 类使用 @RequiredArgsConstructor 进行依赖注入
- 避免在实体类中使用 @Builder，除非有特殊的构建需求

### 注释规范
- **禁止在类注释中添加作者和版本信息**：不使用 `@author` 和 `@version` 标签
- 类注释应该专注于描述类的用途和功能，而不是维护信息
- 方法注释应该描述方法的功能、参数和返回值
- 使用 Git 进行版本控制和作者信息管理，而非代码注释
- 示例：
  ```java
  /**
   * 软删除接口定义
   *
   * 提供软删除相关的基础操作，支持灵活的功能组合
   */
  ```

### 中文排版规范
- 中文与英文或数字混排时，中文与英文或数字之间加空格
- 示例："使用 JWT 进行认证" 而不是 "使用JWT进行认证"
- 示例："Spring Boot 3.5.6 版本" 而不是 "Spring Boot3.5.6版本"

### 安全考虑
- 所有 API 接口都需要认证
- 密码必须加密存储
- 使用 HTTPS 生产环境
- 定期更新依赖版本

### 性能优化
- 合理使用缓存策略
- 优化数据库查询
- 使用异步处理耗时操作
- 监控应用性能指标

## 与 Claude 协作提示

当在这个项目中工作时，请：
1. 参考 Spring Boot 技术栈背景文档
2. 遵循通用开发准则
3. 保持项目结构的一致性
4. 优先考虑简单实用的解决方案
5. 避免添加不必要的企业级复杂度

## 更新记录

- 2024-01-01: 初始化项目级 CLAUDE.md
- 2024-01-01: 完成全局 memory 系统建设