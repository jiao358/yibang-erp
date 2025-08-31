# 订单核心模块设计文档

## 1. 架构设计

### 1.1 整体架构
```
┌─────────────────────────────────────────────────────────────┐
│                    前端展示层 (Vue 3)                        │
├─────────────────────────────────────────────────────────────┤
│                    订单管理界面                              │
│                    客户管理界面                              │
│                    模板管理界面                              │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    API网关层 (Spring Boot)                   │
├─────────────────────────────────────────────────────────────┤
│                    权限控制                                 │
│                    请求路由                                 │
│                    限流熔断                                 │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务服务层                                │
├─────────────────────────────────────────────────────────────┤
│  订单服务  │  客户服务  │  模板服务  │  批量处理服务  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    AI转换层                                  │
├─────────────────────────────────────────────────────────────┤
│                    内容提取                                 │
│                    智能标准化                               │
│                    模板映射                                 │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    数据访问层                                │
├─────────────────────────────────────────────────────────────┤
│                    MyBatis Plus                            │
│                    自定义分页                               │
│                    多数据源                                 │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    数据存储层                                │
├─────────────────────────────────────────────────────────────┤
│                    MySQL 8.0                               │
│                    Redis 7.x                               │
│                    文件存储                                 │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 核心设计理念

#### 1.2.1 统一订单模板
- **设计目标**：建立统一的平台订单模板，支持多格式输入
- **核心思想**：固定核心字段 + 动态扩展字段
- **优势**：
  - 降低系统复杂度
  - 提高扩展性
  - 便于维护和升级

#### 1.2.2 分层处理架构
- **录入层**：支持多种输入方式
- **转换层**：AI智能转换和标准化
- **控制层**：风险控制和权限管理
- **管理层**：状态流转和业务处理

#### 1.2.3 配置化驱动
- **模板配置**：动态配置订单模板
- **规则配置**：业务规则可配置
- **流程配置**：审批流程可配置

## 2. 数据模型设计

### 2.1 核心实体关系
```
Order (订单)
├── OrderItem (订单项)
├── Customer (客户)
├── OrderStatusLog (状态日志)
├── OrderApprovalLog (审批日志)
├── LogisticsInfo (物流信息)
└── ExcelImportLog (导入日志)

OrderTemplate (订单模板)
├── FieldConfig (字段配置)
├── BusinessRuleConfig (业务规则)
└── FieldMappingConfig (字段映射)
```

### 2.2 关键字段设计

#### 2.2.1 订单核心字段
```sql
-- 订单主表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform_order_id VARCHAR(64) UNIQUE NOT NULL,  -- 平台订单号
    sales_id VARCHAR(64),                          -- 销售订单号
    customer_id BIGINT,                           -- 客户ID
    supplier_company_id BIGINT,                   -- 供应商公司ID
    order_status ENUM(...),                       -- 订单状态
    order_source ENUM(...),                       -- 订单来源
    order_type ENUM(...),                         -- 订单类型
    total_amount DECIMAL(15,2),                   -- 订单总金额
    currency VARCHAR(10),                         -- 货币类型
    payment_status ENUM(...),                     -- 支付状态
    approval_status ENUM(...),                    -- 审批状态
    extended_fields JSON,                         -- 扩展字段
    ai_confidence DECIMAL(5,2),                   -- AI置信度
    ai_processed BOOLEAN,                         -- AI是否处理
    created_at TIMESTAMP,                         -- 创建时间
    updated_at TIMESTAMP                          -- 更新时间
);
```

#### 2.2.2 扩展字段设计
```json
{
  "extended_fields": {
    "custom_field_1": "value1",
    "custom_field_2": "value2",
    "source_specific_data": {
      "excel_row": 5,
      "image_ocr_result": "...",
      "api_metadata": {...}
    }
  }
}
```

## 3. 接口设计

### 3.1 RESTful API设计

#### 3.1.1 订单管理接口
```
POST   /api/orders                    # 创建订单
GET    /api/orders                    # 获取订单列表
GET    /api/orders/{id}              # 获取订单详情
PUT    /api/orders/{id}              # 更新订单
DELETE /api/orders/{id}              # 删除订单
POST   /api/orders/{id}/status       # 更新订单状态
POST   /api/orders/{id}/submit       # 提交订单
POST   /api/orders/{id}/cancel       # 取消订单
```

#### 3.1.2 批量处理接口
```
POST   /api/orders/batch/import-excel    # 批量导入Excel
POST   /api/orders/batch/update-status   # 批量更新状态
POST   /api/orders/batch/delete          # 批量删除
POST   /api/orders/batch/assign-supplier # 批量分配供应商
```

#### 3.1.3 模板管理接口
```
POST   /api/order-templates              # 创建模板
GET    /api/order-templates              # 获取模板列表
PUT    /api/order-templates/{id}         # 更新模板
DELETE /api/order-templates/{id}         # 删除模板
POST   /api/order-templates/{id}/activate # 激活模板
```

### 3.2 数据格式规范

#### 3.2.1 请求格式
```json
{
  "customerId": 123,
  "orderItems": [
    {
      "productId": 456,
      "quantity": 10,
      "unitPrice": 99.99
    }
  ],
  "deliveryAddress": "北京市朝阳区...",
  "expectedDeliveryDate": "2024-02-01",
  "remarks": "加急订单"
}
```

#### 3.2.2 响应格式
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 789,
    "platformOrderId": "PO20240115001",
    "orderStatus": "DRAFT",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

## 4. 业务流程设计

### 4.1 订单创建流程
```
1. 选择订单来源 (手动/Excel/图片/API)
2. 输入/上传订单数据
3. AI智能转换和标准化
4. 数据验证和冲突检测
5. 创建订单记录
6. 返回订单信息
```

### 4.2 订单审批流程
```
1. 订单提交
2. 自动/手动审批
3. 审批通过/拒绝
4. 状态更新
5. 通知相关人员
6. 记录审批日志
```

### 4.3 订单处理流程
```
1. 订单确认
2. 分配供应商
3. 库存检查
4. 生产/采购
5. 发货处理
6. 物流跟踪
7. 订单完成
```

## 5. 技术实现方案

### 5.1 前端实现

#### 5.1.1 组件架构
```
OrderManagement/
├── OrderList.vue              # 订单列表
├── OrderDialog.vue            # 订单编辑对话框
├── ImportDialog.vue           # 导入对话框
├── StatusHistoryDialog.vue    # 状态历史对话框
└── components/
    ├── OrderForm.vue          # 订单表单
    ├── OrderItemTable.vue     # 订单项表格
    └── OrderStatusFlow.vue    # 状态流程图
```

#### 5.1.2 状态管理
```typescript
// 使用 Pinia 进行状态管理
export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null,
    loading: false,
    pagination: {
      current: 1,
      pageSize: 20,
      total: 0
    }
  }),
  
  actions: {
    async fetchOrders(params) { /* ... */ },
    async createOrder(orderData) { /* ... */ },
    async updateOrder(id, orderData) { /* ... */ }
  }
})
```

### 5.2 后端实现

#### 5.2.1 服务层设计
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AIService aiService;
    
    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. 数据验证
        validateOrderData(request);
        
        // 2. AI转换处理
        OrderData processedData = aiService.processOrderData(request);
        
        // 3. 冲突检测
        handleOrderConflict(processedData);
        
        // 4. 创建订单
        Order order = buildOrder(processedData);
        orderRepository.insert(order);
        
        // 5. 返回响应
        return buildOrderResponse(order);
    }
}
```

#### 5.2.2 异常处理
```java
@ControllerAdvice
public class OrderExceptionHandler {
    
    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(OrderValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "订单数据验证失败", e.getMessage()));
    }
    
    @ExceptionHandler(OrderConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(OrderConflictException e) {
        return ResponseEntity.status(409)
            .body(ApiResponse.error(409, "订单冲突", e.getMessage()));
    }
}
```

## 6. 性能优化策略

### 6.1 数据库优化
- 索引优化：为常用查询字段建立复合索引
- 分页查询：使用游标分页避免深度分页问题
- 读写分离：主库写操作，从库读操作

### 6.2 缓存策略
- 订单缓存：热点订单数据缓存到Redis
- 模板缓存：订单模板配置缓存
- 用户权限缓存：减少数据库查询

### 6.3 异步处理
- 批量导入：使用消息队列异步处理
- AI转换：异步调用AI服务
- 状态更新：异步更新相关数据

## 7. 安全设计

### 7.1 权限控制
- 基于角色的访问控制（RBAC）
- 数据级权限控制
- 操作审计日志

### 7.2 数据安全
- 敏感数据加密存储
- 数据传输加密
- 数据备份和恢复

### 7.3 接口安全
- API限流和防刷
- 参数验证和过滤
- SQL注入防护

## 8. 监控和运维

### 8.1 性能监控
- 接口响应时间监控
- 数据库性能监控
- 系统资源监控

### 8.2 业务监控
- 订单处理量监控
- 错误率监控
- 用户行为监控

### 8.3 告警机制
- 性能阈值告警
- 错误率告警
- 系统异常告警
