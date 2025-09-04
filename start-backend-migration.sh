#!/bin/bash

# 易邦ERP后端迁移管理脚本
# 支持多端口部署、负载均衡、优雅关闭
# 使用方法: ./start-backend-migration.sh [命令] [参数]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置参数
DEFAULT_PORT=7102
BACKUP_PORT=7103
MIGRATION_PORT=7104
NGINX_CONFIG_DIR="/etc/nginx/sites-available"
NGINX_CONFIG_FILE="yibang-erp-migration"
FRONTEND_CONFIG_FILE="frontend/src/utils/request.ts"

# 可自定义的Nginx配置
NGINX_SERVER_NAME="localhost"  # 可以改为您的域名
NGINX_HTTP_PORT=80            # HTTP端口
NGINX_HEALTH_PORT=8080        # 健康检查端口
BACKEND_HOST="127.0.0.1"      # 后端主机地址

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_migration() {
    echo -e "${PURPLE}[MIGRATION]${NC} $1"
}

log_nginx() {
    echo -e "${CYAN}[NGINX]${NC} $1"
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装Java 17"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java版本过低，需要Java 17或更高版本"
        exit 1
    fi
    
    log_success "Java环境检查通过: $(java -version 2>&1 | head -n 1)"
}

# 检查Maven环境
check_maven() {
    log_info "检查Maven环境..."
    if ! command -v mvn &> /dev/null && [ ! -f "./mvnw" ]; then
        log_error "Maven未安装且未找到mvnw包装器"
        exit 1
    fi
    
    if [ -f "./mvnw" ]; then
        log_success "使用Maven包装器"
        MVN_CMD="./mvnw"
    else
        log_success "使用系统Maven"
        MVN_CMD="mvn"
    fi
}

# 构建后端应用
build_backend() {
    log_info "构建后端应用..."
    cd backend
    
    # 清理并构建
    $MVN_CMD clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        log_success "后端应用构建成功"
    else
        log_error "后端应用构建失败"
        exit 1
    fi
    
    cd ..
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -i :$port > /dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 获取可用端口
get_available_port() {
    local start_port=$1
    local port=$start_port
    
    while check_port $port; do
        port=$((port + 1))
        if [ $port -gt $((start_port + 10)) ]; then
            log_error "无法找到可用端口，请检查端口范围"
            exit 1
        fi
    done
    
    echo $port
}

# 启动后端实例
start_backend_instance() {
    local port=$1
    local profile=$2
    local instance_name=$3
    local jar_file="backend/target/yibang-erp-*.jar"
    
    log_migration "启动后端实例: $instance_name (端口: $port)"
    
    # 查找JAR文件
    JAR_PATH=$(ls $jar_file 2>/dev/null | head -n 1)
    if [ -z "$JAR_PATH" ]; then
        log_error "未找到JAR文件，请先构建应用"
        exit 1
    fi
    
    # 设置JVM参数
    JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    
    # 启动应用
    nohup java $JVM_OPTS -jar "$JAR_PATH" --spring.profiles.active=$profile --server.port=$port > "backend-${port}.log" 2>&1 &
    
    # 获取进程ID
    BACKEND_PID=$!
    echo $BACKEND_PID > "backend-${port}.pid"
    
    log_success "后端实例 $instance_name 已启动，PID: $BACKEND_PID，端口: $port"
    log_info "日志文件: backend-${port}.log"
    log_info "PID文件: backend-${port}.pid"
    
    # 等待应用启动
    log_info "等待应用启动..."
    sleep 5
    
    # 检查应用是否启动成功
    if ps -p $BACKEND_PID > /dev/null; then
        # 检查健康状态
        if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            log_success "后端实例 $instance_name 启动成功并健康检查通过！"
            return 0
        else
            log_warning "后端实例 $instance_name 启动但健康检查失败"
            return 1
        fi
    else
        log_error "后端实例 $instance_name 启动失败，请检查日志文件: backend-${port}.log"
        return 1
    fi
}

# 停止后端实例
stop_backend_instance() {
    local port=$1
    local instance_name=$2
    
    log_migration "停止后端实例: $instance_name (端口: $port)"
    
    if [ -f "backend-${port}.pid" ]; then
        PID=$(cat "backend-${port}.pid")
        if ps -p $PID > /dev/null; then
            # 优雅关闭
            log_info "发送SIGTERM信号进行优雅关闭..."
            kill $PID
            
            # 等待优雅关闭
            local count=0
            while ps -p $PID > /dev/null && [ $count -lt 30 ]; do
                sleep 1
                count=$((count + 1))
            done
            
            # 如果还在运行，强制关闭
            if ps -p $PID > /dev/null; then
                log_warning "优雅关闭超时，强制关闭进程"
                kill -9 $PID
            fi
            
            log_success "后端实例 $instance_name 已停止 (PID: $PID)"
            rm -f "backend-${port}.pid"
        else
            log_warning "后端实例 $instance_name 未运行"
            rm -f "backend-${port}.pid"
        fi
    else
        log_warning "未找到PID文件: backend-${port}.pid"
    fi
}

# 检查Nginx是否安装
check_nginx() {
    if ! command -v nginx &> /dev/null; then
        log_warning "Nginx未安装，将跳过负载均衡配置"
        return 1
    fi
    return 0
}

# 生成Nginx配置
generate_nginx_config() {
    local primary_port=$1
    local backup_port=$2
    
    log_nginx "生成Nginx负载均衡配置..."
    
    cat > "/tmp/${NGINX_CONFIG_FILE}.conf" << EOF
# 易邦ERP后端负载均衡配置
upstream yibang_backend {
    server ${BACKEND_HOST}:${primary_port} weight=3 max_fails=3 fail_timeout=30s;
    server ${BACKEND_HOST}:${backup_port} weight=1 max_fails=3 fail_timeout=30s backup;
    keepalive 32;
}

# 健康检查端点
server {
    listen ${NGINX_HEALTH_PORT};
    server_name ${NGINX_SERVER_NAME};
    
    location /health {
        proxy_pass http://yibang_backend/actuator/health;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
    
    location /backend-status {
        access_log off;
        return 200 "Backend Status: Primary=${primary_port}, Backup=${backup_port}";
        add_header Content-Type text/plain;
    }
}

# API代理配置
server {
    listen ${NGINX_HTTP_PORT};
    server_name ${NGINX_SERVER_NAME};
    
    location /api {
        proxy_pass http://yibang_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }
}
EOF
    
    log_success "Nginx配置文件已生成: /tmp/${NGINX_CONFIG_FILE}.conf"
}

# 更新前端配置
update_frontend_config() {
    local new_port=$1
    
    log_info "更新前端API配置到端口: $new_port"
    
    if [ -f "$FRONTEND_CONFIG_FILE" ]; then
        # 备份原文件
        cp "$FRONTEND_CONFIG_FILE" "${FRONTEND_CONFIG_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
        
        # 更新端口配置
        sed -i.tmp "s/localhost:710[0-9]/localhost:${new_port}/g" "$FRONTEND_CONFIG_FILE"
        rm -f "${FRONTEND_CONFIG_FILE}.tmp"
        
        log_success "前端配置已更新"
    else
        log_warning "前端配置文件不存在: $FRONTEND_CONFIG_FILE"
    fi
}

# 部署Nginx配置
deploy_nginx_config() {
    if ! check_nginx; then
        return 1
    fi
    
    log_nginx "部署Nginx配置..."
    
    # 复制配置文件
    sudo cp "/tmp/${NGINX_CONFIG_FILE}.conf" "${NGINX_CONFIG_DIR}/${NGINX_CONFIG_FILE}"
    
    # 创建软链接
    sudo ln -sf "${NGINX_CONFIG_DIR}/${NGINX_CONFIG_FILE}" "/etc/nginx/sites-enabled/${NGINX_CONFIG_FILE}"
    
    # 测试配置
    if sudo nginx -t; then
        # 重新加载Nginx
        sudo systemctl reload nginx
        log_success "Nginx配置已部署并重新加载"
    else
        log_error "Nginx配置测试失败"
        return 1
    fi
}

# 启动主服务
start_primary() {
    local profile=${1:-dev}
    
    log_migration "启动主后端服务..."
    
    check_java
    check_maven
    build_backend
    
    # 检查默认端口
    if check_port $DEFAULT_PORT; then
        log_warning "默认端口 $DEFAULT_PORT 已被占用"
        stop_backend_instance $DEFAULT_PORT "primary"
    fi
    
    # 启动主服务
    if start_backend_instance $DEFAULT_PORT $profile "primary"; then
        log_success "主后端服务启动成功"
        return 0
    else
        log_error "主后端服务启动失败"
        return 1
    fi
}

# 启动备份服务
start_backup() {
    local profile=${1:-dev}
    
    log_migration "启动备份后端服务..."
    
    # 获取可用端口
    local backup_port=$(get_available_port $BACKUP_PORT)
    
    # 启动备份服务
    if start_backend_instance $backup_port $profile "backup"; then
        log_success "备份后端服务启动成功，端口: $backup_port"
        echo $backup_port > "backup.port"
        return 0
    else
        log_error "备份后端服务启动失败"
        return 1
    fi
}

# 执行迁移
migrate_backend() {
    local profile=${1:-dev}
    
    log_migration "开始后端服务迁移..."
    
    # 获取当前备份端口
    if [ -f "backup.port" ]; then
        local backup_port=$(cat "backup.port")
    else
        log_error "未找到备份服务，请先启动备份服务"
        return 1
    fi
    
    # 获取迁移端口
    local migration_port=$(get_available_port $MIGRATION_PORT)
    
    log_migration "启动迁移服务，端口: $migration_port"
    
    # 启动迁移服务
    if start_backend_instance $migration_port $profile "migration"; then
        log_migration "迁移服务启动成功"
        
        # 更新Nginx配置，将流量切换到迁移服务
        generate_nginx_config $migration_port $backup_port
        deploy_nginx_config
        
        # 更新前端配置
        update_frontend_config $migration_port
        
        log_success "迁移完成！流量已切换到端口: $migration_port"
        log_info "原主服务端口: $DEFAULT_PORT"
        log_info "新主服务端口: $migration_port"
        log_info "备份服务端口: $backup_port"
        
        # 保存迁移信息
        echo $migration_port > "migration.port"
        echo $DEFAULT_PORT > "old.port"
        
        return 0
    else
        log_error "迁移服务启动失败"
        return 1
    fi
}

# 完成迁移
complete_migration() {
    log_migration "完成迁移，清理旧服务..."
    
    # 获取端口信息
    if [ -f "old.port" ]; then
        local old_port=$(cat "old.port")
        local migration_port=$(cat "migration.port")
        local backup_port=$(cat "backup.port")
        
        # 停止旧的主服务
        stop_backend_instance $old_port "old-primary"
        
        # 更新Nginx配置，将迁移服务设为主服务
        generate_nginx_config $migration_port $backup_port
        deploy_nginx_config
        
        # 更新端口文件
        echo $migration_port > "primary.port"
        rm -f "migration.port" "old.port"
        
        log_success "迁移完成！"
        log_info "新主服务端口: $migration_port"
        log_info "备份服务端口: $backup_port"
        
    else
        log_error "未找到迁移信息，无法完成迁移"
        return 1
    fi
}

# 回滚迁移
rollback_migration() {
    log_migration "回滚迁移..."
    
    if [ -f "old.port" ]; then
        local old_port=$(cat "old.port")
        local migration_port=$(cat "migration.port")
        local backup_port=$(cat "backup.port")
        
        # 停止迁移服务
        stop_backend_instance $migration_port "migration"
        
        # 恢复原配置
        generate_nginx_config $old_port $backup_port
        deploy_nginx_config
        
        update_frontend_config $old_port
        
        # 清理迁移文件
        rm -f "migration.port" "old.port"
        
        log_success "迁移已回滚"
        log_info "主服务端口: $old_port"
        log_info "备份服务端口: $backup_port"
        
    else
        log_error "未找到迁移信息，无法回滚"
        return 1
    fi
}

# 查看服务状态
show_status() {
    log_migration "查看后端服务状态..."
    echo "=================================="
    
    # 检查主服务
    if [ -f "primary.port" ]; then
        local primary_port=$(cat "primary.port")
        if [ -f "backend-${primary_port}.pid" ]; then
            local pid=$(cat "backend-${primary_port}.pid")
            if ps -p $pid > /dev/null; then
                log_success "主服务运行中 (端口: $primary_port, PID: $pid)"
            else
                log_warning "主服务未运行 (端口: $primary_port)"
            fi
        fi
    elif check_port $DEFAULT_PORT; then
        log_success "主服务运行中 (端口: $DEFAULT_PORT)"
    else
        log_warning "主服务未运行"
    fi
    
    # 检查备份服务
    if [ -f "backup.port" ]; then
        local backup_port=$(cat "backup.port")
        if [ -f "backend-${backup_port}.pid" ]; then
            local pid=$(cat "backend-${backup_port}.pid")
            if ps -p $pid > /dev/null; then
                log_success "备份服务运行中 (端口: $backup_port, PID: $pid)"
            else
                log_warning "备份服务未运行 (端口: $backup_port)"
            fi
        fi
    else
        log_info "备份服务未配置"
    fi
    
    # 检查迁移服务
    if [ -f "migration.port" ]; then
        local migration_port=$(cat "migration.port")
        if [ -f "backend-${migration_port}.pid" ]; then
            local pid=$(cat "backend-${migration_port}.pid")
            if ps -p $pid > /dev/null; then
                log_success "迁移服务运行中 (端口: $migration_port, PID: $pid)"
            else
                log_warning "迁移服务未运行 (端口: $migration_port)"
            fi
        fi
    else
        log_info "迁移服务未运行"
    fi
    
    # 检查Nginx状态
    if check_nginx; then
        if systemctl is-active --quiet nginx; then
            log_success "Nginx服务运行中"
        else
            log_warning "Nginx服务未运行"
        fi
    fi
    
    echo "=================================="
}

# 停止所有服务
stop_all() {
    log_migration "停止所有后端服务..."
    
    # 停止所有后端实例
    for pid_file in backend-*.pid; do
        if [ -f "$pid_file" ]; then
            local port=$(echo $pid_file | sed 's/backend-\(.*\)\.pid/\1/')
            local pid=$(cat $pid_file)
            if ps -p $pid > /dev/null; then
                kill $pid
                log_success "已停止服务 (端口: $port, PID: $pid)"
            fi
            rm -f $pid_file
        fi
    done
    
    # 清理端口文件
    rm -f primary.port backup.port migration.port old.port
    
    log_success "所有后端服务已停止"
}

# 显示帮助信息
show_help() {
    echo "易邦ERP后端迁移管理脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令] [参数]"
    echo ""
    echo "命令选项:"
    echo "  start-primary [环境]    启动主后端服务"
    echo "  start-backup [环境]     启动备份后端服务"
    echo "  migrate [环境]          执行服务迁移"
    echo "  complete                完成迁移"
    echo "  rollback                回滚迁移"
    echo "  stop-all                停止所有服务"
    echo "  status                  查看服务状态"
    echo "  help                    显示帮助信息"
    echo ""
    echo "环境选项:"
    echo "  dev     开发环境 (默认)"
    echo "  test    测试环境"
    echo "  prod    生产环境"
    echo ""
    echo "使用示例:"
    echo "  $0 start-primary dev     # 启动主服务"
    echo "  $0 start-backup dev      # 启动备份服务"
    echo "  $0 migrate dev           # 执行迁移"
    echo "  $0 complete              # 完成迁移"
    echo "  $0 rollback              # 回滚迁移"
    echo "  $0 status                # 查看状态"
    echo "  $0 stop-all              # 停止所有服务"
    echo ""
    echo "迁移流程:"
    echo "  1. start-primary         # 启动主服务"
    echo "  2. start-backup          # 启动备份服务"
    echo "  3. migrate               # 执行迁移"
    echo "  4. complete              # 完成迁移"
    echo ""
    echo "注意事项:"
    echo "  - 需要安装Nginx用于负载均衡"
    echo "  - 需要root权限配置Nginx"
    echo "  - 迁移过程中服务不会中断"
}

# 主函数
main() {
    local command=$1
    local profile=${2:-dev}
    
    case $command in
        "start-primary")
            start_primary $profile
            ;;
        "start-backup")
            start_backup $profile
            ;;
        "migrate")
            migrate_backend $profile
            ;;
        "complete")
            complete_migration
            ;;
        "rollback")
            rollback_migration
            ;;
        "stop-all")
            stop_all
            ;;
        "status")
            show_status
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
