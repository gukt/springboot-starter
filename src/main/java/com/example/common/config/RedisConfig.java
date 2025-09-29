package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 配置类
 * 
 * 配置策略：
 * - String 类型的 key: 使用 StringRedisSerializer
 * - String 类型的 value: 使用 StringRedisSerializer
 * - Hash 类型的 value: 使用 Jackson2JsonRedisSerializer
 * - 其他复杂对象: 使用 Jackson2JsonRedisSerializer
 */
@Slf4j
@Configuration
public class RedisConfig {

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
     * 主要的 RedisTemplate Bean，用于操作复杂对象，使用 Jackson 序列化
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 创建 Jackson 序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = jackson2JsonRedisSerializer();
        // 创建 String 序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 统一设置对应所有的 Key 都使用 String 序列化，默认为 JdkSerializationRedisSerializer
        template.setDefaultSerializer(stringRedisSerializer);
        // 对于所有的 Value 以及 Hash Value 均采用 jackson2JsonRedisSerializer
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        log.info("RedisTemplate configured with Jackson serializer for values");
        return template;
    }

    /**
     * 专门用于 String 操作的 RedisTemplate
     */
    @Bean("stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        log.info("StringRedisTemplate configured for string operations");
        return template;
    }

}
