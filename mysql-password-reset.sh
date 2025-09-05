#!/bin/bash

# =====================================================
# MySQL 5.7 Root密码修改脚本
# 创建时间: 2024-12-19
# 用途: 修改MySQL root用户密码
# =====================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_header() {
    echo "====================================================="
    print_message $BLUE "$1"
    echo "====================================================="
}

# 检查MySQL是否运行
check_mysql_status() {
    if systemctl is-active --quiet mysql 2>/dev/null || systemctl is-active --quiet mysqld 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# 方法1: 使用mysqladmin修改密码（需要知道当前密码）
method_mysqladmin() {
    print_header "方法1: 使用mysqladmin修改密码"
    
    if ! check_mysql_status; then
        print_message $RED "错误: MySQL服务未运行，请先启动MySQL服务"
        return 1
    fi
    
    read -p "请输入当前root密码: " -s current_password
    echo
    read -p "请输入新密码: " -s new_password
    echo
    read -p "请再次确认新密码: " -s confirm_password
    echo
    
    if [ "$new_password" != "$confirm_password" ]; then
        print_message $RED "错误: 两次输入的密码不一致"
        return 1
    fi
    
    print_message $YELLOW "正在修改密码..."
    
    if mysqladmin -u root -p"$current_password" password "$new_password" 2>/dev/null; then
        print_message $GREEN "密码修改成功！"
        return 0
    else
        print_message $RED "密码修改失败，请检查当前密码是否正确"
        return 1
    fi
}

# 方法2: 使用SQL命令修改密码
method_sql() {
    print_header "方法2: 使用SQL命令修改密码"
    
    if ! check_mysql_status; then
        print_message $RED "错误: MySQL服务未运行，请先启动MySQL服务"
        return 1
    fi
    
    read -p "请输入当前root密码: " -s current_password
    echo
    read -p "请输入新密码: " -s new_password
    echo
    read -p "请再次确认新密码: " -s confirm_password
    echo
    
    if [ "$new_password" != "$confirm_password" ]; then
        print_message $RED "错误: 两次输入的密码不一致"
        return 1
    fi
    
    print_message $YELLOW "正在修改密码..."
    
    # 创建临时SQL文件
    cat > /tmp/mysql_password_reset.sql << EOF
ALTER USER 'root'@'localhost' IDENTIFIED BY '$new_password';
FLUSH PRIVILEGES;
EOF
    
    if mysql -u root -p"$current_password" < /tmp/mysql_password_reset.sql 2>/dev/null; then
        print_message $GREEN "密码修改成功！"
        rm -f /tmp/mysql_password_reset.sql
        return 0
    else
        print_message $RED "密码修改失败，请检查当前密码是否正确"
        rm -f /tmp/mysql_password_reset.sql
        return 1
    fi
}

# 方法3: 忘记密码时的重置方法
method_reset() {
    print_header "方法3: 忘记密码时的重置方法"
    
    print_message $YELLOW "警告: 此方法将暂时停止MySQL服务，请确保没有其他应用正在使用数据库"
    read -p "是否继续？(y/N): " confirm
    
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        print_message $YELLOW "操作已取消"
        return 1
    fi
    
    read -p "请输入新密码: " -s new_password
    echo
    read -p "请再次确认新密码: " -s confirm_password
    echo
    
    if [ "$new_password" != "$confirm_password" ]; then
        print_message $RED "错误: 两次输入的密码不一致"
        return 1
    fi
    
    print_message $YELLOW "正在停止MySQL服务..."
    
    # 停止MySQL服务
    if systemctl stop mysql 2>/dev/null || systemctl stop mysqld 2>/dev/null; then
        print_message $GREEN "MySQL服务已停止"
    else
        print_message $YELLOW "尝试使用service命令停止MySQL..."
        service mysql stop 2>/dev/null || service mysqld stop 2>/dev/null || true
    fi
    
    print_message $YELLOW "正在以安全模式启动MySQL..."
    
    # 以跳过权限表的方式启动MySQL
    mysqld_safe --skip-grant-tables --skip-networking &
    MYSQL_PID=$!
    
    # 等待MySQL启动
    sleep 5
    
    print_message $YELLOW "正在重置密码..."
    
    # 创建重置密码的SQL文件
    cat > /tmp/mysql_password_reset.sql << EOF
USE mysql;
UPDATE user SET authentication_string = PASSWORD('$new_password') WHERE User = 'root' AND Host = 'localhost';
FLUSH PRIVILEGES;
EOF
    
    # 执行密码重置
    if mysql -u root < /tmp/mysql_password_reset.sql 2>/dev/null; then
        print_message $GREEN "密码重置成功！"
    else
        print_message $RED "密码重置失败"
        kill $MYSQL_PID 2>/dev/null || true
        rm -f /tmp/mysql_password_reset.sql
        return 1
    fi
    
    print_message $YELLOW "正在停止安全模式的MySQL进程..."
    kill $MYSQL_PID 2>/dev/null || pkill mysqld 2>/dev/null || true
    sleep 2
    
    print_message $YELLOW "正在正常启动MySQL服务..."
    if systemctl start mysql 2>/dev/null || systemctl start mysqld 2>/dev/null; then
        print_message $GREEN "MySQL服务已正常启动"
    else
        print_message $YELLOW "尝试使用service命令启动MySQL..."
        service mysql start 2>/dev/null || service mysqld start 2>/dev/null || true
    fi
    
    # 清理临时文件
    rm -f /tmp/mysql_password_reset.sql
    
    print_message $GREEN "密码重置完成！"
    return 0
}

# 测试新密码
test_password() {
    local password=$1
    print_message $YELLOW "正在测试新密码..."
    
    if mysql -u root -p"$password" -e "SELECT 1;" >/dev/null 2>&1; then
        print_message $GREEN "密码测试成功！可以正常登录MySQL"
        return 0
    else
        print_message $RED "密码测试失败！"
        return 1
    fi
}

# 主菜单
main_menu() {
    print_header "MySQL 5.7 Root密码修改工具"
    
    echo "请选择修改密码的方法："
    echo "1) 使用mysqladmin修改密码（需要知道当前密码）"
    echo "2) 使用SQL命令修改密码（需要知道当前密码）"
    echo "3) 忘记密码时的重置方法（不需要知道当前密码）"
    echo "4) 退出"
    echo
    
    read -p "请输入选项 (1-4): " choice
    
    case $choice in
        1)
            method_mysqladmin
            ;;
        2)
            method_sql
            ;;
        3)
            method_reset
            ;;
        4)
            print_message $YELLOW "退出程序"
            exit 0
            ;;
        *)
            print_message $RED "无效选项，请重新选择"
            main_menu
            ;;
    esac
}

# 检查是否以root权限运行
if [ "$EUID" -ne 0 ]; then
    print_message $RED "错误: 请使用sudo运行此脚本"
    echo "使用方法: sudo ./mysql-password-reset.sh"
    exit 1
fi

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    print_message $RED "错误: MySQL未安装或未在PATH中找到"
    exit 1
fi

# 运行主菜单
main_menu

print_message $GREEN "脚本执行完成！"
