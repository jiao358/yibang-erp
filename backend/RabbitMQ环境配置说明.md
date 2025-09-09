# RabbitMQ环境配置说明

## 概述

本文档说明了在不同环境中RabbitMQ的配置方式，确保生产服务（orderingest）和消费服务（yibang-erp）能够正常通信。

## 环境配置文件

### 1. 开发环境 (application-dev.yml)
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    connection-timeout: 15000
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2.0
          max-interval: 10000
```

### 2. 测试环境 (application-test.yml)
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    connection-timeout: 15000
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2.0
          max-interval: 10000
```

### 3. 生产环境 (application-prod.yml)
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
    connection-timeout: 15000
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2.0
          max-interval: 10000
```

### 4. 本地环境 (application-local.yml)
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
    connection-timeout: 15000
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2.0
          max-interval: 10000
```

## 环境变量配置

### 生产环境环境变量
```bash
# RabbitMQ连接配置
export RABBITMQ_HOST=your-rabbitmq-host
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
export RABBITMQ_VHOST=/

# 其他环境变量
export MYSQL_HOST=your-mysql-host
export MYSQL_PORT=3306
export MYSQL_DATABASE=yibang_erp_prod
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password
```

### Docker Compose 配置示例
```yaml
version: '3.8'
services:
  yibang-erp:
    image: yibang-erp:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_PORT=5672
      - RABBITMQ_USERNAME=admin
      - RABBITMQ_PASSWORD=admin123
      - RABBITMQ_VHOST=/
      - MYSQL_HOST=mysql
      - MYSQL_DATABASE=yibang_erp_prod
      - REDIS_HOST=redis
    depends_on:
      - rabbitmq
      - mysql
      - redis

  rabbitmq:
    image: rabbitmq:3.12-management
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=admin123
    ports:
      - "5672:5672"
      - "15672:15672"
```

## 队列和交换机配置

### 队列信息
- **主队列**: `orders.OrderIngest.q`
- **死信队列**: `orders.dlq`
- **交换机**: `orders.OrderExchange` (Topic)
- **死信交换机**: `orders.dlx` (Direct)
- **路由键**: `orders.created`
- **死信路由键**: `dead`

### 消息流程
1. 生产服务发送消息到 `orders.OrderExchange` 交换机
2. 消息通过 `orders.created` 路由键进入 `orders.OrderIngest.q` 队列
3. 消费服务从 `orders.OrderIngest.q` 队列消费消息
4. 失败的消息进入 `orders.dlq` 死信队列

## 监控和调试

### 健康检查
```bash
# 检查RabbitMQ连接
curl -X POST http://localhost:7102/api/test/rabbitmq/check-connection

# 发送测试消息
curl -X POST http://localhost:7102/api/test/rabbitmq/send-test-message
```

### 日志配置
```yaml
logging:
  level:
    org.springframework.amqp: DEBUG
    com.yibang.erp.controller.OrderApiController: DEBUG
```

## 故障排除

### 常见问题
1. **连接失败**: 检查RabbitMQ服务是否启动，网络是否连通
2. **认证失败**: 检查用户名密码是否正确
3. **交换机类型冲突**: 确保交换机类型与生产服务一致
4. **消息丢失**: 检查消息确认模式和重试配置

### 调试步骤
1. 检查应用启动日志中的RabbitMQ连接信息
2. 使用RabbitMQ管理界面查看队列状态
3. 检查消息处理日志
4. 验证消息格式是否正确
