# UserRedisRepository 使用说明

## 概述

`UserRedisRepository` 是一个基于Redis的用户数据缓存Repository，它封装了`UserRepository`的所有查询方法，并添加了Redis缓存功能，有效防止数据库击穿。

## 主要特性

### 1. 缓存策略
- **缓存时间**: 5分钟自动过期
- **缓存穿透保护**: 缓存未命中时查询数据库并缓存结果
- **缓存雪崩保护**: 异常时直接查询数据库，保证服务可用性
- **多维度缓存**: 支持按ID、用户名、邮箱、手机号、公司ID、角色ID等多种查询维度缓存

### 2. 缓存键设计
```
user:{id}                    # 按用户ID缓存
user:username:{username}     # 按用户名缓存
user:email:{email}           # 按邮箱缓存
user:phone:{phone}           # 按手机号缓存
user:company:{companyId}     # 按公司ID缓存用户列表
user:role:{roleId}           # 按角色ID缓存用户列表
```

### 3. 自动缓存管理
- **查询时**: 自动从缓存获取，未命中时查询数据库并缓存
- **更新时**: 自动清除相关缓存
- **删除时**: 自动清除相关缓存
- **插入时**: 自动清除相关缓存

## 使用方法

### 1. 依赖注入
```java
@Autowired
private UserRedisRepository userRedisRepository;
```

### 2. 基本查询方法

#### 根据ID查询用户
```java
User user = userRedisRepository.selectById(1L);
```

#### 根据用户名查询用户
```java
User user = userRedisRepository.findByUsername("admin");
```

#### 根据邮箱查询用户
```java
User user = userRedisRepository.findByEmail("admin@example.com");
```

#### 根据手机号查询用户
```java
User user = userRedisRepository.findByPhone("13800138000");
```

#### 根据公司ID查询用户列表
```java
List<User> users = userRedisRepository.findByCompanyId(1L);
```

#### 根据角色ID查询用户列表
```java
List<User> users = userRedisRepository.findByRoleId(1L);
```

### 3. 数据修改方法

#### 插入用户
```java
User user = new User();
user.setUsername("newuser");
user.setEmail("newuser@example.com");
// ... 设置其他属性
int result = userRedisRepository.insert(user);
```

#### 更新用户
```java
User user = userRedisRepository.selectById(1L);
user.setEmail("updated@example.com");
int result = userRedisRepository.updateById(user);
```

#### 删除用户
```java
int result = userRedisRepository.deleteById(1L);
```

### 4. 缓存管理方法

#### 手动刷新用户缓存
```java
userRedisRepository.refreshUserCache(1L);
```

#### 清除所有用户缓存
```java
userRedisRepository.clearAllUserCache();
```

## 配置要求

### 1. Redis配置
确保在`application.yml`中配置了Redis连接信息：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 2000ms
```

### 2. RedisTemplate配置
`UserRedisRepository`依赖`RedisTemplate<Object, Object>`，已在`RedisConfig`中配置：

```java
@Bean
public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    // 配置Jackson2JsonRedisSerializer用于对象序列化
    // 配置StringRedisSerializer用于键序列化
}
```

## 性能优化

### 1. 缓存命中率监控
```java
// 在日志中可以看到缓存命中情况
log.debug("从缓存获取用户: id={}", id);
log.debug("缓存未命中，查询数据库: id={}", id);
```

### 2. 缓存预热
```java
// 系统启动时预加载热点数据
@PostConstruct
public void warmUpCache() {
    // 预加载管理员用户
    userRedisRepository.findByUsername("admin");
    // 预加载其他热点用户
}
```

### 3. 缓存清理策略
- 用户数据更新时自动清理相关缓存
- 支持手动清理特定用户或全部用户缓存
- 5分钟自动过期，避免数据不一致

## 异常处理

### 1. Redis连接异常
当Redis不可用时，`UserRedisRepository`会自动降级到直接查询数据库，确保服务可用性。

### 2. 序列化异常
使用Jackson2JsonRedisSerializer进行对象序列化，支持复杂对象和集合类型。

### 3. 缓存穿透
通过缓存空值（null）和合理的缓存键设计，有效防止缓存穿透。

## 注意事项

1. **数据一致性**: 缓存有5分钟过期时间，对于实时性要求极高的场景，建议直接查询数据库
2. **内存使用**: 缓存会占用Redis内存，建议监控Redis内存使用情况
3. **并发安全**: Redis操作是原子性的，支持高并发访问
4. **异常降级**: 当Redis不可用时，自动降级到数据库查询，保证服务可用性

## 监控建议

1. **缓存命中率**: 监控日志中的缓存命中情况
2. **Redis性能**: 监控Redis的CPU、内存、连接数等指标
3. **数据库压力**: 监控数据库查询频率，评估缓存效果
4. **异常日志**: 监控Redis连接异常和序列化异常

## 扩展功能

### 1. 自定义缓存时间
可以修改`CACHE_EXPIRE_TIME`常量来调整缓存过期时间：

```java
private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(10); // 改为10分钟
```

### 2. 添加新的查询维度
可以按照现有模式添加新的查询方法，如按部门查询：

```java
public List<User> findByDepartment(String department) {
    // 实现逻辑
}
```

### 3. 缓存统计
可以添加缓存统计功能，监控缓存命中率和性能指标。
