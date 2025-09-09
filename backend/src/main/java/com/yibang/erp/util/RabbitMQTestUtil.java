package com.yibang.erp.util;

import com.yibang.erp.config.RabbitMQConfig;
import com.yibang.erp.domain.dto.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ测试工具类
 * 用于测试消息发送和消费
 */
@Slf4j
@Component
public class RabbitMQTestUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送测试订单消息
     */
    public void sendTestOrderMessage() {
        try {
            OrderMessage message = createTestOrderMessage();
            
            // 发送到订单交换机
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE, 
                RabbitMQConfig.ORDERS_ROUTING_KEY, 
                message
            );
            
            log.info("测试订单消息发送成功: messageId={}, sourceOrderId={}", 
                    message.getMessageId(), message.getSourceOrderId());
                    
        } catch (Exception e) {
            log.error("发送测试订单消息失败", e);
        }
    }

    /**
     * 创建测试订单消息
     */
    private OrderMessage createTestOrderMessage() {
        Map<String, Object> extendedFields = new HashMap<>();
        extendedFields.put("orderType", "NORMAL");
        extendedFields.put("expectedDeliveryDate", "2024-12-31");
        extendedFields.put("currency", "CNY");
        extendedFields.put("deliveryAddress", "测试地址");
        extendedFields.put("deliveryContact", "测试联系人");
        extendedFields.put("deliveryPhone", "13800138000");
        extendedFields.put("sourceOrderId", "TEST_ORDER_" + System.currentTimeMillis());

        OrderMessage.OrderItemMessage item = OrderMessage.OrderItemMessage.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .remarks("测试商品")
                .extendedFields(new HashMap<>())
                .build();

        return OrderMessage.builder()
                .messageId("MSG_" + System.currentTimeMillis())
                .sourceOrderId("TEST_ORDER_" + System.currentTimeMillis())
                .salesOrderId("SALES_" + System.currentTimeMillis())
                .salesUserId(1L)
                .salesCompanyId(1L)
                .customerId(0L)
                .source("TEST")
                .templateVersion("1.0")
                .remarks("测试订单消息")
                .extendedFields(extendedFields)
                .orderItems(java.util.List.of(item))
                .messageTime(java.time.LocalDateTime.now().toString())
                .sourceSystem("TEST_SYSTEM")
                .build();
    }
}
