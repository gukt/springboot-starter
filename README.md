# Spring Boot 脚手架项目

## 项目简介

这是一个功能完整的 Spring Boot 3.5.6 企业级项目脚手架，专注于高频场景，避免过度设计，适合快速启动单体应用开发。项目遵循 Spring 官方推荐的项目结构，集成了常用的企业级功能组件。

## 项目特性

### 🚀 快速开发
- 标准化的项目结构，遵循 Spring 官方推荐
- 预配置的常用组件，开箱即用
- 统一的异常处理和响应格式
- 完整的 API 文档支持

### 🔒 安全特性
- JWT 双 Token 认证机制（Access Token + Refresh Token）
- 密码 BCrypt 加密存储
- CORS 跨域配置
- 细粒度的接口权限控制

### 🚀 性能优化
- Redis + Caffeine 多级缓存策略
- HikariCP 高性能数据库连接池
- 异步处理支持
- 分页查询优化

### 🛠️ 开发工具
- Swagger/OpenAPI 3.0 自动生成 API 文档
- Spring Boot DevTools 热更新
- 完整的单元测试和集成测试
- Docker 容器化支持

## 技术栈

### 核心技术

- **Spring Boot**: 3.5.6
- **Java**: JDK 17
- **构建工具**: Gradle (Groovy DSL)
- **包管理**: Gradle 依赖管理

### 数据存储

- **数据库**: MySQL 8+
- **连接池**: HikariCP
- **ORM**: JPA/Hibernate
- **数据库迁移**: Flyway

### 缓存方案

- **本地缓存**: Caffeine
- **分布式缓存**: Redis
- **序列化**: StringRedisSerializer + Jackson2JsonRedisSerializer

### 安全认证

- **安全框架**: Spring Security
- **认证方案**: JWT (Access Token + Refresh Token)
- **密码加密**: BCryptPasswordEncoder

### 开发支持

- **API 文档**: Swagger/OpenAPI 3.0
- **热更新**: Spring Boot DevTools
- **数据验证**: Spring Validation
- **日志框架**: Logback

### 测试框架

- **单元测试**: JUnit 5 + Mockito
- **集成测试**: Spring Boot Test
- **内存数据库**: H2
- **测试容器**: TestContainers

## 项目特性

### 🚀 快速开发

- 标准化的项目结构
- 预配置的常用组件
- 统一的异常处理
- 标准化的响应格式

### 🔒 安全特性

- JWT 认证机制
- 密码加密存储
- CORS 跨域配置
- 接口权限控制

### 🚀 性能优化

- 多级缓存策略
- 数据库连接池
- 异步处理支持
- 分页查询优化

### 🛠️ 开发工具

- Swagger API 文档
- 开发环境热更新
- 完整的测试支持
- Docker 容器化

## 项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java              # 启动类
├──
├── config/                          # 所有配置类
│   ├── SecurityConfig.java          # 安全配置
│   ├── RedisConfig.java             # Redis 配置
│   ├── WebConfig.java               # Web 配置
│   ├── SwaggerConfig.java           # Swagger 配置
│   └── JpaConfig.java               # JPA 配置
├──
├── controller/                      # 控制器层
│   ├── AuthController.java          # 认证接口
│   └── UserController.java          # 用户管理接口
├──
├── service/                         # 服务层
│   ├── AuthService.java              # 认证服务
│   └── UserService.java              # 用户服务
├──
├── repository/                      # 数据访问层
│   ├── UserRepository.java          # 用户数据访问
│   └── RoleRepository.java          # 角色数据访问
├──
├── domain/                          # 领域模型（实体）
│   ├── User.java                    # 用户实体
│   ├── Role.java                    # 角色实体
│   └── BaseEntity.java              # 基础实体
├──
├── dto/                             # 数据传输对象
│   ├── LoginRequest.java            # 登录请求
│   ├── LoginResponse.java           # 登录响应
│   ├── UserDto.java                 # 用户信息
│   └── PageDto.java                 # 分页请求
├──
├── exception/                       # 异常处理
│   ├── GlobalExceptionHandler.java  # 全局异常处理
│   ├── BusinessException.java        # 业务异常
│   └── ValidationException.java      # 验证异常
├──
├── util/                            # 工具类
│   ├── JwtUtil.java                 # JWT 工具
│   ├── DateUtil.java                # 日期工具
│   ├── EncryptionUtil.java          # 加密工具
│   └── PageUtil.java                # 分页工具
└──
├── common/                          # 通用类
    ├── ApiResponse.java             # 统一响应格式
    ├── ApiError.java               # 错误信息
    └── PageResult.java             # 分页结果

src/main/resources/
├── application.yml                  # 主配置文件
├── application-dev.yml             # 开发环境配置
├── application-prod.yml            # 生产环境配置
├── db/migration/                    # 数据库迁移文件
│   ├── V1__Create_tables.sql       # 建表语句
│   └── V2__Insert_init_data.sql     # 初始数据
└── logback-spring.xml              # 日志配置

src/test/java/                       # 测试代码
├── controller/                      # 控制器测试
├── service/                         # 服务测试
└── repository/                      # 数据访问测试

docker/                              # Docker 配置
├── Dockerfile                       # 应用镜像
└── docker-compose.yml               # 开发环境编排
```

## 快速开始

### 环境要求

- **Java**: JDK 17+
- **Gradle**: 7.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Docker**: 20.0+ (可选)

### 安装步骤

1. **克隆项目**

   ```bash
   git clone <repository-url>
   cd springboot-starter
   ```

2. **配置环境变量**

   ```bash
   cp .env.example .env
   # 编辑 .env 文件，配置数据库连接等信息
   ```

3. **启动依赖服务**

   ```bash
   # 使用 Docker Compose 启动 MySQL 和 Redis
   docker-compose -f docker/docker-compose.yml up -d
   ```

4. **构建项目**

   ```bash
   ./gradlew build
   ```

5. **运行应用**

   ```bash
   # 开发环境
   ./gradlew bootRun

   # 或者直接运行 JAR 文件
   java -jar build/libs/springboot-starter-0.0.1-SNAPSHOT.jar
   ```

6. **访问应用**
   - 应用地址: http://localhost:8080
   - Swagger 文档: http://localhost:8080/swagger-ui.html
   - 健康检查: http://localhost:8080/actuator/health

### 环境配置

#### 开发环境配置 (application-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/springboot_starter_dev
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

logging:
  level:
    com.example.demo: DEBUG
    org.springframework.security: DEBUG
```

#### 生产环境配置 (application-prod.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}

logging:
  level:
    com.example.demo: INFO
    org.springframework.security: WARN
```

## API 文档

### 认证接口

#### 用户登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

**响应示例**:

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com"
    }
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

#### Token 刷新

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 用户管理接口

#### 获取用户列表

```http
GET /api/users?page=0&size=10&sort=username,asc
Authorization: Bearer <access-token>
```

#### 获取用户详情

```http
GET /api/users/{id}
Authorization: Bearer <access-token>
```

#### 创建用户

```http
POST /api/users
Content-Type: application/json
Authorization: Bearer <access-token>

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "role": "USER"
}
```

## 开发指南

### 代码规范

#### 命名约定

- **包名**: 小写，使用 domain.com reverse
- **类名**: 大驼峰命名法 (PascalCase)
- **方法名**: 小驼峰命名法 (camelCase)
- **常量**: 全大写，下划线分隔
- **数据库表**: 小写，下划线分隔

#### 注解使用

- **Controller**: @RestController
- **Service**: @Service
- **Repository**: @Repository
- **实体**: @Entity, @Table
- **配置**: @Configuration

### 数据库迁移

项目使用 Flyway 进行数据库版本管理：

1. **创建迁移文件**

   - 文件位置: `src/main/resources/db/migration/`
   - 文件命名: `V{version}__{description}.sql`
   - 示例: `V1__Create_user_tables.sql`

2. **迁移文件示例**

   ```sql
   -- V1__Create_user_tables.sql
   CREATE TABLE users (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(50) UNIQUE NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       role VARCHAR(20) NOT NULL DEFAULT 'USER',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       is_active BOOLEAN DEFAULT TRUE
   );

   CREATE TABLE roles (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(50) UNIQUE NOT NULL,
       description TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

### 缓存使用

#### 基本缓存操作

```java
@Service
public class UserService {

    @Cacheable(value = "user", key = "#id")
    public UserDto getUserById(Long id) {
        // 从数据库查询用户
        return userRepository.findById(id)
               .map(this::convertToDto)
               .orElseThrow(() -> new UserNotFoundException(id));
    }

    @CacheEvict(value = "user", key = "#id")
    public void updateUser(Long id, UserDto userDto) {
        // 更新用户信息，同时清除缓存
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        userRepository.save(user);
    }

    @CacheEvict(value = "user", allEntries = true)
    public void clearAllUserCache() {
        // 清除所有用户缓存
    }
}
```

#### Redis 字符串操作

```java
@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void setStringValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String getStringValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void setStringValueWithExpire(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }
}
```

### 异常处理

#### 自定义异常

```java
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super(String.format("用户未找到: %d", userId), "USER_NOT_FOUND");
    }
}
```

#### 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "数据验证失败", errors));
    }
}
```

### 测试指南

#### 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // 准备测试数据
        UserDto userDto = new UserDto("testuser", "test@example.com", "password123");
        User savedUser = new User(1L, "testuser", "test@example.com", "encoded_password");

        // 模拟依赖行为
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 执行测试
        UserDto result = userService.createUser(userDto);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }
}
```

#### 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getUserById_Success() {
        // 准备测试数据
        User user = new User("testuser", "test@example.com", "password");
        user = userRepository.save(user);

        // 执行测试
        ResponseEntity<UserDto> response = restTemplate.getForEntity(
                "/api/users/" + user.getId(),
                UserDto.class
        );

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }
}
```

## 部署指南

### Docker 部署

1. **构建 Docker 镜像**

   ```bash
   docker build -t springboot-starter:latest .
   ```

2. **运行 Docker 容器**
   ```bash
   docker run -p 8080:8080 \
          -e SPRING_PROFILES_ACTIVE=prod \
          -e DB_HOST=mysql \
          -e DB_PORT=3306 \
          -e DB_NAME=springboot_starter \
          -e DB_USERNAME=root \
          -e DB_PASSWORD=password \
          -e REDIS_HOST=redis \
          -e REDIS_PORT=6379 \
          springboot-starter:latest
   ```

### Docker Compose 部署

```bash
# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 查看日志
docker-compose -f docker/docker-compose.yml logs -f app

# 停止服务
docker-compose -f docker/docker-compose.yml down
```

### 生产环境配置

#### JVM 优化参数

```bash
java -jar -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:ParallelGCThreads=4 \
     -XX:ConcGCThreads=2 \
     -Djava.security.egd=file:/dev/./urandom \
     -Dspring.profiles.active=prod \
     build/libs/springboot-starter-0.0.1-SNAPSHOT.jar
```

#### 应用配置

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

  cache:
    type: redis
    redis:
      time-to-live: 600000

server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/plain

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

## 监控和运维

### 健康检查

应用提供了以下健康检查端点：

- **应用健康**: `/actuator/health`
- **应用信息**: `/actuator/info`
- **系统指标**: `/actuator/metrics`

### 日志配置

使用 Logback 进行日志管理，支持：

- 按日期滚动日志文件
- 不同环境日志级别
- 结构化日志输出
- 异步日志记录

### 性能监控

集成了以下监控功能：

- **应用性能**: Spring Boot Actuator
- **数据库连接池**: HikariCP 监控
- **缓存命中率**: Redis 统计
- **HTTP 请求统计**: Micrometer

## 贡献指南

### 开发流程

1. **Fork 项目**
2. **创建功能分支**
   ```bash
   git checkout -b feature/new-feature
   ```
3. **开发并提交更改**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```
4. **推送分支**
   ```bash
   git push origin feature/new-feature
   ```
5. **创建 Pull Request**

### 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**类型说明**:

- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更改
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建或辅助工具变动

**示例**:

```
feat(auth): add JWT token refresh functionality

- Add refresh token endpoint
- Implement token rotation logic
- Add tests for token refresh

Closes #123
```

## 常见问题

### Q: 如何添加新的功能模块？

A: 在相应的包下创建新的类，遵循现有的项目结构和命名规范。

### Q: 如何修改数据库配置？

A: 编辑 `application-{profile}.yml` 文件中的数据库连接配置，或者使用环境变量。

### Q: 如何自定义缓存策略？

A: 修改 `RedisConfig` 类中的缓存配置，或者在具体的方法上使用 `@Cacheable` 等注解。

### Q: 如何添加新的 API 接口？

A: 在 `controller` 包下创建新的 Controller 类，使用 `@RestController` 注解。

### Q: 如何处理跨域请求？

A: 已经在 `WebConfig` 类中配置了 CORS，可以根据需要进行调整。

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请通过以下方式联系：

- **Issues**: [GitHub Issues](https://github.com/your-username/springboot-starter/issues)
- **Email**: your-email@example.com

## 更新日志

### v1.0.0 (2024-01-01)

- 初始版本发布
- 集成 Spring Boot 3.5.6
- 实现 JWT 认证
- 添加 Redis 缓存支持
- 集成 Swagger API 文档
- 完善的测试覆盖
