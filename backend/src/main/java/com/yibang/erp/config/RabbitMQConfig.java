package com.yibang.erp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类 - 消费服务
 * 
 * 注意：此配置与MQ中间服务层（orderingest）的RabbitMQ配置兼容
 * MQ中间服务层负责创建交换机和队列，消费服务只负责声明Bean引用和绑定
 * 
 * 队列配置对应关系：
 * - 订单创建队列：orders.create.q
 * - 路由键：orders.created
 * - 死信队列：orders.dlq
 */
@Configuration
public class RabbitMQConfig {

    // 队列和交换机常量 - 与MQ中间服务层保持一致
    public static final String ORDERS_EXCHANGE = "orders.OrderExchange";
    public static final String ORDER_CREATE_QUEUE = "orders.create.q";
    public static final String ORDERS_DLX = "orders.dlx";
    public static final String ORDERS_DLQ = "orders.dlq";
    public static final String ORDER_CREATE_ROUTING_KEY = "orders.created";
    public static final String DLQ_ROUTING_KEY = "dead";

    // 交换机和队列已由生产服务创建，这里只需要声明Bean引用
    // 不重新创建，避免配置冲突

    /**
     * 订单交换机引用（由生产服务创建）
     */
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    /**
     * 死信交换机引用（由生产服务创建）
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ORDERS_DLX, true, false);
    }

    /**
     * 订单创建队列引用（由MQ中间服务层创建）
     * 注意：必须与MQ中间服务层的队列参数完全一致
     */
    @Bean
    public Queue orderCreateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDERS_DLX);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(ORDER_CREATE_QUEUE, true, false, false, args);
    }

    /**
     * 死信队列引用（由生产服务创建）
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(ORDERS_DLQ, true);
    }

    /**
     * 订单创建队列绑定到交换机
     * 使用具体路由键 orders.created 以匹配MQ中间服务层的路由
     */
    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder
                .bind(orderCreateQueue())
                .to(ordersExchange())
                .with(ORDER_CREATE_ROUTING_KEY);
    }

    /**
     * 死信队列绑定到死信交换机
     * 使用由生产服务创建的 orders.dlx 交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    /**
     * JSON消息转换器
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
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); // 开启消息返回确认
        return template;
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
