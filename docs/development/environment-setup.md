# 环境配置说明

## 概述

本项目采用混合配置策略，支持多环境部署：

- **开发环境（dev）**：使用Docker MySQL和Redis，适合本地开发
- **测试环境（test）**：可选择Docker或独立部署，适合测试验证
- **生产环境（prod）**：使用独立MySQL和Redis服务器，适合生产部署
- **本地环境（local）**：支持环境变量覆盖，适合个性化配置

## 环境配置文件

### 主配置文件
- `application.yml` - 通用配置和默认值
- `application-dev.yml` - 开发环境配置
- `application-test.yml` - 测试环境配置
- `application-prod.yml` - 生产环境配置
- `application-local.yml` - 本地环境配置

### 环境变量配置
- `env.example` - 环境变量示例文件
- 复制为 `.env` 文件并填入实际值

## 快速开始

### 1. 开发环境（推荐）

#### 使用Docker启动数据库
```bash
cd docker
docker-compose -f docker-compose-dev.yml up -d
```

#### 启动后端服务
```bash
cd backend
# 使用开发环境配置
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 2. 本地环境（自定义）

#### 配置环境变量
```bash
cd backend
cp env.example .env
# 编辑.env文件，填入实际配置
```

#### 启动后端服务
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

### 3. 生产环境

#### 设置环境变量
```bash
export MYSQL_HOST=your-mysql-host
export MYSQL_PORT=3306
export MYSQL_DATABASE=yibang_erp_prod
export MYSQL_USERNAME=your-username
export MYSQL_PASSWORD=your-password
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password
export JWT_SECRET=your-jwt-secret
```

#### 启动后端服务
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## 配置说明

### 数据库配置

#### 开发环境
- 使用Docker MySQL 8.0
- 端口：3306
- 数据库：yibang_erp_dev
- 用户名：root
- 密码：123456

#### 生产环境
- 使用独立MySQL服务器
- 支持集群部署
- 通过环境变量配置连接信息

### Redis配置

#### 开发环境
- 使用Docker Redis 7.0
- 端口：6379
- 无密码
- 数据库：0

#### 生产环境
- 使用独立Redis服务器
- 支持集群部署
- 通过环境变量配置连接信息

### 安全配置

#### JWT配置
- 开发环境：使用固定密钥（仅用于开发）
- 生产环境：通过环境变量配置密钥
- 支持密钥轮换

#### 权限配置
- 开发环境：详细日志，便于调试
- 生产环境：最小化日志，提高安全性

### 监控配置

#### 开发环境
- 暴露所有监控端点
- 详细健康检查信息
- 开启SQL日志

#### 生产环境
- 仅暴露必要监控端点
- 隐藏敏感信息
- 关闭SQL日志

## 环境切换

### 通过启动参数
```bash
# 开发环境
mvn spring-boot:run -Dspring.profiles.active=dev

# 测试环境
mvn spring-boot:run -Dspring.profiles.active=test

# 生产环境
mvn spring-boot:run -Dspring.profiles.active=prod

# 本地环境
mvn spring-boot:run -Dspring.profiles.active=local
```

### 通过环境变量
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

### 通过配置文件
在 `application.yml` 中修改：
```yaml
spring:
  profiles:
    active: dev  # 改为目标环境
```

## 注意事项

### 开发环境
- 数据会持久化到Docker volumes
- 重启容器数据不会丢失
- 适合频繁重启和调试

### 生产环境
- 必须使用环境变量配置敏感信息
- 不要将生产配置提交到代码仓库
- 建议使用配置管理工具（如Vault）

### 安全建议
- 定期更换JWT密钥
- 使用强密码
- 限制数据库访问权限
- 启用SSL连接

## 故障排除

### 常见问题

#### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 验证连接参数是否正确
- 确认网络连接是否正常

#### 2. Redis连接失败
- 检查Redis服务是否启动
- 验证端口是否被占用
- 确认密码配置是否正确

#### 3. 配置文件加载失败
- 检查配置文件语法是否正确
- 确认文件路径是否正确
- 验证环境变量是否设置

### 日志查看
```bash
# 查看应用日志
tail -f logs/yibang-erp.log

# 查看Docker容器日志
docker logs yibang_erp_mysql_dev
docker logs yibang_erp_redis_dev
```
