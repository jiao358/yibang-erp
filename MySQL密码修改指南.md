# MySQL 5.7 Root密码修改指南

## 🚀 快速修改方法

### 方法1: 使用mysqladmin（推荐，需要知道当前密码）

```bash
# 如果知道当前密码
mysqladmin -u root -p password '新密码'

# 示例
mysqladmin -u root -p password 'MyNewPassword123!'
```

### 方法2: 使用SQL命令

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 在MySQL中执行（选择其中一种）
ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;

# 或者使用（兼容性更好）
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('新密码');

# 3. 退出
EXIT;
```

### 方法3: 忘记密码时的重置（不需要知道当前密码）

```bash
# 1. 停止MySQL服务
sudo systemctl stop mysql
# 或者
sudo service mysql stop

# 2. 以安全模式启动MySQL
sudo mysqld_safe --skip-grant-tables --skip-networking &

# 3. 在另一个终端登录MySQL（无需密码）
mysql -u root

# 4. 在MySQL中重置密码
USE mysql;
UPDATE user SET authentication_string = PASSWORD('新密码') WHERE User = 'root' AND Host = 'localhost';
FLUSH PRIVILEGES;
EXIT;

# 5. 停止安全模式的MySQL
sudo pkill mysqld

# 6. 正常启动MySQL
sudo systemctl start mysql
# 或者
sudo service mysql start

# 7. 测试新密码
mysql -u root -p
```

## 🛠️ 使用自动化脚本

我为您创建了一个自动化脚本 `mysql-password-reset.sh`，使用方法：

```bash
# 给脚本执行权限
chmod +x mysql-password-reset.sh

# 运行脚本（需要sudo权限）
sudo ./mysql-password-reset.sh
```

脚本特点：
- ✅ 交互式菜单，选择修改方法
- ✅ 密码确认，防止输入错误
- ✅ 自动检测MySQL服务状态
- ✅ 支持多种MySQL启动方式
- ✅ 密码修改后自动测试
- ✅ 彩色输出，易于阅读

## 🔍 验证密码修改

修改密码后，使用以下命令验证：

```bash
# 测试登录
mysql -u root -p

# 查看用户信息
mysql -u root -p -e "SELECT User, Host FROM mysql.user WHERE User='root';"

# 查看MySQL版本
mysql -u root -p -e "SELECT VERSION();"
```

## ⚠️ 注意事项

1. **密码安全**: 使用强密码，包含大小写字母、数字和特殊字符
2. **权限检查**: 确保有足够的系统权限执行MySQL操作
3. **服务状态**: 确保MySQL服务正在运行（方法1和2）
4. **备份数据**: 在修改密码前，建议备份重要数据
5. **防火墙**: 如果从远程连接，确保防火墙允许MySQL端口（3306）

## 🆘 常见问题

### Q: 提示"Access denied"错误
A: 检查用户名和密码是否正确，或者使用忘记密码的重置方法

### Q: 提示"MySQL service not found"
A: 检查MySQL是否正确安装，服务名称可能是`mysqld`而不是`mysql`

### Q: 修改后仍然无法登录
A: 检查是否有多个root用户（不同Host），使用以下命令查看：
```sql
SELECT User, Host FROM mysql.user WHERE User='root';
```

### Q: 忘记新设置的密码
A: 重新使用方法3（忘记密码重置方法）

## 📝 推荐密码格式

- 长度：至少8位，推荐12位以上
- 包含：大写字母、小写字母、数字、特殊字符
- 示例：`MySecure123!`、`Admin@2024#`、`Root$Pass456`

## 🔧 后续配置

修改密码后，您可能需要：

1. **更新应用配置**: 修改应用程序中的数据库连接密码
2. **创建新用户**: 为应用创建专用的数据库用户
3. **配置远程访问**: 如果需要远程连接，配置相应的用户权限
4. **设置自动启动**: 确保MySQL服务开机自启

```bash
# 设置MySQL开机自启
sudo systemctl enable mysql
# 或者
sudo chkconfig mysqld on
```
