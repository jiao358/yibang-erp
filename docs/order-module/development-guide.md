# 订单核心模块开发指南

## 1. 开发环境配置

### 1.1 必需环境
```bash
# Java环境
Java 17+
Maven 3.8+

# 数据库
MySQL 8.0+
Redis 7.x+

# 前端环境
Node.js 18+
npm 9+
Vue 3.3+
```

### 1.2 推荐开发工具
- **IDE**：IntelliJ IDEA 2023.1+ / VS Code
- **数据库工具**：Navicat / DBeaver
- **API测试**：Postman / Insomnia
- **Git工具**：GitKraken / SourceTree

### 1.3 环境变量配置
```bash
# 后端环境变量
export JAVA_HOME=/path/to/java17
export MAVEN_HOME=/path/to/maven
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 前端环境变量
export NODE_ENV=development
export VITE_API_BASE_URL=http://localhost:8080/api
```

## 2. 代码规范

### 2.1 后端代码规范

#### 2.1.1 命名规范
```java
// 类名：大驼峰命名
public class OrderService {}
public class OrderServiceImpl {}

// 方法名：小驼峰命名
public OrderResponse createOrder(OrderCreateRequest request) {}
public void updateOrderStatus(Long orderId, String status) {}

// 常量：全大写，下划线分隔
public static final String DEFAULT_ORDER_STATUS = "DRAFT";
public static final int MAX_ORDER_ITEMS = 100;

// 变量名：小驼峰命名
private String platformOrderId;
private List<OrderItem> orderItems;
```

#### 2.1.2 注释规范
```java
/**
 * 订单服务接口
 * 
 * @author 开发团队
 * @since 2024-01-15
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 
     * @param request 订单创建请求
     * @return 订单响应信息
     * @throws OrderValidationException 订单验证失败时抛出
     * @throws OrderConflictException 订单冲突时抛出
     */
    OrderResponse createOrder(OrderCreateRequest request);
}
```

#### 2.1.3 异常处理规范
```java
// 自定义异常类
public class OrderValidationException extends BusinessException {
    public OrderValidationException(String message) {
        super(message);
    }
    
    public OrderValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// 异常处理
@ControllerAdvice
public class OrderExceptionHandler {
    
    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(OrderValidationException e) {
        log.error("订单验证失败: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "订单数据验证失败", e.getMessage()));
    }
}
```

### 2.2 前端代码规范

#### 2.2.1 命名规范
```typescript
// 组件名：大驼峰命名
export default defineComponent({
  name: 'OrderDialog'
})

// 变量名：小驼峰命名
const orderList = ref<OrderResponse[]>([])
const currentOrder = ref<OrderResponse | null>(null)

// 常量：全大写，下划线分隔
const DEFAULT_PAGE_SIZE = 20
const ORDER_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' }
]

// 方法名：小驼峰命名
const handleOrderSubmit = async () => {}
const fetchOrderList = async (params: OrderListRequest) => {}
```

#### 2.2.2 类型定义规范
```typescript
// 接口定义：大驼峰命名，以I开头
export interface IOrderResponse {
  id: number
  platformOrderId: string
  orderStatus: OrderStatus
  createdAt: string
  updatedAt: string
}

// 枚举定义：大驼峰命名
export enum OrderStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  CONFIRMED = 'CONFIRMED',
  COMPLETED = 'COMPLETED'
}

// 类型别名：大驼峰命名
export type OrderListRequest = {
  page: number
  pageSize: number
  status?: OrderStatus
  keyword?: string
}
```

#### 2.2.3 组件规范
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 导入语句
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { IOrderResponse } from '@/types/order'
import { orderApi } from '@/api/order'

// 接口定义
interface Props {
  visible: boolean
  orderId?: number
}

// 属性定义
const props = withDefaults(defineProps<Props>(), {
  visible: false
})

// 事件定义
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const loading = ref(false)
const orderData = ref<IOrderResponse | null>(null)

// 计算属性
const dialogTitle = computed(() => {
  return props.orderId ? '编辑订单' : '创建订单'
})

// 方法定义
const handleSubmit = async () => {
  try {
    loading.value = true
    // 业务逻辑
    ElMessage.success('操作成功')
    emit('success')
    emit('update:visible', false)
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  if (props.orderId) {
    fetchOrderData()
  }
})
</script>

<style scoped>
/* 样式定义 */
</style>
```

## 3. 开发流程

### 3.1 功能开发流程
```
1. 需求分析 → 2. 技术设计 → 3. 数据库设计 → 4. 后端开发 → 5. 前端开发 → 6. 联调测试 → 7. 代码审查 → 8. 合并发布
```

### 3.2 代码提交规范
```bash
# 提交信息格式
<type>(<scope>): <subject>

# 类型说明
feat:     新功能
fix:      修复bug
docs:     文档更新
style:    代码格式调整
refactor: 代码重构
test:     测试相关
chore:    构建过程或辅助工具的变动

# 示例
feat(order): 添加订单批量导入功能
fix(customer): 修复客户信息更新失败问题
docs(api): 更新API接口文档
```

### 3.3 分支管理规范
```bash
# 分支命名
feature/order-batch-import    # 功能分支
bugfix/customer-update-error   # 修复分支
hotfix/order-status-bug       # 热修复分支

# 分支操作流程
git checkout -b feature/order-batch-import
# 开发完成后
git push origin feature/order-batch-import
# 创建Pull Request
# 代码审查通过后合并到develop分支
```

## 4. 最佳实践

### 4.1 后端最佳实践

#### 4.1.1 服务层设计
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. 参数验证
        validateRequest(request);
        
        // 2. 业务逻辑处理
        Order order = buildOrder(request);
        
        // 3. 数据持久化
        orderRepository.insert(order);
        
        // 4. 返回结果
        return buildOrderResponse(order);
    }
    
    private void validateRequest(OrderCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (request.getCustomerId() == null) {
            throw new OrderValidationException("客户ID不能为空");
        }
        // 更多验证逻辑...
    }
}
```

#### 4.1.2 数据访问层
```java
@Repository
public interface OrderRepository extends BaseMapper<Order> {
    
    /**
     * 根据平台订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE platform_order_id = #{platformOrderId}")
    Order selectByPlatformOrderId(@Param("platformOrderId") String platformOrderId);
    
    /**
     * 分页查询订单列表
     */
    @Select("<script>" +
            "SELECT * FROM orders WHERE 1=1 " +
            "<if test='status != null'>AND order_status = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (platform_order_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR sales_id LIKE CONCAT('%', #{keyword}, '%'))</if> " +
            "ORDER BY created_at DESC " +
            "</script>")
    IPage<Order> selectOrderList(IPage<Order> page, @Param("status") String status, @Param("keyword") String keyword);
}
```

### 4.2 前端最佳实践

#### 4.2.1 状态管理
```typescript
// 使用 Pinia 进行状态管理
export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [] as IOrderResponse[],
    currentOrder: null as IOrderResponse | null,
    loading: false,
    pagination: {
      current: 1,
      pageSize: 20,
      total: 0
    }
  }),
  
  getters: {
    orderCount: (state) => state.orders.length,
    hasOrders: (state) => state.orders.length > 0
  },
  
  actions: {
    async fetchOrders(params: OrderListRequest) {
      try {
        this.loading = true
        const response = await orderApi.getOrderList(params)
        this.orders = response.data.records
        this.pagination = {
          current: response.data.current,
          pageSize: response.data.size,
          total: response.data.total
        }
      } catch (error) {
        console.error('获取订单列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
```

#### 4.2.2 API调用
```typescript
// API服务封装
export const orderApi = {
  // 获取订单列表
  async getOrderList(params: OrderListRequest): Promise<ApiResponse<IPage<IOrderResponse>>> {
    return request.get('/orders', { params })
  },
  
  // 创建订单
  async createOrder(data: OrderCreateRequest): Promise<ApiResponse<IOrderResponse>> {
    return request.post('/orders', data)
  },
  
  // 更新订单
  async updateOrder(id: number, data: OrderUpdateRequest): Promise<ApiResponse<IOrderResponse>> {
    return request.put(`/orders/${id}`, data)
  },
  
  // 删除订单
  async deleteOrder(id: number): Promise<ApiResponse<boolean>> {
    return request.delete(`/orders/${id}`)
  }
}
```

#### 4.2.3 错误处理
```typescript
// 统一错误处理
export const useErrorHandler = () => {
  const handleApiError = (error: any, defaultMessage = '操作失败') => {
    let message = defaultMessage
    
    if (error.response?.data?.message) {
      message = error.response.data.message
    } else if (error.message) {
      message = error.message
    }
    
    ElMessage.error(message)
    console.error('API调用失败:', error)
  }
  
  const handleValidationError = (errors: Record<string, string[]>) => {
    const messages = Object.values(errors).flat()
    messages.forEach(message => ElMessage.warning(message))
  }
  
  return {
    handleApiError,
    handleValidationError
  }
}
```

## 5. 测试规范

### 5.1 单元测试
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    @Test
    void createOrder_WithValidRequest_ShouldReturnOrderResponse() {
        // Given
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(1L);
        request.setOrderItems(Arrays.asList(new OrderItemCreateRequest()));
        
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("测试客户");
        
        when(customerRepository.selectById(1L)).thenReturn(customer);
        when(orderRepository.insert(any(Order.class))).thenReturn(1);
        
        // When
        OrderResponse response = orderService.createOrder(request);
        
        // Then
        assertNotNull(response);
        assertEquals("DRAFT", response.getOrderStatus());
        verify(orderRepository).insert(any(Order.class));
    }
}
```

### 5.2 集成测试
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class OrderControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Given
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(1L);
        
        // When
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/orders", request, OrderResponse.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
```

## 6. 性能优化

### 6.1 数据库优化
```sql
-- 索引优化
CREATE INDEX idx_orders_status_created ON orders(order_status, created_at);
CREATE INDEX idx_orders_customer_created ON orders(customer_id, created_at);
CREATE INDEX idx_orders_platform_id ON orders(platform_order_id);

-- 查询优化
SELECT o.*, c.name as customer_name 
FROM orders o 
LEFT JOIN customers c ON o.customer_id = c.id 
WHERE o.order_status = ? 
ORDER BY o.created_at DESC 
LIMIT ?, ?;
```

### 6.2 缓存策略
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getOrderById(Long id) {
        return orderRepository.selectById(id);
    }
    
    @CacheEvict(value = "orders", key = "#order.id")
    public void updateOrder(Order order) {
        orderRepository.updateById(order);
    }
}
```

### 6.3 前端优化
```typescript
// 虚拟滚动优化
const useVirtualScroll = (items: any[], itemHeight: number) => {
  const containerHeight = 400
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  
  const visibleItems = computed(() => {
    const start = Math.floor(scrollTop.value / itemHeight)
    const end = start + visibleCount
    return items.slice(start, end)
  })
  
  return { visibleItems }
}

// 防抖优化
const useDebounce = (fn: Function, delay: number) => {
  const timeoutRef = ref<NodeJS.Timeout>()
  
  return (...args: any[]) => {
    if (timeoutRef.value) {
      clearTimeout(timeoutRef.value)
    }
    timeoutRef.value = setTimeout(() => fn(...args), delay)
  }
}
```

## 7. 部署指南

### 7.1 构建配置
```xml
<!-- Maven构建配置 -->
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 7.2 环境配置
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/yibang_erp
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  redis:
    host: prod-redis
    port: 6379
    password: ${REDIS_PASSWORD}

logging:
  level:
    com.yibang.erp: INFO
  file:
    name: logs/application.log
```

### 7.3 Docker部署
```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY target/yibang-erp-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: yibang_erp
  
  redis:
    image: redis:7-alpine
```

## 8. 监控和日志

### 8.1 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 8.2 性能监控
```java
@Component
public class OrderPerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public OrderPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordOrderCreation(long duration) {
        meterRegistry.timer("order.creation.duration").record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void incrementOrderCount() {
        meterRegistry.counter("order.count").increment();
    }
}
```

---

**文档版本**：v1.0.0  
**最后更新**：2024年1月15日  
**维护人员**：开发团队
