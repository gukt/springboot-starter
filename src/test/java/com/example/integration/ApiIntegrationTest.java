package com.example.integration;

import com.example.domain.User;
import com.example.domain.Role;
import com.example.domain.Permission;
import com.example.repository.UserRepository;
import com.example.repository.RoleRepository;
import com.example.repository.PermissionRepository;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * API 集成测试类
 *
 * 测试完整的 API 调用流程，包括认证、授权和数据操作
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // 清理数据库
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // 创建测试数据
        createTestData();
    }

    private void createTestData() {
        // 创建权限
        Permission userReadPermission = new Permission();
        userReadPermission.setName("查看用户");
        userReadPermission.setCode("user:read");
        userReadPermission.setDescription("查看用户信息");
        permissionRepository.save(userReadPermission);

        Permission userWritePermission = new Permission();
        userWritePermission.setName("编辑用户");
        userWritePermission.setCode("user:write");
        userWritePermission.setDescription("编辑用户信息");
        permissionRepository.save(userWritePermission);

        // 创建角色
        Role adminRole = new Role();
        adminRole.setName("管理员");
        adminRole.setCode("ADMIN");
        adminRole.setDescription("系统管理员");
        adminRole.setPermissions(Set.of(userReadPermission, userWritePermission));
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("普通用户");
        userRole.setCode("USER");
        userRole.setDescription("普通用户");
        userRole.setPermissions(Set.of(userReadPermission));
        roleRepository.save(userRole);

        // 创建用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("系统管理员");
        admin.setStatus("ACTIVE");
        admin.setRoles(Set.of(adminRole));
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setEmail("user@example.com");
        normalUser.setPassword(passwordEncoder.encode("user123"));
        normalUser.setFullName("普通用户");
        normalUser.setStatus("ACTIVE");
        normalUser.setRoles(Set.of(userRole));
        normalUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(normalUser);

        // 生成 JWT Token
        adminToken = jwtUtil.generateToken(admin.getUsername(), Set.of("ADMIN"));
        userToken = jwtUtil.generateToken(normalUser.getUsername(), Set.of("USER"));
    }

    @Test
    void testHelloEndpoint_NoAuth() throws Exception {
        // 测试不需要认证的接口
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test/hello", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Hello, Spring Boot Starter!"));
    }

    @Test
    void testAuthEndpoint_WithValidCredentials() throws Exception {
        // 测试登录接口
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "admin", "admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("accessToken"));
    }

    @Test
    void testUserEndpoint_WithAdminToken() throws Exception {
        // 测试管理员访问用户列表接口
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users?page=1&size=10",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("content"));
    }

    @Test
    void testUserEndpoint_WithUserToken() throws Exception {
        // 测试普通用户访问用户列表接口（应该被拒绝）
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users?page=1&size=10",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUserEndpoint_WithoutToken() throws Exception {
        // 测试未认证访问用户列表接口
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users?page=1&size=10", String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testCreateUser_WithAdminToken() throws Exception {
        // 测试管理员创建用户
        String createUserRequest = "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"password\":\"password123\",\"fullName\":\"New User\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(createUserRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/users", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("newuser"));
    }

    @Test
    void testCreateUser_WithUserToken() throws Exception {
        // 测试普通用户创建用户（应该被拒绝）
        String createUserRequest = "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"password\":\"password123\",\"fullName\":\"New User\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(createUserRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/users", request, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetMe_WithValidToken() throws Exception {
        // 测试获取当前用户信息
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("user"));
    }

    @Test
    void testCacheEndpoint() throws Exception {
        // 测试缓存接口
        String cacheRequest = "{\"key\":\"test-key\",\"value\":\"test-value\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(cacheRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/test/cache", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("success"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        // 测试健康检查接口
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    void testProtectedEndpoint_WithoutToken() throws Exception {
        // 测试需要认证的接口无Token访问
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test/auth", String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testProtectedEndpoint_WithToken() throws Exception {
        // 测试需要认证的接口有Token访问
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/test/auth",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Authentication successful"));
    }

    @Test
    void testValidationEndpoint_WithValidData() throws Exception {
        // 测试参数验证接口（有效数据）
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/test/validate?name=John&age=25",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Validation passed"));
    }

    @Test
    void testValidationEndpoint_WithInvalidData() throws Exception {
        // 测试参数验证接口（无效数据）
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/test/validate?name=&age=200",
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUserNotFound() throws Exception {
        // 测试访问不存在的用户
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/999",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}