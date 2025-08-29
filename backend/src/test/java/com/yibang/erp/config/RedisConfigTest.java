package com.yibang.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis配置测试类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@SpringBootTest
@ActiveProfiles("dev")
class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisScript<Long> incrementScript;

    @Test
    void testRedisTemplateConfiguration() {
        assertNotNull(redisTemplate, "RedisTemplate应该被正确配置");
        assertNotNull(redisTemplate.getConnectionFactory(), "Redis连接工厂应该被配置");
    }

    @Test
    void testIncrementScriptConfiguration() {
        assertNotNull(incrementScript, "Redis脚本应该被正确配置");
        assertEquals(Long.class, incrementScript.getResultType(), "脚本返回类型应该是Long");
    }

    @Test
    void testRedisConnection() {
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("test:key", "test:value");
            Object value = redisTemplate.opsForValue().get("test:key");
            assertEquals("test:value", value, "Redis读写操作应该正常工作");
            
            // 清理测试数据
            redisTemplate.delete("test:key");
        } catch (Exception e) {
            fail("Redis连接测试失败: " + e.getMessage());
        }
    }

    @Test
    void testRedisScriptExecution() {
        try {
            // 测试脚本执行
            Long result = redisTemplate.execute(
                incrementScript,
                java.util.Collections.singletonList("test:counter"),
                1L
            );
            assertNotNull(result, "脚本执行结果不应该为null");
            assertTrue(result >= 1, "计数器应该至少为1");
            
            // 清理测试数据
            redisTemplate.delete("test:counter");
        } catch (Exception e) {
            fail("Redis脚本执行测试失败: " + e.getMessage());
        }
    }
}
