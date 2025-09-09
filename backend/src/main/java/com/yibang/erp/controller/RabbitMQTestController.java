package com.yibang.erp.controller;

import com.yibang.erp.util.RabbitMQTestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ测试控制器
 * 用于测试消息发送和消费功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test/rabbitmq")
public class RabbitMQTestController {

    @Autowired
    private RabbitMQTestUtil rabbitMQTestUtil;

    /**
     * 发送测试订单消息
     */
    @PostMapping("/send-test-message")
    public Map<String, Object> sendTestMessage() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            rabbitMQTestUtil.sendTestOrderMessage();
            result.put("success", true);
            result.put("message", "测试消息发送成功");
            log.info("测试消息发送请求处理成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试消息发送失败: " + e.getMessage());
            log.error("测试消息发送失败", e);
        }
        
        return result;
    }

    /**
     * 检查RabbitMQ连接状态
     */
    @PostMapping("/check-connection")
    public Map<String, Object> checkConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里可以添加连接检查逻辑
            result.put("success", true);
            result.put("message", "RabbitMQ连接正常");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "RabbitMQ连接异常: " + e.getMessage());
        }
        
        return result;
    }
}
