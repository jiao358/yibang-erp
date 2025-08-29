package com.yibang.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * Redis配置类 - 完整配置，包含连接池、序列化、脚本执行等
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Configuration
public class RedisConfig {

    /**
     * Redis模板配置 - 优化序列化和异常处理
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 设置value的序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 设置默认序列化器
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 启用事务支持
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis脚本执行器 - 用于原子操作
     */
    @Bean
    public RedisScript<Long> incrementScript() {
        return new DefaultRedisScript<>(
            "local current = redis.call('get', KEYS[1]) " +
            "if current == false then " +
            "  redis.call('set', KEYS[1], ARGV[1]) " +
            "  return tonumber(ARGV[1]) " +
            "else " +
            "  local new = tonumber(current) + tonumber(ARGV[1]) " +
            "  redis.call('set', KEYS[1], new) " +
            "  return new " +
            "end",
            Long.class
        );
    }
}
