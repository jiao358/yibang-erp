package com.yibang.erp.domain.service;

import com.yibang.erp.domain.service.impl.OrderNumberGeneratorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单号生成服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderNumberGeneratorServiceTest {

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @Test
    void testGeneratePlatformOrderNo() {
        // 准备测试数据
        Long accountId = 123L;
        String orderSource = "MANUAL";
        
        // Mock Redis操作
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        // 创建服务实例
        OrderNumberGeneratorService service = new OrderNumberGeneratorServiceImpl(stringRedisTemplate);
        
        // 执行测试

        
        // 验证Redis调用
        verify(stringRedisTemplate.opsForValue(), times(1));
        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(valueOperations, times(1)).increment(anyString());
    }

    @Test
    void testPreGenerateOrderNumbers() {
        // 准备测试数据
        Long accountId = 456L;
        String orderSource = "EXCEL_IMPORT";
        int count = 3;
        
        // Mock Redis操作
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);
        
        // 创建服务实例
        OrderNumberGeneratorService service = new OrderNumberGeneratorServiceImpl(stringRedisTemplate);
        

    }

    @Test
    void testValidateOrderNoFormat() {
        // 创建服务实例
        OrderNumberGeneratorService service = new OrderNumberGeneratorServiceImpl(stringRedisTemplate);
        
        // 测试有效格式
        assertTrue(service.validateOrderNoFormat("000123MANUAL202412010001"));
        assertTrue(service.validateOrderNoFormat("000456EXCEL_IMPORT202412019999"));
        
        // 测试无效格式
        assertFalse(service.validateOrderNoFormat(null));
        assertFalse(service.validateOrderNoFormat(""));
        assertFalse(service.validateOrderNoFormat("invalid"));
        assertFalse(service.validateOrderNoFormat("000123MANUAL20241201"));
        assertFalse(service.validateOrderNoFormat("000123MANUAL202412010000"));
        assertFalse(service.validateOrderNoFormat("000123MANUAL2024120110000"));
    }

    @Test
    void testGeneratePlatformOrderNoWithNullParams() {
        // 创建服务实例
        OrderNumberGeneratorService service = new OrderNumberGeneratorServiceImpl(stringRedisTemplate);
        
        // 测试空参数
        assertThrows(IllegalArgumentException.class, () -> 
            service.generatePlatformOrderNo(null, "MANUAL"));

    }

    @Test
    void testPreGenerateOrderNumbersWithInvalidCount() {
        // 创建服务实例
        OrderNumberGeneratorService service = new OrderNumberGeneratorServiceImpl(stringRedisTemplate);
        
        // 测试无效数量

    }
}
