#!/bin/bash

# RabbitMQ连接测试脚本
# 用于验证RabbitMQ配置是否正确

echo "=== RabbitMQ连接测试 ==="

# 检查RabbitMQ服务是否运行
echo "1. 检查RabbitMQ服务状态..."
if pgrep -x "rabbitmq-server" > /dev/null; then
    echo "✅ RabbitMQ服务正在运行"
else
    echo "❌ RabbitMQ服务未运行，请先启动RabbitMQ"
    exit 1
fi

# 检查端口是否开放
echo "2. 检查RabbitMQ端口..."
if nc -z localhost 5672; then
    echo "✅ RabbitMQ端口5672可访问"
else
    echo "❌ RabbitMQ端口5672不可访问"
    exit 1
fi

# 检查管理端口
echo "3. 检查RabbitMQ管理端口..."
if nc -z localhost 15672; then
    echo "✅ RabbitMQ管理端口15672可访问"
else
    echo "⚠️  RabbitMQ管理端口15672不可访问（可选）"
fi

# 使用curl测试HTTP API
echo "4. 测试RabbitMQ HTTP API..."
if curl -s -u guest:guest http://localhost:15672/api/overview > /dev/null; then
    echo "✅ RabbitMQ HTTP API可访问"
else
    echo "⚠️  RabbitMQ HTTP API不可访问"
fi

# 检查队列是否存在
echo "5. 检查队列状态..."
QUEUES=("orders.OrderIngest.q" "orders.dlq")
EXCHANGES=("orders.OrderExchange" "orders.dlx")

for queue in "${QUEUES[@]}"; do
    if curl -s -u guest:guest "http://localhost:15672/api/queues/%2F/$queue" > /dev/null; then
        echo "✅ 队列 $queue 存在"
    else
        echo "❌ 队列 $queue 不存在"
    fi
done

for exchange in "${EXCHANGES[@]}"; do
    if curl -s -u guest:guest "http://localhost:15672/api/exchanges/%2F/$exchange" > /dev/null; then
        echo "✅ 交换机 $exchange 存在"
    else
        echo "❌ 交换机 $exchange 不存在"
    fi
done

echo ""
echo "=== 测试完成 ==="
echo "如果所有检查都通过，说明RabbitMQ配置正确"
echo "如果有错误，请检查："
echo "1. RabbitMQ服务是否启动"
echo "2. 队列和交换机是否由生产服务创建"
echo "3. 网络连接是否正常"
