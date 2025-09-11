package com.yibang.erp.controller;

import com.rabbitmq.client.Channel;
import com.yibang.erp.config.RabbitMQConfig;
import com.yibang.erp.domain.dto.AddressChangeMessage;
import com.yibang.erp.domain.dto.OrderCreateRequest;
import com.yibang.erp.domain.dto.OrderMessage;
import com.yibang.erp.domain.entity.Customer;
import com.yibang.erp.domain.entity.MessageProcessingLog;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.OrderService;
import com.yibang.erp.infrastructure.repository.CustomerRepository;
import com.yibang.erp.infrastructure.repository.MessageProcessingLogRepository;
import com.yibang.erp.infrastructure.repository.UserRedisRepository;
import com.yibang.erp.service.MessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
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

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRedisRepository userRedisRepository;

    @Autowired
    private MessageLogService messageLogService;

    // RabbitTemplate 用于发送消息（如需要）
    // @Autowired
    // private RabbitTemplate rabbitTemplate;
    /**
     * 消费订单地址修改消息
     *
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleAddressChangeMessage(AddressChangeMessage addressChangeMessage,
                                           @Header(value = "x-idempotency-key", required = true) String xIdempotencyKey,
                                           @Header(value = "x-correlation-id", required = true) String xCorrelationId,
                                           @Header(value = "messageId", required = false) String messageId,
                                           Channel channel,
                                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){






    }





    /**
     * 消费订单创建消息
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderMessage(OrderMessage message, 
                                  @Header(value = "x-idempotency-key", required = true) String xIdempotencyKey,
                                  @Header(value = "x-correlation-id", required = true) String xCorrelationId,
                                  @Header(value = "messageId", required = false) String messageId,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
     ) {
        // messageId 是消息唯一id
        messageId = xIdempotencyKey;
        
        //xCorrelationId 最好使用的为 orderId
        // 从消息头提取Idempotency-Key（按优先级）
        //message中的id 为xIdempotencyKey 业务唯一件id ，在创建订单中应该是orderId
        message.setIdempotencyKey(xCorrelationId);



        
        log.info("开始处理订单消息: messageId={}, xCorrelationId={}, orderId={}",
                messageId, xCorrelationId, message.getOrderId());

        try {
            // 1. 消息幂等性由MQ层面处理，这里直接处理业务逻辑

            // 2. 使用Redis分布式锁确保消息处理不重复（使用messageId）
            String lockKeyToUse = "order:message:lock:" + messageId;
                
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKeyToUse, "1", Duration.ofMinutes(10));
                    
            if (!Boolean.TRUE.equals(locked)) {
                log.warn("消息正在处理中，跳过: messageId={}, idempotencyKey={}", messageId, xIdempotencyKey);
                
                // 发送ACK，确认消息已处理（重复消息）
                channel.basicAck(deliveryTag, false);
                log.info("重复处理消息已确认: messageId={}", messageId);
                return;
            }

            try {
                //先检查业务订单的幂等
                // 3. 检查订单是否已存在（业务幂等性） 消息订单必须为成功则触发幂等，这里检查的应该是业务信息唯一件，对于订单来说唯一件的确应该是orderId ，但对于状态的修改，唯一件就应该是别的id了
                if(isOrderExists(xIdempotencyKey,xCorrelationId)){
                    log.warn("订单已存在，跳过: xCorrelationId={}", xCorrelationId);
                    channel.basicAck(deliveryTag, false);
                    log.info("重复订单消息已确认: messageId={}", messageId);
                    return;
                }

                // 4. 记录消息处理开始
                messageLogService.recordMessageProcessing(messageId, message, RabbitMQConfig.ORDER_CREATE_QUEUE, "PROCESSING", "开始处理订单消息");


                // 5. 转换消息为订单创建请求
                OrderCreateRequest orderRequest = convertToOrderCreateRequest(message);

                // 6. 创建订单
                orderService.createOrderByAPI(orderRequest);

                // 7. 记录处理成功
                messageLogService.recordMessageProcessing(messageId, message, RabbitMQConfig.ORDER_CREATE_QUEUE, "SUCCESS", "订单创建成功");
                log.info("订单消息处理成功: messageId={}, orderId={}", 
                        messageId, message.getOrderId());

            } finally {
                // 8. 释放分布式锁
                redisTemplate.delete(lockKeyToUse);
            }
            //9. 这里要进行ack确认
            channel.basicAck(deliveryTag, false);


        } catch (Exception e) {
            log.error("处理订单消息失败: messageId={}", messageId, e);
            messageLogService.recordMessageProcessing(messageId, message, RabbitMQConfig.ORDER_CREATE_QUEUE, "FAILED", "处理失败: " + e.getMessage());
            
            try {
                // 手动发送 ACK，让消息进入死信队列
                channel.basicNack(deliveryTag, false, false);
                log.info("消息已确认，将进入死信队列: messageId={}", messageId);
            } catch (Exception ackException) {
                log.error("发送ACK失败: messageId={}", messageId, ackException);
            }
            
            // 不再抛出异常，避免重复处理
            return;
        }
    }

    /**
     * 消费死信队列消息
     */
//    @RabbitListener(queues = RabbitMQConfig.ORDERS_DLQ)
    public void handleDeadLetterMessage(OrderMessage message, 
                                       @Header(value = "messageId", required = false) String messageId,
                                       @Header(value = "x-correlation-id", required = false) String xCorrelationId,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 如果RabbitMQ没有提供messageId，则使用xCorrelationId作为消息ID
        if (messageId == null || messageId.trim().isEmpty()) {
            messageId = xCorrelationId;
        }
        
        log.error("收到死信消息: messageId={}, orderId={}", 
                messageId, message.getOrderId());
        
        try {
            // 记录死信消息
            messageLogService.recordMessageProcessing(messageId, message, RabbitMQConfig.ORDERS_DLQ, "DEAD_LETTER", "消息进入死信队列");
            
            // 手动发送 ACK，确认死信消息已处理
            channel.basicAck(deliveryTag, false);
            log.info("死信消息已确认: messageId={}", messageId);
            
        } catch (Exception e) {
            log.error("处理死信消息失败: messageId={}", messageId, e);
            try {
                // 发送 NACK，拒绝消息
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackException) {
                log.error("发送NACK失败: messageId={}", messageId, nackException);
            }
        }
        
        // 这里可以添加人工处理逻辑或告警通知
        // 例如：发送邮件、短信通知运维人员
    }

    /**
     * 检查消息是否已处理
     */
    private boolean isMessageProcessed(String messageId, String idempotencyKey) {
        // 优先使用Idempotency-Key检查（业务级幂等性）
        if (idempotencyKey != null) {
            MessageProcessingLog log = messageLogRepository.selectByIdempotencyKey(idempotencyKey);
            if (log != null && ("SUCCESS".equals(log.getStatus()) || "DUPLICATE".equals(log.getStatus()))) {
                return true;
            }
        }
        
        // 回退到messageId检查（消息级幂等性）
        MessageProcessingLog log = messageLogRepository.selectByMessageId(messageId);
        return log != null && ("SUCCESS".equals(log.getStatus()) || "DUPLICATE".equals(log.getStatus()));
    }

    /**
     * 检查消息是否存在
     */
    private boolean isOrderExists(String xIdempotencyKey,
                                  String xCorrelationId) {
        //先查询是否同样的消息 ，防止消息重复投敌
        MessageProcessingLog existingLog = messageLogRepository.selectByMessageId(xIdempotencyKey);
        if(existingLog != null){
            log.info("消息处理状态已存在，跳过记录: messageId={}, status={}", xIdempotencyKey, existingLog.getStatus());
            return true;
        }

        //查询是否有相同的业务id
        existingLog = messageLogRepository.selectByIdempotencyKey(xCorrelationId);
        if(existingLog != null){
            log.info("消息处理状态已存在，跳过记录: messageId={}, status={}", xCorrelationId, existingLog);
            return true
                    ;
        }

        return false;



    }


    /**
     * 转换消息为订单创建请求
     */
    private OrderCreateRequest convertToOrderCreateRequest(OrderMessage message) {
        OrderCreateRequest request = new OrderCreateRequest();
        // 将电商平台的orderId映射到salesOrderId
        request.setSalesOrderId(message.getOrderId());


        //全部为estela
        User user = userRedisRepository.findByUsername("estela");
        request.setSalesUserId(user.getId());
        request.setSalesCompanyId(user.getCompanyId());//所属公司

        //这里面应该去进行客户的查找和创建
        Customer customer = findOrCreateCustomerByNickName(message.getUserNickName(), user.getCompanyId());


        request.setCustomerId(customer.getId());

        //固定source api
        request.setSource("API");
        request.setTemplateVersion(message.getTemplateVersion());

        request.setBuyerNote(message.getBuyerNote());




        request.setRemarks(message.getRemarks());
        // 将电商平台的orderId也映射到sourceOrderId
        request.setSourceOrderId(message.getOrderId());




        // 构建扩展字段，包含电商平台字段
        Map<String, Object> extendedFields = new HashMap<>();
        if (message.getExtendedFields() != null) {
            extendedFields.putAll(message.getExtendedFields());
        }
        
        // 添加电商平台字段到扩展字段
        if (message.getCreateDate() != null) {
            extendedFields.put("createDate", message.getCreateDate());
        }
        if (message.getUserNickName() != null) {
            extendedFields.put("userNickName", message.getUserNickName());
        }
        if (message.getOrderType() != null) {
            extendedFields.put("orderType", message.getOrderType());
        }
        if (message.getOrderId() != null) {
            extendedFields.put("orderId", message.getOrderId());
        }
        if (message.getStatus() != null) {
            extendedFields.put("status", message.getStatus());
        }
        if (message.getProvinceName() != null) {
            extendedFields.put("provinceName", message.getProvinceName());
        }
        if (message.getCityName() != null) {
            extendedFields.put("cityName", message.getCityName());
        }
        if (message.getDistrictName() != null) {
            extendedFields.put("districtName", message.getDistrictName());
        }
        if (message.getDeliveryContact() != null) {
            extendedFields.put("deliveryContact", message.getDeliveryContact());
        }
        if (message.getDeliveryPhone() != null) {
            extendedFields.put("deliveryPhone", message.getDeliveryPhone());
        }
        if (message.getDeliveryAddress() != null) {
            extendedFields.put("deliveryAddress", message.getDeliveryAddress());
        }
        if (message.getBuyerNote() != null) {
            extendedFields.put("buyerNote", message.getBuyerNote());
        }
        
        request.setExtendedFields(extendedFields);
        
        // 转换订单项
        if (message.getOrderItems() != null) {
            request.setOrderItems(message.getOrderItems().stream()
                    .map(item -> {
                        OrderCreateRequest.OrderItemRequest itemRequest = new OrderCreateRequest.OrderItemRequest();
                        itemRequest.setProductId(item.getProductId());
                        itemRequest.setQuantity(item.getQuantity());
                        itemRequest.setUnitPrice(item.getUnitPrice());
                        itemRequest.setRemarks(item.getRemarks());

                        // 构建商品扩展字段
                        Map<String, Object> itemExtendedFields = new HashMap<>();
                        if (item.getExtendedFields() != null) {
                            itemExtendedFields.putAll(item.getExtendedFields());
                        }
                        
                        // 添加电商平台商品字段
                        if (item.getOfferName() != null) {
                            itemExtendedFields.put("offerName", item.getOfferName());
                        }
                        if (item.getOfferId() != null) {
                            itemExtendedFields.put("offerId", item.getOfferId());
                        }
                        if (item.getCommission() != null) {
                            itemExtendedFields.put("commission", item.getCommission());
                        }
                        
                        itemRequest.setExtendedFields(itemExtendedFields);
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


    /**
     * 根据用户昵称查找或创建客户
     */
    private Customer findOrCreateCustomerByNickName(String userNickName, Long salesCompanyId) {
        try {
            // 先尝试根据客户名称查找
            Customer existingCustomer = customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getName, userNickName)
                    .eq(Customer::getCompanyId, salesCompanyId)
                    .eq(Customer::getDeleted, false)
            );
            
            if (existingCustomer != null) {
                log.info("找到现有客户: customerId={}, name={}", existingCustomer.getId(), userNickName);
                return existingCustomer;
            }
            
            // 如果不存在，创建新客户
            Customer newCustomer = new Customer();
            newCustomer.setName(userNickName);
            newCustomer.setCompanyId(salesCompanyId);
            newCustomer.setCustomerType("INDIVIDUAL"); // 默认为个人客户
            newCustomer.setCustomerLevel("REGULAR"); // 默认为普通客户
            newCustomer.setStatus("ACTIVE");
            newCustomer.setCustomerCode(userNickName);
            newCustomer.setCreatedAt(LocalDateTime.now());
            newCustomer.setUpdatedAt(LocalDateTime.now());
            newCustomer.setCreatedBy(0L); // 系统创建
            newCustomer.setUpdatedBy(0L);
            newCustomer.setDeleted(false);
            
            customerRepository.insert(newCustomer);
            log.info("创建新客户: customerId={}, name={}", newCustomer.getId(), userNickName);
            return newCustomer;
            
        } catch (Exception e) {
            log.error("查找或创建客户失败: userNickName={}, salesCompanyId={}", userNickName, salesCompanyId, e);
            // 返回默认客户ID 0，表示未知客户
            Customer defaultCustomer = new Customer();
            defaultCustomer.setId(0L);
            return defaultCustomer;
        }
    }
}
