# 生产环境登录 403 根因与一键修复指南（最小改动）

## 背景与根因（一句话）
- 生产外部配置启用了 server.servlet.context-path=/api，而控制器类路由写的是 @RequestMapping("/api/...")，两者叠加成 /api/api/... 导致登录匹配不到控制器，被 Spring Security 403。

## 一次性修复步骤（命令可直接执行）

### 1) 去除生产 context-path 外部配置（保持控制器仍用 /api/...）
```bash
# 备份
cp /opt/yibang-erp/config/application-prod.yml /opt/yibang-erp/config/application-prod.yml.bak.$(date +%F_%H%M%S)

# 注释/移除 context-path（幂等）
sed -i -E 's/^([[:space:]]*)context-path:[[:space:]]*(.+)$/\1# context-path: \2 (removed)/' /opt/yibang-erp/config/application-prod.yml

# 若存在错误的单行 server.servlet: 标量，统一注释（防止绑定异常）
sed -i -E 's/^([[:space:]]*)server\.servlet:[[:space:]]*(.+)$/\1# server.servlet: \2 (removed)/' /opt/yibang-erp/config/application-prod.yml

# 查看确认
sed -n '1,140p' /opt/yibang-erp/config/application-prod.yml | cat
```

### 2) 规范 Nginx 反向代理（原样透传 /api 给后端）
```bash
# 检查当前配置
grep -n "location /api/" /www/server/panel/vhost/nginx/yibang-erp.conf
grep -n "proxy_pass" /www/server/panel/vhost/nginx/yibang-erp.conf

# 如发现是 proxy_pass http://localhost:7102/api/; 则改为不带 /api 的：
sed -i 's|proxy_pass http://localhost:7102/api/;|proxy_pass http://localhost:7102;|g' /www/server/panel/vhost/nginx/yibang-erp.conf

# 语法检查并重载
/www/server/nginx/sbin/nginx -t && /www/server/nginx/sbin/nginx -s reload
```

### 3) 重启后端并校验
```bash
systemctl restart yibang-erp
sleep 3
systemctl status yibang-erp --no-pager
journalctl -u yibang-erp -n 80 --no-pager | tail -n +1
```

### 4) 验证（先本机直连，再域名）
```bash
# 健康检查（应 200）
curl -v http://localhost:7102/api/health | cat

# 登录（应返回业务结果 200/400/422，非 403）
curl -v -X POST http://localhost:7102/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | cat

# 经域名验证
curl -v -X POST https://www.yibangkj.com/api/auth/login \
  -H "Origin: https://www.yibangkj.com" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | cat
```

## 常见陷阱自检（按需执行）
```bash
# 1) Spring Security 日志应看到 /api/auth/login（而不是 /auth/login）
journalctl -u yibang-erp -n 200 --no-pager | grep -E "Securing|JWT认证过滤器" | tail -n 20

# 2) MySQL 5.7 方言（避免 57/8 不兼容）
grep -n -A2 -B2 "hibernate.*dialect" /opt/yibang-erp/config/application-prod.yml
# 如为 MySQL8Dialect，改为 MySQL57Dialect：
sed -i 's|org.hibernate.dialect.MySQL8Dialect|org.hibernate.dialect.MySQL57Dialect|g' /opt/yibang-erp/config/application-prod.yml

# 3) JWT 密钥不能为空
grep -n -A2 -B2 '^jwt:' /opt/yibang-erp/config/application-prod.yml
# 如 secret 为空：
sed -i -E 's/^([[:space:]]*secret:)[[:space:]]*$/\1 prod-yibang-erp-super-secure-jwt-2025-rotate/' /opt/yibang-erp/config/application-prod.yml

# 4) Nginx CORS 预检（204）
curl -v -X OPTIONS https://www.yibangkj.com/api/auth/login \
  -H "Origin: https://www.yibangkj.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" | cat
```

## 若必须保留 context-path=/api（替代方案，需改代码并重发版）
- 控制器类级路由去掉 /api 前缀，例如：@RequestMapping("/auth")、@RequestMapping("/ai")。
- SecurityConfig 使用应用内路径匹配（不带 /api），并保证顺序：
  - "/auth/**".permitAll() 必须在 "/**".authenticated() 或 "/api/**".authenticated() 之前。
- Nginx 继续 proxy_pass http://localhost:7102;，浏览器走 /api/...，后端由 context-path 吸收前缀。

## 期望日志特征（成功）
- Security: Securing POST /api/auth/login
- JWT 过滤器：处理请求: POST /api/auth/login 且 跳过登录接口认证
- 登录返回非 403（200/4xx 业务态）

—— 最小改动生产修复手册；严格按顺序执行即可稳定消除登录 403。
