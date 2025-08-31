#!/bin/bash

# 测试订单相关API的脚本
echo "🧪 测试订单相关API..."

# 测试生成订单号API
echo "1. 测试生成订单号API..."
ORDER_NO_RESPONSE=$(curl -s -X GET "http://localhost:7102/api/orders/generate-order-no" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json")

echo "   响应: $ORDER_NO_RESPONSE"

# 测试获取订单模板API
echo "2. 测试获取订单模板API..."
TEMPLATES_RESPONSE=$(curl -s -X GET "http://localhost:7102/api/order-templates" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json")

echo "   响应: $TEMPLATES_RESPONSE"

echo "✅ API测试完成！"
echo ""
echo "注意：如果看到401错误，说明需要有效的JWT token"
echo "如果看到其他错误，请检查后端服务是否启动"
