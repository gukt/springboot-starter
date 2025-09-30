package com.example.common.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存配置类
 *
 * 配置策略：
 * - 一级缓存：Caffeine 本地缓存，快速访问热点数据
 * - 二级缓存：Redis 分布式缓存，数据共享和持久化
 * - 多级缓存策略：本地缓存优先，Redis 作为后备
 * - 针对不同场景配置不同的过期时间
 */
@Slf4j
@EnableCaching
@Configuration
public class CacheConfig {

        /**
         * 创建 Jackson 序列化器
         */
        private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.WRAPPER_ARRAY);

                return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        }

        /**
         * Caffeine 缓存配置
         * 本地缓存，用于高频访问的静态数据和热点数据
         */
        @Bean
        public CaffeineCacheManager caffeineCacheManager() {
                CaffeineCacheManager cacheManager = new CaffeineCacheManager();

                // 设置默认配置
                cacheManager.setCaffeine(com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                                .initialCapacity(100) // 初始容量
                                .maximumSize(1000) // 最大容量
                                .expireAfterWrite(Duration.ofMinutes(30)) // 写入后30分钟过期
                                .recordStats()); // 记录统计信息

                log.info("Caffeine cache manager configured");
                return cacheManager;
        }

        /**
         * Redis 缓存配置
         * 分布式缓存，用于共享数据和持久化
         */
        @Bean
        public RedisCacheConfiguration redisCacheConfiguration() {
                Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = jackson2JsonRedisSerializer();
                StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

                // 配置序列化
                RedisSerializationContext.SerializationPair<Object> jsonSerialization = RedisSerializationContext.SerializationPair
                                .fromSerializer(jackson2JsonRedisSerializer);
                RedisSerializationContext.SerializationPair<String> stringSerialization = RedisSerializationContext.SerializationPair
                                .fromSerializer(stringRedisSerializer);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(stringSerialization)
                                .serializeValuesWith(jsonSerialization)
                                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                                .disableCachingNullValues() // 不缓存空值
                                .computePrefixWith(cacheName -> cacheName + ":"); // 设置缓存键前缀
        }

        /**
         * Redis 缓存管理器
         * 针对不同业务场景配置不同的缓存策略
         */
        @Bean
        public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration defaultConfig = redisCacheConfiguration();

                // 针对不同缓存名称配置不同的过期时间
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // 用户相关缓存：2小时
                cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofHours(2)));

                // 权限相关缓存：3小时
                cacheConfigurations.put("permission", defaultConfig.entryTtl(Duration.ofHours(3)));

                // 系统配置缓存：6小时
                cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ofHours(6)));

                // 验证码缓存：5分钟
                cacheConfigurations.put("captcha", defaultConfig.entryTtl(Duration.ofMinutes(5)));

                // 会话缓存：24小时
                cacheConfigurations.put("session", defaultConfig.entryTtl(Duration.ofHours(24)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .transactionAware() // 支持事务
                                .build();
        }

        // /**
        // * 主要的 RedisTemplate Bean，用于操作复杂对象，使用 Jackson 序列化
        // */
        // @Bean
        // @Primary
        // public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory
        // connectionFactory) {
        // RedisTemplate<String, Object> template = new RedisTemplate<>();
        // template.setConnectionFactory(connectionFactory);
        //
        // // 创建序列化器
        // Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        // jackson2JsonRedisSerializer();
        // StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //
        // // 设置 Key 序列化
        // template.setKeySerializer(stringRedisSerializer);
        // template.setHashKeySerializer(stringRedisSerializer);
        //
        // // 设置 Value 序列化
        // template.setValueSerializer(jackson2JsonRedisSerializer);
        // template.setHashValueSerializer(jackson2JsonRedisSerializer);
        //
        // // 启用事务支持
        // template.setEnableTransactionSupport(true);
        // template.afterPropertiesSet();
        //
        // log.info("RedisTemplate configured with Jackson serializer");
        // return template;
        // }
        //
        // /**
        // * 专门用于 String 操作的 RedisTemplate
        // */
        // @Bean("stringRedisTemplate")
        // public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory
        // connectionFactory) {
        // StringRedisTemplate template = new StringRedisTemplate();
        // template.setConnectionFactory(connectionFactory);
        // template.setEnableTransactionSupport(true);
        // template.afterPropertiesSet();
        //
        // log.info("StringRedisTemplate configured for string operations");
        // return template;
        // }

        /**
         * 多级缓存管理器
         * 实现本地缓存 + Redis 的多级缓存策略
         * 先查询 Caffeine，未命中再查询 Redis
         */
        @Bean
        @Primary
        public CacheManager multiLevelCacheManager(
                        CaffeineCacheManager caffeineCacheManager,
                        RedisCacheManager redisCacheManager) {

                CompositeCacheManager compositeCacheManager = new CompositeCacheManager(caffeineCacheManager,
                                redisCacheManager);
                compositeCacheManager.setFallbackToNoOpCache(true);

                log.info("Multi-level cache manager configured with Caffeine (L1) and Redis (L2)");
                return compositeCacheManager;
        }
}