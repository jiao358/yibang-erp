#!/bin/bash

# 创建订单模板表的脚本
# 使用系统配置的MySQL账户信息

echo "正在创建 order_templates 表..."

# MySQL连接信息（从application-dev.yml中获取）
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD="Pino542635"
MYSQL_DATABASE="yibang_erp_dev"

# 执行SQL文件
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < database/init/ddl/11_order_templates_table.sql

if [ $? -eq 0 ]; then
    echo "✅ order_templates 表创建成功！"
    echo "✅ 已插入默认模板数据"
else
    echo "❌ order_templates 表创建失败！"
    exit 1
fi

echo "数据库表创建完成！"
