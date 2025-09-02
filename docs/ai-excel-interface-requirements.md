# AI Excel 接口需求文档

## 概述

本文档记录了AI Excel任务管理中心前端界面所需的完整后端接口。基于现有代码分析，部分接口已经实现，部分接口需要新增或完善。

## 已实现的接口

### 1. 基础文件上传
- **接口**: `POST /api/ai-excel/upload`
- **功能**: 上传Excel文件
- **状态**: ✅ 已实现

### 2. AI处理启动
- **接口**: `POST /api/ai-excel-orders/process`
- **功能**: 启动AI Excel处理任务
- **状态**: ✅ 已实现

### 3. 进度查询
- **接口**: `GET /api/ai-excel-orders/progress/{taskId}`
- **功能**: 查询任务处理进度
- **状态**: ✅ 已实现

### 4. 任务取消
- **接口**: `POST /api/ai-excel-orders/cancel/{taskId}`
- **功能**: 取消正在处理的任务
- **状态**: ✅ 已实现

### 5. 结果获取
- **接口**: `GET /api/ai-excel-orders/result/{taskId}`
- **功能**: 获取任务处理结果
- **状态**: ✅ 已实现

### 6. 任务历史
- **接口**: `GET /api/ai-excel-orders/tasks`
- **功能**: 获取任务历史列表
- **状态**: ✅ 已实现

### 7. 任务重试
- **接口**: `POST /api/ai-excel-orders/retry/{taskId}`
- **功能**: 重新处理失败的任务
- **状态**: ✅ 已实现

### 8. 任务删除
- **接口**: `DELETE /api/ai-excel-orders/task/{taskId}`
- **功能**: 删除指定任务
- **状态**: ✅ 已实现

### 9. 结果导出
- **接口**: `GET /api/ai-excel-orders/export/{taskId}`
- **功能**: 导出处理结果
- **状态**: ✅ 已实现

### 10. 日志下载
- **接口**: `GET /api/ai-excel-orders/logs/{taskId}`
- **功能**: 下载处理日志
- **状态**: ✅ 已实现

### 11. AI配置管理
- **接口**: `GET /api/ai-excel-orders/config`
- **功能**: 获取AI配置
- **状态**: ✅ 已实现

- **接口**: `PUT /api/ai-excel-orders/config`
- **功能**: 更新AI配置
- **状态**: ✅ 已实现

### 12. AI连接测试
- **接口**: `POST /api/ai-excel-orders/test-connection`
- **功能**: 测试AI服务连接
- **状态**: ✅ 已实现

## 需要新增的接口

### 1. 任务统计接口
- **接口**: `GET /api/ai-excel-orders/statistics`
- **功能**: 获取任务统计信息（总数、处理中、成功、失败等）
- **请求参数**: 
  - `companyId`: 公司ID
  - `startDate`: 开始日期（可选）
  - `endDate`: 结束日期（可选）
- **响应数据**:
```json
{
  "totalTasks": 12,
  "processingTasks": 3,
  "completedTasks": 8,
  "failedTasks": 1,
  "todayTasks": 2,
  "thisWeekTasks": 8,
  "thisMonthTasks": 25
}
```

### 2. 成功订单查询接口
- **接口**: `GET /api/ai-excel-orders/{taskId}/success-orders`
- **功能**: 获取任务成功生成的订单列表
- **请求参数**:
  - `page`: 页码
  - `size`: 每页大小
  - `sortBy`: 排序字段
  - `sortOrder`: 排序方向
- **响应数据**:
```json
{
  "content": [
    {
      "orderId": "ORD001",
      "customerName": "张三超市",
      "productName": "青岛啤酒",
      "quantity": 10,
      "amount": 200.00,
      "createdAt": "2024-01-15T14:35:12",
      "status": "CREATED"
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "currentPage": 1
}
```

### 3. 失败订单查询接口 ✅ 已实现
- **接口**: `GET /api/ai-excel-orders/{taskId}/failed-orders`
- **功能**: 获取任务处理失败的订单列表
- **请求参数**: 
  - `page`: 页码（默认1）
  - `size`: 每页大小（默认20）
  - `sortBy`: 排序字段（默认excelRowNumber）
  - `sortOrder`: 排序方向（默认asc）
- **响应数据**:
```json
{
  "content": [
    {
      "id": 1,
      "taskId": "AI_EXCEL_1756801831443_e7adae8e",
      "excelRowNumber": 23,
      "rawData": "{\"customerName\":\"\",\"productName\":\"青岛啤酒\",\"quantity\":5,\"amount\":100.00}",
      "errorType": "VALIDATION_ERROR",
      "errorMessage": "客户名称为必填字段",
      "suggestedAction": "请填写客户名称",
      "status": "PENDING",
      "createdAt": "2024-01-15T14:35:12",
      "updatedAt": "2024-01-15T14:35:12"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 1,
  "size": 20,
  "status": "SUCCESS",
  "message": "查询成功"
}
```
- **实现状态**: ✅ 已完成
- **前端组件**: `FailedOrdersList.vue` - 完整的失败订单列表展示
- **功能特性**: 
  - 分页显示失败订单
  - 按Excel行号排序
  - 显示错误类型、错误信息、处理建议
  - 支持查看原始数据
  - 支持重试失败订单

### 4. 失败原因统计接口
- **接口**: `GET /api/ai-excel-orders/{taskId}/error-statistics`
- **功能**: 获取失败订单的错误类型统计
- **响应数据**:
```json
{
  "totalErrors": 2,
  "errorTypeStats": [
    {
      "errorType": "VALIDATION_ERROR",
      "count": 1,
      "percentage": 50.0
    },
    {
      "errorType": "DATA_FORMAT_ERROR",
      "count": 1,
      "percentage": 50.0
    }
  ]
}
```

### 5. 原始数据查询接口
- **接口**: `GET /api/ai-excel-orders/{taskId}/raw-data`
- **功能**: 获取Excel原始数据
- **请求参数**:
  - `page`: 页码
  - `size`: 每页大小
- **响应数据**:
```json
{
  "content": [
    {
      "rowNumber": 1,
      "data": {
        "客户名称": "张三超市",
        "产品名称": "青岛啤酒",
        "数量": "10",
        "金额": "200.00"
      }
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 1
}
```

### 6. 字段映射信息接口
- **接口**: `GET /api/ai-excel-orders/{taskId}/field-mapping`
- **功能**: 获取AI识别的字段映射关系
- **响应数据**:
```json
{
  "mappings": [
    {
      "excelColumn": "A",
      "excelHeader": "客户名称",
      "systemField": "customer_name",
      "fieldType": "STRING",
      "confidence": 0.95,
      "description": "客户名称字段"
    }
  ],
  "overallConfidence": 0.92,
  "recognitionReasoning": "基于Excel内容分析，识别出客户、产品、数量等关键字段"
}
```

### 7. 处理日志查询接口
- **接口**: `GET /api/ai-excel-orders/{taskId}/processing-logs`
- **功能**: 获取任务处理过程的详细日志
- **请求参数**:
  - `page`: 页码
  - `size`: 每页大小
  - `level`: 日志级别（INFO, WARN, ERROR）
- **响应数据**:
```json
{
  "content": [
    {
      "timestamp": "2024-01-15T14:30:25",
      "level": "INFO",
      "message": "开始处理Excel文件",
      "details": "文件包含150行数据"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "currentPage": 1
}
```

### 8. 批量操作接口
- **接口**: `POST /api/ai-excel-orders/batch-retry`
- **功能**: 批量重试失败的任务
- **请求数据**:
```json
{
  "taskIds": ["task1", "task2", "task3"]
}
```

- **接口**: `POST /api/ai-excel-orders/batch-delete`
- **功能**: 批量删除任务
- **请求数据**: 同上

### 9. 任务搜索接口
- **接口**: `GET /api/ai-excel-orders/search`
- **功能**: 高级搜索任务
- **请求参数**:
  - `keyword`: 搜索关键词
  - `status`: 状态筛选
  - `dateRange`: 时间范围
  - `fileName`: 文件名
  - `minRows`: 最小行数
  - `maxRows`: 最大行数
  - `successRate`: 成功率范围
  - `processingDuration`: 处理时长范围

### 10. 实时进度推送接口
- **接口**: WebSocket `/ws/ai-excel-orders/{taskId}/progress`
- **功能**: 实时推送任务处理进度
- **推送数据**:
```json
{
  "taskId": "task123",
  "progress": {
    "totalRows": 150,
    "processedRows": 75,
    "successRows": 70,
    "failedRows": 5,
    "percentage": 50.0,
    "currentStep": "处理第75行数据",
    "estimatedTime": "预计剩余5分钟"
  }
}
```

## 数据模型扩展

### 1. TaskHistoryItem 扩展
需要在现有的 `TaskHistoryItem` 接口中添加以下字段：
```typescript
interface TaskHistoryItem {
  // 现有字段...
  
  // 新增字段
  supplier?: string;           // 供应商信息
  fileSize?: number;           // 文件大小
  uploadUser?: string;         // 上传用户
  startedAt?: string;          // 开始处理时间
  manualProcessRows?: number;  // 需手动处理的行数
  fieldMapping?: FieldMapping[]; // 字段映射信息
}
```

### 2. 新增类型定义
```typescript
interface FieldMapping {
  excelColumn: string;
  excelHeader: string;
  systemField: string;
  fieldType: string;
  confidence: number;
  description: string;
}

interface ErrorStatistics {
  totalErrors: number;
  errorTypeStats: ErrorTypeStat[];
}

interface ErrorTypeStat {
  errorType: string;
  count: number;
  percentage: number;
}
```

## 优先级建议

### 高优先级（必须实现）
1. 任务统计接口 ✅ 已实现
2. 成功订单查询接口
3. 失败订单查询接口 ✅ 已实现
4. 失败原因统计接口

### 中优先级（建议实现）
1. 原始数据查询接口
2. 字段映射信息接口
3. 处理日志查询接口
4. 批量操作接口

### 低优先级（可选实现）
1. 任务搜索接口
2. 实时进度推送接口

## 注意事项

1. **分页支持**: 所有列表查询接口都应该支持分页
2. **权限控制**: 确保用户只能访问自己公司的数据
3. **性能优化**: 对于大量数据的查询，考虑使用缓存和索引
4. **错误处理**: 提供详细的错误信息和错误码
5. **数据一致性**: 确保任务状态变更的原子性
6. **审计日志**: 记录所有重要操作的审计信息

## 实现进度总结

### ✅ 已完成的功能
1. **任务统计接口** (`GET /api/ai-excel-orders/statistics`)
   - 后端实现：`AIExcelOrderServiceImpl.getTaskStatistics()`
   - 前端集成：`TaskOverview.vue` 组件
   - 功能：显示总任务数、处理中、完成、失败等统计信息

2. **任务历史查询接口** (`GET /api/ai-excel-orders/tasks`)
   - 后端实现：`AIExcelOrderServiceImpl.getUserTasks()`
   - 前端集成：`TaskTable.vue` 组件
   - 功能：分页查询用户的任务列表，支持状态筛选

3. **失败订单查询接口** (`GET /api/ai-excel-orders/{taskId}/failed-orders`)
   - 后端实现：`AIExcelOrderServiceImpl.getFailedOrders()`
   - 前端集成：`FailedOrdersList.vue` 组件
   - 功能：分页查询失败订单，支持排序、查看原始数据、重试等

4. **任务详情弹窗**
   - 前端实现：`TaskDetailDialog.vue` 组件
   - 功能：标签页展示基本信息、失败订单、处理进度、操作按钮
   - 特性：弹窗位置固定、主页面锁定、智能滚动恢复

### 🔄 进行中的功能
- 无

### 📋 待实现的功能
1. **成功订单查询接口** (`GET /api/ai-excel-orders/{taskId}/success-orders`)
2. **失败原因统计接口** (`GET /api/ai-excel-orders/{taskId}/error-statistics`)
3. **原始数据查询接口** (`GET /api/ai-excel-orders/{taskId}/raw-data`)
4. **字段映射信息接口** (`GET /api/ai-excel-orders/{taskId}/field-mapping`)
5. **处理日志查询接口** (`GET /api/ai-excel-orders/{taskId}/processing-logs`)
6. **批量操作接口** (`POST /api/ai-excel-orders/batch-retry`, `POST /api/ai-excel-orders/batch-delete`)
7. **任务搜索接口** (`GET /api/ai-excel-orders/search`)
8. **实时进度推送接口** (WebSocket `/ws/ai-excel-orders/{taskId}/progress`)

### 📊 实现统计
- **总接口数**: 22个
- **已完成**: 3个 (13.6%)
- **进行中**: 0个 (0%)
- **待实现**: 19个 (86.4%)

## 测试建议

1. **单元测试**: 为每个接口编写单元测试
2. **集成测试**: 测试前后端接口的集成
3. **性能测试**: 测试大数据量下的接口性能
4. **安全测试**: 测试权限控制和数据隔离
5. **兼容性测试**: 测试不同Excel格式的兼容性
