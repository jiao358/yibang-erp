# RabbitMQ配置问题解决方案

## 问题描述

遇到以下错误：
```
PRECONDITION_FAILED - inequivalent arg 'x-dead-letter-exchange' for queue 'orders.OrderIngest.q' in vhost '/': received none but current is the value 'orders.dlx' of type 'longstr'
```

## 问题原因

这个错误表明队列`orders.OrderIngest.q`已经存在，但配置参数不匹配：

1. **生产服务**已经创建了队列，并配置了死信交换机参数：
   - `x-dead-letter-exchange`: `orders.dlx`
   - `x-dead-letter-routing-key`: `dead`

2. **消费服务**尝试声明同一个队列，但没有提供相同的参数，导致参数不匹配

## 解决方案

### 1. 修复队列声明参数

在`RabbitMQConfig.java`中，队列声明必须包含与生产服务相同的参数：

```java
@Bean
public Queue ordersQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", ORDERS_DLX);
    args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
    return new Queue(ORDERS_QUEUE, true, false, false, args);
}
```

### 2. 完整的配置对比

#### 生产服务配置（orderingest）
```java
@Bean
@Qualifier("ordersQueue")
public Queue ordersQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", ORDERS_DLX);
    args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
    if (ordersQueueTtl > 0) {
        args.put("x-message-ttl", ordersQueueTtl);
    }
    return new Queue(ORDERS_QUEUE, true, false, false, args);
}
```

#### 消费服务配置（yibang-erp）
```java
@Bean
public Queue ordersQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", ORDERS_DLX);
    args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
    return new Queue(ORDERS_QUEUE, true, false, false, args);
}
```

### 3. 关键参数说明

| 参数 | 值 | 说明 |
|------|-----|------|
| `x-dead-letter-exchange` | `orders.dlx` | 死信交换机名称 |
| `x-dead-letter-routing-key` | `dead` | 死信路由键 |
| `durable` | `true` | 队列持久化 |
| `exclusive` | `false` | 非独占队列 |
| `autoDelete` | `false` | 不自动删除 |

## 验证步骤

### 1. 检查RabbitMQ管理界面

访问 `http://localhost:15672`，使用 `guest/guest` 登录：

1. 查看 **Queues** 标签页
2. 找到 `orders.OrderIngest.q` 队列
3. 点击队列名称查看详情
4. 确认 **Arguments** 部分包含：
   - `x-dead-letter-exchange`: `orders.dlx`
   - `x-dead-letter-routing-key`: `dead`

### 2. 使用测试脚本

运行测试脚本检查连接：
```bash
./backend/test-rabbitmq-connection.sh
```

### 3. 使用HTTP API测试

```bash
# 检查队列参数
curl -u guest:guest http://localhost:15672/api/queues/%2F/orders.OrderIngest.q

# 检查交换机
curl -u guest:guest http://localhost:15672/api/exchanges/%2F/orders.OrderExchange
curl -u guest:guest http://localhost:15672/api/exchanges/%2F/orders.dlx
```

## 常见问题

### 1. 队列已存在但参数不匹配

**解决方案**: 删除现有队列，让生产服务重新创建，或确保消费服务使用相同的参数

```bash
# 删除队列（谨慎操作）
rabbitmqctl delete_queue orders.OrderIngest.q
```

### 2. 交换机类型不匹配

**解决方案**: 确保交换机类型一致
- `orders.OrderExchange`: Topic
- `orders.dlx`: Direct

### 3. 路由键不匹配

**解决方案**: 确保路由键模式一致
- 生产服务使用: `orders.created`
- 消费服务绑定: `orders.*`

## 最佳实践

### 1. 配置管理

- 生产服务和消费服务使用相同的常量定义
- 队列参数必须完全一致
- 使用版本控制管理配置变更

### 2. 环境隔离

- 不同环境使用不同的虚拟主机
- 开发环境: `/dev`
- 测试环境: `/test`
- 生产环境: `/prod`

### 3. 监控和日志

- 启用RabbitMQ管理插件
- 监控队列长度和消息处理速度
- 设置适当的日志级别

## 故障排除

### 1. 连接失败

```bash
# 检查RabbitMQ服务状态
systemctl status rabbitmq-server

# 检查端口
netstat -tlnp | grep 5672
```

### 2. 权限问题

```bash
# 检查用户权限
rabbitmqctl list_users
rabbitmqctl list_permissions
```

### 3. 内存不足

```bash
# 检查内存使用
rabbitmqctl status
```

## 配置验证清单

- [ ] 队列参数与生产服务一致
- [ ] 交换机类型正确
- [ ] 路由键模式匹配
- [ ] 死信队列配置正确
- [ ] 连接参数正确
- [ ] 权限配置正确
- [ ] 网络连接正常
- [ ] 服务状态正常
