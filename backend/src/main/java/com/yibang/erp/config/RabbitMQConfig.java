package com.yibang.erp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类 - 消费服务
 * 
 * 注意：此配置与MQ中间服务层（orderingest）的RabbitMQ配置完全兼容
 * MQ中间服务层负责创建交换机和队列，消费服务只负责声明Bean引用和绑定
 * 
 * 队列配置对应关系：
 * - 订单创建队列：orders.create.q
 * - 地址更新队列：orders.address.q
 * - 物流更新队列：orders.logistics.q
 * - 订单状态更新队列：orders.status.q
 * - 死信队列：orders.dlq
 */
@Configuration
public class RabbitMQConfig {

    // 队列和交换机常量 - 与MQ中间服务层保持一致
    public static final String ORDERS_EXCHANGE = "orders.OrderExchange";
    
    // 按消息类型分离的队列
    public static final String ORDER_CREATE_QUEUE = "orders.create.q";
    public static final String ADDRESS_UPDATE_QUEUE = "orders.address.q";
    public static final String LOGISTICS_UPDATE_QUEUE = "orders.logistics.q";
    public static final String ORDER_STATUS_UPDATE_QUEUE = "orders.status.q";
    
    // 死信队列配置
    public static final String ORDERS_DLX = "orders.dlx";
    public static final String ORDERS_DLQ = "orders.dlq";
    
    // 路由键
    public static final String ORDER_CREATE_ROUTING_KEY = "orders.created";
    public static final String ADDRESS_UPDATE_ROUTING_KEY = "orders.address.updated";
    public static final String LOGISTICS_UPDATE_ROUTING_KEY = "orders.logistics.updated";
    public static final String ORDER_STATUS_UPDATE_ROUTING_KEY = "orders.status.updated";
    public static final String DLQ_ROUTING_KEY = "dead";

    @Value("${orders.queue.ttl:0}")
    private long ordersQueueTtl;

    /**
     * 声明订单交换机（topic）。
     */
    @Bean
    @Qualifier("ordersExchange")
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    /**
     * 声明死信交换机（direct）。
     */
    @Bean
    @Qualifier("ordersDlx")
    public DirectExchange ordersDlx() {
        return new DirectExchange(ORDERS_DLX, true, false);
    }

    /**
     * 声明订单创建队列。
     */
    @Bean
    @Qualifier("orderCreateQueue")
    public Queue orderCreateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        if (ordersQueueTtl > 0) {
            args.put("x-message-ttl", ordersQueueTtl);
        }
        return new Queue(ORDER_CREATE_QUEUE, true, false, false, args);
    }

    /**
     * 声明地址修改队列。
     */
    @Bean
    @Qualifier("addressUpdateQueue")
    public Queue addressUpdateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        if (ordersQueueTtl > 0) {
            args.put("x-message-ttl", ordersQueueTtl);
        }
        return new Queue(ADDRESS_UPDATE_QUEUE, true, false, false, args);
    }

    /**
     * 声明物流信息修改队列。
     */
    @Bean
    @Qualifier("logisticsUpdateQueue")
    public Queue logisticsUpdateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        if (ordersQueueTtl > 0) {
            args.put("x-message-ttl", ordersQueueTtl);
        }
        return new Queue(LOGISTICS_UPDATE_QUEUE, true, false, false, args);
    }

    /**
     * 声明订单状态修改队列。
     */
    @Bean
    @Qualifier("orderStatusUpdateQueue")
    public Queue orderStatusUpdateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        if (ordersQueueTtl > 0) {
            args.put("x-message-ttl", ordersQueueTtl);
        }
        return new Queue(ORDER_STATUS_UPDATE_QUEUE, true, false, false, args);
    }

    /**
     * 声明死信队列。
     */
    @Bean
    @Qualifier("ordersDlq")
    public Queue ordersDlq() {
        return new Queue(ORDERS_DLQ, true);
    }

    /**
     * 绑定订单创建队列到订单交换机。
     */
    @Bean
    public Binding orderCreateBinding(
            @Qualifier("orderCreateQueue") Queue orderCreateQueue,
            @Qualifier("ordersExchange") TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderCreateQueue).to(ordersExchange).with(ORDER_CREATE_ROUTING_KEY);
    }

    /**
     * 绑定地址修改队列到订单交换机。
     */
    @Bean
    public Binding addressUpdateBinding(
            @Qualifier("addressUpdateQueue") Queue addressUpdateQueue,
            @Qualifier("ordersExchange") TopicExchange ordersExchange) {
        return BindingBuilder.bind(addressUpdateQueue).to(ordersExchange).with(ADDRESS_UPDATE_ROUTING_KEY);
    }

    /**
     * 绑定物流信息修改队列到订单交换机。
     */
    @Bean
    public Binding logisticsUpdateBinding(
            @Qualifier("logisticsUpdateQueue") Queue logisticsUpdateQueue,
            @Qualifier("ordersExchange") TopicExchange ordersExchange) {
        return BindingBuilder.bind(logisticsUpdateQueue).to(ordersExchange).with(LOGISTICS_UPDATE_ROUTING_KEY);
    }

    /**
     * 绑定订单状态修改队列到订单交换机。
     */
    @Bean
    public Binding orderStatusUpdateBinding(
            @Qualifier("orderStatusUpdateQueue") Queue orderStatusUpdateQueue,
            @Qualifier("ordersExchange") TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderStatusUpdateQueue).to(ordersExchange).with(ORDER_STATUS_UPDATE_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机。
     */
    @Bean
    public Binding dlqBinding(
            @Qualifier("ordersDlq") Queue ordersDlq,
            @Qualifier("ordersDlx") DirectExchange ordersDlx) {
        return BindingBuilder.bind(ordersDlq).to(ordersDlx).with(DLQ_ROUTING_KEY);
    }

    /**
     * 配置 JSON 消息转换器（Jackson）。
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        return converter;
    }

    /**
     * 配置 RabbitTemplate，启用 JSON 转换与 mandatory 返回。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * 消费者容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        
        // 设置手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        
        // 设置预取数量
        factory.setPrefetchCount(1);
        
        return factory;
    }
}