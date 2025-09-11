package com.yibang.erp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    }


}
