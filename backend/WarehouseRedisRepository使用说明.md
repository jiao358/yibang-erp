# WarehouseRedisRepository 使用说明

## 概述

`WarehouseRedisRepository` 是基于Redis对 `WarehouseRepository` 进行缓存封装的Repository，用于防止数据库击穿，提高查询性能。

## 主要功能

### 1. 缓存策略
- **缓存过期时间**: 5分钟
- **缓存键前缀**:
  - `warehouse:` - 单个仓库缓存
  - `warehouse:code:company:` - 按编码+公司+状态缓存
  - `warehouse:company:` - 按公司ID缓存
  - `warehouse:status:` - 按状态缓存

### 2. 核心方法

#### 根据ID查询仓库
```java
Warehouse warehouse = warehouseRedisRepository.selectById(warehouseId);
```

#### 根据编码、公司ID和状态查询仓库（对应原代码）
```java
// 原代码：
QueryWrapper<Warehouse> warehouseWrapper = new QueryWrapper<>();
warehouseWrapper.eq("warehouse_code", warehouseCode);
warehouseWrapper.eq("status", "ACTIVE");
warehouseWrapper.eq("deleted", 0);
warehouseWrapper.eq("company_id", supplierCompanyId);
Warehouse warehouse = warehouseRepository.selectOne(warehouseWrapper);

// 替换为：
Warehouse warehouse = warehouseRedisRepository.findByCodeAndCompanyAndStatus(
    warehouseCode, supplierCompanyId, "ACTIVE");
```

#### 根据公司ID查询仓库列表
```java
List<Warehouse> warehouses = warehouseRedisRepository.findByCompanyId(companyId);
```

#### 根据状态查询仓库列表
```java
List<Warehouse> warehouses = warehouseRedisRepository.findByStatus("ACTIVE");
```

### 3. 数据修改方法

#### 插入仓库
```java
int result = warehouseRedisRepository.insert(warehouse);
```

#### 更新仓库
```java
int result = warehouseRedisRepository.updateById(warehouse);
```

#### 删除仓库
```java
int result = warehouseRedisRepository.deleteById(warehouseId);
```

### 4. 缓存管理方法

#### 手动刷新缓存
```java
warehouseRedisRepository.refreshWarehouseCache(warehouseId);
```

#### 清除所有仓库缓存
```java
warehouseRedisRepository.clearAllWarehouseCache();
```

## 使用示例

### 替换原有查询代码

**原代码**:
```java
QueryWrapper<Warehouse> warehouseWrapper = new QueryWrapper<>();
warehouseWrapper.eq("warehouse_code", warehouseCode);
warehouseWrapper.eq("status", "ACTIVE");
warehouseWrapper.eq("deleted", 0);
warehouseWrapper.eq("company_id", supplierCompanyId);
Warehouse warehouse = warehouseRepository.selectOne(warehouseWrapper);
```

**替换后**:
```java
@Autowired
private WarehouseRedisRepository warehouseRedisRepository;

// 直接调用缓存方法
Warehouse warehouse = warehouseRedisRepository.findByCodeAndCompanyAndStatus(
    warehouseCode, supplierCompanyId, "ACTIVE");
```

### 在Service中使用

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final WarehouseRedisRepository warehouseRedisRepository;
    
    public void processOrder(String warehouseCode, Long supplierCompanyId) {
        // 使用缓存查询，提高性能
        Warehouse warehouse = warehouseRedisRepository.findByCodeAndCompanyAndStatus(
            warehouseCode, supplierCompanyId, "ACTIVE");
            
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在或已停用");
        }
        
        // 处理订单逻辑...
    }
}
```

## 缓存特性

### 1. 缓存穿透防护
- 当缓存未命中时，自动查询数据库
- 查询结果自动缓存，避免重复查询
- 异常时降级到数据库查询

### 2. 缓存一致性
- 数据修改时自动清除相关缓存
- 支持手动刷新缓存
- 支持清除所有缓存

### 3. 性能优化
- 多级缓存键设计，提高命中率
- 批量查询时同时缓存单个对象
- 异常处理不影响业务逻辑

## 注意事项

1. **Redis配置**: 确保Redis服务正常运行，且配置了正确的序列化器
2. **异常处理**: 缓存异常时会自动降级到数据库查询
3. **内存使用**: 缓存过期时间为5分钟，可根据需要调整
4. **数据一致性**: 修改数据时会自动清除相关缓存，保证数据一致性

## 依赖要求

- Spring Boot 3.x
- MyBatis Plus
- Redis
- Jackson (用于序列化)

## 配置示例

确保在 `RedisConfig` 中配置了正确的序列化器：

```java
@Bean
public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    // 配置Jackson2JsonRedisSerializer支持LocalDateTime
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
    
    Jackson2JsonRedisSerializer<Object> serializer = 
        new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    
    // 配置RedisTemplate...
}
```
