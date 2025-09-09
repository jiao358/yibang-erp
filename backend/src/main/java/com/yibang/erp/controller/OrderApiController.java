package com.yibang.erp.controller;

import com.yibang.erp.config.RabbitMQConfig;
import com.yibang.erp.domain.dto.OrderCreateRequest;
import com.yibang.erp.domain.dto.OrderMessage;
import com.yibang.erp.domain.entity.MessageProcessingLog;
import com.yibang.erp.domain.service.OrderService;
import com.yibang.erp.infrastructure.repository.MessageProcessingLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
/**
 * 订单API消息消费者 - 处理RabbitMQ消息
 */
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageProcessingLogRepository messageLogRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // RabbitTemplate 用于发送消息（如需要）
    // @Autowired
    // private RabbitTemplate rabbitTemplate;

    /**
     * 消费订单创建消息
     */
    @RabbitListener(queues = RabbitMQConfig.ORDERS_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderMessage(OrderMessage message) {
        String messageId = message.getMessageId();
        String lockKey = "order:message:lock:" + messageId;
        
        log.info("开始处理订单消息: messageId={}, sourceOrderId={}, salesOrderId={}", 
                messageId, message.getSourceOrderId(), message.getSalesOrderId());

        try {
            // 1. 检查消息是否已处理（幂等性检查）
            if (isMessageProcessed(messageId)) {
                log.warn("消息已被处理，跳过: messageId={}", messageId);
                return;
            }

            // 2. 使用Redis分布式锁确保幂等性
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofMinutes(10));
                    
            if (!Boolean.TRUE.equals(locked)) {
                log.warn("消息正在处理中，跳过: messageId={}", messageId);
                return;
            }

            try {
                // 3. 记录消息处理开始
                recordMessageProcessing(messageId, message, "PROCESSING", "开始处理订单消息");

                // 4. 检查订单是否已存在（业务幂等性）
                if (isOrderExists(message.getSourceOrderId(), message.getSalesOrderId())) {
                    log.warn("订单已存在，跳过: sourceOrderId={}, salesOrderId={}", 
                            message.getSourceOrderId(), message.getSalesOrderId());
                    recordMessageProcessing(messageId, message, "DUPLICATE", "订单已存在");
                    return;
                }

                // 5. 转换消息为订单创建请求
                OrderCreateRequest orderRequest = convertToOrderCreateRequest(message);

                // 6. 创建订单
                orderService.createOrder(orderRequest);

                // 7. 记录处理成功
                recordMessageProcessing(messageId, message, "SUCCESS", "订单创建成功");
                log.info("订单消息处理成功: messageId={}, sourceOrderId={}", 
                        messageId, message.getSourceOrderId());

            } finally {
                // 8. 释放分布式锁
                redisTemplate.delete(lockKey);
            }

        } catch (Exception e) {
            log.error("处理订单消息失败: messageId={}", messageId, e);
            recordMessageProcessing(messageId, message, "FAILED", "处理失败: " + e.getMessage());
            
            // 可以选择重新投递到死信队列或重试
            throw e; // 让Spring AMQP处理重试逻辑
        }
    }

    /**
     * 消费死信队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.ORDERS_DLQ)
    public void handleDeadLetterMessage(OrderMessage message) {
        String messageId = message.getMessageId();
        log.error("收到死信消息: messageId={}, sourceOrderId={}, salesOrderId={}", 
                messageId, message.getSourceOrderId(), message.getSalesOrderId());
        
        // 记录死信消息
        recordMessageProcessing(messageId, message, "DEAD_LETTER", "消息进入死信队列");
        
        // 这里可以添加人工处理逻辑或告警通知
        // 例如：发送邮件、短信通知运维人员
    }

    /**
     * 检查消息是否已处理
     */
    private boolean isMessageProcessed(String messageId) {
        MessageProcessingLog log = messageLogRepository.selectByMessageId(messageId);
        return log != null && ("SUCCESS".equals(log.getStatus()) || "DUPLICATE".equals(log.getStatus()));
    }

    /**
     * 检查订单是否已存在
     */
    private boolean isOrderExists(String sourceOrderId, String salesOrderId) {
        MessageProcessingLog log = messageLogRepository.selectByOrderIds(sourceOrderId, salesOrderId);
        return log != null && "SUCCESS".equals(log.getStatus());
    }

    /**
     * 记录消息处理日志
     */
    private void recordMessageProcessing(String messageId, OrderMessage message, String status, String resultMessage) {
        MessageProcessingLog log = new MessageProcessingLog();
        log.setMessageId(messageId);
        log.setSourceOrderId(message.getSourceOrderId());
        log.setSalesOrderId(message.getSalesOrderId());
        log.setStatus(status);
        log.setResultMessage(resultMessage);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        
        if ("SUCCESS".equals(status) || "DUPLICATE".equals(status)) {
            log.setProcessedAt(LocalDateTime.now());
        }
        
        messageLogRepository.insert(log);
    }

    /**
     * 转换消息为订单创建请求
     */
    private OrderCreateRequest convertToOrderCreateRequest(OrderMessage message) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setSalesOrderId(message.getSalesOrderId());
        request.setSalesUserId(message.getSalesUserId());
        request.setSalesCompanyId(message.getSalesCompanyId());
        request.setCustomerId(message.getCustomerId() != null ? message.getCustomerId() : 0L);
        request.setSource(message.getSource());
        request.setTemplateVersion(message.getTemplateVersion());
        request.setRemarks(message.getRemarks());
        request.setExtendedFields(message.getExtendedFields());
        
        // 转换订单项
        if (message.getOrderItems() != null) {
            request.setOrderItems(message.getOrderItems().stream()
                    .map(item -> {
                        OrderCreateRequest.OrderItemRequest itemRequest = new OrderCreateRequest.OrderItemRequest();
                        itemRequest.setProductId(item.getProductId());
                        itemRequest.setQuantity(item.getQuantity());
                        itemRequest.setUnitPrice(item.getUnitPrice());
                        itemRequest.setRemarks(item.getRemarks());
                        itemRequest.setExtendedFields(item.getExtendedFields());
                        return itemRequest;
                    })
                    .toList());
        }
        
        return request;
    }

    /**
     * 手动重新处理死信消息
     */
    public void reprocessDeadLetterMessage(String messageId) {
        log.info("手动重新处理死信消息: messageId={}", messageId);
        // 这里可以实现从死信队列重新投递消息到主队列的逻辑
    }

    /**
     * 获取消息处理统计
     */
    public Map<String, Object> getMessageProcessingStats() {
        Map<String, Object> stats = new HashMap<>();
        // 这里可以实现统计逻辑
        return stats;
    }
}
