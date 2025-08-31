package com.yibang.erp.domain.service.impl;

import com.yibang.erp.domain.service.OrderNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单号生成服务实现类
 * 采用Redis分布式锁方式，确保订单号唯一性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNumberGeneratorServiceImpl implements OrderNumberGeneratorService {
    
    private final StringRedisTemplate redisTemplate;
    
    // Redis键前缀
    private static final String ORDER_NUMBER_LOCK_PREFIX = "order_number_lock:";
    private static final String ORDER_NUMBER_SEQUENCE_PREFIX = "order_number_sequence:";
    
    // 锁超时时间（秒）
    private static final long LOCK_TIMEOUT = 10;
    
    // 订单号格式：登录ID+订单渠道+日期+序号
    private static final String ORDER_NUMBER_FORMAT = "%s%s%s%s";
    private static final String SEQUENCE_FORMAT = "%04d";
    
    @Override
    public String generatePlatformOrderNo(String  userName, String orderSource) {
        if (userName == null || orderSource == null) {
            throw new IllegalArgumentException("登录ID和订单渠道不能为空");
        }




        String lockKey = ORDER_NUMBER_LOCK_PREFIX + userName + ":" + orderSource;
        String orderNo = null;

        
        try {
            // 获取分布式锁
            Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(locked)) {
                // 生成订单号
                orderNo = generateOrderNumberWithLock(userName, orderSource);
                log.debug("成功生成订单号: {}", orderNo);
            } else {
                // 等待锁释放后重试
                log.debug("获取锁失败，等待后重试: {}", lockKey);
                Thread.sleep(100);
                return generatePlatformOrderNo(userName, orderSource);
            }
        } catch (Exception e) {
            log.error("生成订单号失败: userId={}, orderSource={}", userName, orderSource, e);
            throw new RuntimeException("生成订单号失败", e);
        } finally {
            // 释放锁
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception e) {
                log.warn("释放锁失败: {}", lockKey, e);
            }
        }
        
        return orderNo;
    }
    
    @Override
    public List<String> preGenerateOrderNumbers(String userName, String orderSource, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("生成数量必须大于0");
        }
        
        List<String> orderNumbers = new ArrayList<>();
        String lockKey = ORDER_NUMBER_LOCK_PREFIX + userName + ":" + orderSource;
        
        try {
            // 获取分布式锁
            Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(locked)) {
                // 批量生成订单号
                for (int i = 0; i < count; i++) {
                    String orderNo = generateOrderNumberWithLock(userName, orderSource);
                    orderNumbers.add(orderNo);
                }
                log.debug("批量生成订单号成功: count={}, accountId={}, orderSource={}", 
                         count, userName, orderSource);
            } else {
                // 等待锁释放后重试
                log.debug("获取锁失败，等待后重试: {}", lockKey);
                Thread.sleep(100);
                return preGenerateOrderNumbers(userName, orderSource, count);
            }
        } catch (Exception e) {
            log.error("批量生成订单号失败: userId={}, orderSource={}, count={}",
                    userName, orderSource, count, e);
            throw new RuntimeException("批量生成订单号失败", e);
        } finally {
            // 释放锁
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception e) {
                log.warn("释放锁失败: {}", lockKey, e);
            }
        }
        
        return orderNumbers;
    }
    
    @Override
    public boolean validateOrderNoFormat(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return false;
        }
        
        // 验证格式：登录ID+订单渠道+日期+序号
        // 例如：000001MANUAL202412010001
        if (orderNo.length() < 20) { // 最小长度：6位ID + 渠道 + 8位日期 + 4位序号
            return false;
        }
        
        // 提取各部分
        String userId = orderNo.substring(0, 6);
        String orderSource = orderNo.substring(6, 6 + getOrderSourceLength(orderNo.substring(6)));
        String dateStr = orderNo.substring(6 + orderSource.length(), 6 + orderSource.length() + 8);
        String sequenceStr = orderNo.substring(6 + orderSource.length() + 8);
        
        // 验证用户ID（6位数字）
        try {
            Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return false;
        }
        
        // 验证日期格式
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return false;
        }
        
        // 验证序号格式（4位数字）
        try {
            int sequence = Integer.parseInt(sequenceStr);
            return sequence > 0 && sequence <= 9999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 获取订单渠道的长度
     */
    private int getOrderSourceLength(String remaining) {
        if (remaining.startsWith("MANUAL")) return 6;
        if (remaining.startsWith("EXCEL_IMPORT")) return 12;
        if (remaining.startsWith("API")) return 3;
        if (remaining.startsWith("WEBSITE")) return 7;
        return 6; // 默认返回MANUAL的长度
    }
    
    /**
     * 在锁保护下生成订单号
     */
    private String generateOrderNumberWithLock(String userName, String orderSource) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequenceKey = ORDER_NUMBER_SEQUENCE_PREFIX + userName + ":" + orderSource + ":" + datePrefix;
        
        // 获取并递增序号
        Long sequence = redisTemplate.opsForValue().increment(sequenceKey);
        if (sequence == null) {
            sequence = 1L;
        }
        
        // 设置序号过期时间（第二天过期）
        redisTemplate.expire(sequenceKey, 24, TimeUnit.HOURS);
        
        // 格式化账户号（确保6位）
        // 确保账户名为6位，不足6位左补0，超过6位取前6位
        String formattedAccountId =userName.length() > 6 ? userName.substring(0, 6) : userName;


        // 格式化序号（4位）
        String formattedSequence = String.format(SEQUENCE_FORMAT, sequence);
        
        // 生成订单号
        return String.format(ORDER_NUMBER_FORMAT, 
                           formattedAccountId, 
                           orderSource, 
                           datePrefix, 
                           formattedSequence);
    }
}
