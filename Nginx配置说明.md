# Nginx配置说明

## 概述

脚本**不需要您手动配置Nginx地址**，所有配置都是自动生成的。但如果您需要自定义配置，可以修改脚本中的参数。

## 自动配置

### 默认配置
脚本会自动生成以下Nginx配置：

```nginx
# 负载均衡配置
upstream yibang_backend {
    server 127.0.0.1:7102 weight=3 max_fails=3 fail_timeout=30s;
    server 127.0.0.1:7103 weight=1 max_fails=3 fail_timeout=30s backup;
    keepalive 32;
}

# 健康检查端点 (端口8080)
server {
    listen 8080;
    server_name localhost;
    location /health {
        proxy_pass http://yibang_backend/actuator/health;
    }
}

# API代理配置 (端口80)
server {
    listen 80;
    server_name localhost;
    location /api {
        proxy_pass http://yibang_backend;
    }
}
```

### 自动部署流程
1. 生成配置文件到 `/tmp/yibang-erp-migration.conf`
2. 复制到 `/etc/nginx/sites-available/`
3. 创建软链接到 `/etc/nginx/sites-enabled/`
4. 测试配置：`sudo nginx -t`
5. 重新加载：`sudo systemctl reload nginx`

## 自定义配置

如果您需要自定义配置，可以修改脚本中的这些参数：

### 在脚本顶部修改
```bash
# 可自定义的Nginx配置
NGINX_SERVER_NAME="localhost"  # 可以改为您的域名
NGINX_HTTP_PORT=80            # HTTP端口
NGINX_HEALTH_PORT=8080        # 健康检查端口
BACKEND_HOST="127.0.0.1"      # 后端主机地址
```

### 配置示例

#### 1. 使用自定义域名
```bash
NGINX_SERVER_NAME="api.yourdomain.com"
```

#### 2. 使用自定义端口
```bash
NGINX_HTTP_PORT=8080
NGINX_HEALTH_PORT=8081
```

#### 3. 使用外部后端服务器
```bash
BACKEND_HOST="192.168.1.100"
```

## 系统要求

### 1. 安装Nginx
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
# 或者
sudo dnf install nginx
```

### 2. 启动Nginx服务
```bash
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 3. 检查Nginx状态
```bash
sudo systemctl status nginx
```

### 4. 确保端口可用
```bash
# 检查端口占用
sudo netstat -tlnp | grep :80
sudo netstat -tlnp | grep :8080

# 如果端口被占用，可以停止占用服务或修改端口
```

## 权限要求

脚本需要以下权限：

### 1. Nginx配置权限
```bash
# 需要sudo权限来修改Nginx配置
sudo cp /tmp/yibang-erp-migration.conf /etc/nginx/sites-available/
sudo ln -sf /etc/nginx/sites-available/yibang-erp-migration /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 2. 文件权限
```bash
# 确保脚本有执行权限
chmod +x start-backend-migration.sh
```

## 故障排查

### 1. Nginx未安装
```bash
# 错误信息
[WARNING] Nginx未安装，将跳过负载均衡配置

# 解决方案
sudo apt install nginx  # Ubuntu/Debian
sudo yum install nginx  # CentOS/RHEL
```

### 2. 端口被占用
```bash
# 检查端口占用
sudo lsof -i :80
sudo lsof -i :8080

# 停止占用服务
sudo systemctl stop apache2  # 如果Apache占用80端口
```

### 3. 权限不足
```bash
# 错误信息
sudo: nginx: command not found

# 解决方案
# 确保Nginx已安装且路径正确
which nginx
sudo systemctl status nginx
```

### 4. 配置测试失败
```bash
# 手动测试配置
sudo nginx -t

# 查看错误日志
sudo tail -f /var/log/nginx/error.log
```

## 高级配置

### 1. SSL/HTTPS配置
如果您需要HTTPS，可以修改脚本生成SSL配置：

```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    location /api {
        proxy_pass http://yibang_backend;
    }
}

server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

### 2. 负载均衡策略
可以修改负载均衡策略：

```nginx
upstream yibang_backend {
    # 轮询 (默认)
    server 127.0.0.1:7102;
    server 127.0.0.1:7103;
    
    # 权重
    server 127.0.0.1:7102 weight=3;
    server 127.0.0.1:7103 weight=1;
    
    # 最少连接
    least_conn;
    
    # IP哈希
    ip_hash;
}
```

### 3. 缓存配置
可以添加缓存配置：

```nginx
location /api {
    proxy_pass http://yibang_backend;
    
    # 缓存配置
    proxy_cache my_cache;
    proxy_cache_valid 200 302 10m;
    proxy_cache_valid 404 1m;
}
```

## 监控和日志

### 1. 访问日志
```bash
# 查看访问日志
sudo tail -f /var/log/nginx/access.log
```

### 2. 错误日志
```bash
# 查看错误日志
sudo tail -f /var/log/nginx/error.log
```

### 3. 状态监控
```bash
# 检查后端状态
curl http://localhost:8080/backend-status

# 检查健康状态
curl http://localhost:8080/health
```

## 总结

**您不需要手动配置Nginx地址**，脚本会自动：

1. ✅ 生成完整的Nginx配置
2. ✅ 自动部署到正确位置
3. ✅ 测试配置并重新加载
4. ✅ 支持自定义配置参数

您只需要：
1. 安装Nginx
2. 启动Nginx服务
3. 确保端口可用
4. 运行脚本即可

如果需要自定义配置，只需修改脚本顶部的配置参数即可。
