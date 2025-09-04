#!/bin/bash

# 易邦ERP后端切换脚本
# 支持动态切换前端API端点，无需重启前端服务
# 使用方法: ./switch-backend.sh [端口] [操作]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 配置参数
FRONTEND_CONFIG_FILE="frontend/src/utils/request.ts"
FRONTEND_DIST_CONFIG="frontend/dist/assets/*.js"
BACKUP_CONFIG_DIR="config-backup"
DEFAULT_BACKEND_PORT=7102

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

log_switch() {
    echo -e "${PURPLE}[SWITCH]${NC} $1"
}

# 检查后端服务健康状态
check_backend_health() {
    local port=$1
    local max_attempts=5
    local attempt=1
    
    log_info "检查后端服务健康状态 (端口: $port)..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log_success "后端服务健康检查通过 (端口: $port)"
            return 0
        else
            log_warning "健康检查失败，尝试 $attempt/$max_attempts"
            sleep 2
            attempt=$((attempt + 1))
        fi
    done
    
    log_error "后端服务健康检查失败 (端口: $port)"
    return 1
}

# 备份当前配置
backup_config() {
    local backup_name=$1
    
    log_info "备份当前配置..."
    
    # 创建备份目录
    mkdir -p "$BACKUP_CONFIG_DIR"
    
    # 备份前端配置文件
    if [ -f "$FRONTEND_CONFIG_FILE" ]; then
        cp "$FRONTEND_CONFIG_FILE" "$BACKUP_CONFIG_DIR/request.ts.backup.${backup_name}"
        log_success "前端配置文件已备份"
    fi
    
    # 备份构建后的配置文件
    if [ -d "frontend/dist" ]; then
        find frontend/dist -name "*.js" -exec cp {} "$BACKUP_CONFIG_DIR/" \;
        log_success "构建文件已备份"
    fi
}

# 更新前端开发配置
update_frontend_dev_config() {
    local new_port=$1
    
    log_switch "更新前端开发配置到端口: $new_port"
    
    if [ -f "$FRONTEND_CONFIG_FILE" ]; then
        # 备份原文件
        backup_config "dev_$(date +%Y%m%d_%H%M%S)"
        
        # 更新端口配置
        sed -i.tmp "s/localhost:710[0-9]/localhost:${new_port}/g" "$FRONTEND_CONFIG_FILE"
        rm -f "${FRONTEND_CONFIG_FILE}.tmp"
        
        log_success "前端开发配置已更新"
    else
        log_error "前端配置文件不存在: $FRONTEND_CONFIG_FILE"
        return 1
    fi
}

# 更新前端生产配置
update_frontend_prod_config() {
    local new_port=$1
    
    log_switch "更新前端生产配置到端口: $new_port"
    
    if [ -d "frontend/dist" ]; then
        # 备份构建文件
        backup_config "prod_$(date +%Y%m%d_%H%M%S)"
        
        # 更新构建后的JS文件中的API端点
        find frontend/dist -name "*.js" -type f -exec sed -i.tmp "s/localhost:710[0-9]/localhost:${new_port}/g" {} \;
        find frontend/dist -name "*.js.tmp" -delete
        
        log_success "前端生产配置已更新"
    else
        log_error "前端构建文件不存在: frontend/dist"
        return 1
    fi
}

# 热重载前端配置
hot_reload_frontend() {
    log_switch "热重载前端配置..."
    
    # 检查前端开发服务器是否运行
    if [ -f "frontend.pid" ]; then
        local pid=$(cat frontend.pid)
        if ps -p $pid > /dev/null; then
            # 发送HUP信号触发热重载
            kill -HUP $pid
            log_success "前端开发服务器已热重载"
        else
            log_warning "前端开发服务器未运行"
        fi
    else
        log_warning "未找到前端开发服务器PID文件"
    fi
}

# 切换开发环境后端
switch_dev_backend() {
    local new_port=$1
    
    log_switch "切换开发环境后端到端口: $new_port"
    
    # 检查新后端健康状态
    if ! check_backend_health $new_port; then
        log_error "目标后端服务不健康，切换失败"
        return 1
    fi
    
    # 更新前端配置
    if update_frontend_dev_config $new_port; then
        # 热重载前端
        hot_reload_frontend
        
        log_success "开发环境后端切换完成！"
        log_info "新后端地址: http://localhost:$new_port"
        log_info "前端地址: http://localhost:7101"
        
        # 保存当前配置
        echo $new_port > "current.backend.port"
        
        return 0
    else
        log_error "开发环境后端切换失败"
        return 1
    fi
}

# 切换生产环境后端
switch_prod_backend() {
    local new_port=$1
    
    log_switch "切换生产环境后端到端口: $new_port"
    
    # 检查新后端健康状态
    if ! check_backend_health $new_port; then
        log_error "目标后端服务不健康，切换失败"
        return 1
    fi
    
    # 更新前端配置
    if update_frontend_prod_config $new_port; then
        log_success "生产环境后端切换完成！"
        log_info "新后端地址: http://localhost:$new_port"
        log_info "前端构建文件已更新"
        
        # 保存当前配置
        echo $new_port > "current.backend.port"
        
        return 0
    else
        log_error "生产环境后端切换失败"
        return 1
    fi
}

# 自动切换后端
auto_switch_backend() {
    local target_port=$1
    local environment=${2:-dev}
    
    log_switch "自动切换后端到端口: $target_port (环境: $environment)"
    
    case $environment in
        "dev")
            switch_dev_backend $target_port
            ;;
        "prod")
            switch_prod_backend $target_port
            ;;
        *)
            log_error "未知环境: $environment"
            return 1
            ;;
    esac
}

# 回滚配置
rollback_config() {
    local backup_name=$1
    
    log_switch "回滚配置到: $backup_name"
    
    if [ -f "$BACKUP_CONFIG_DIR/request.ts.backup.${backup_name}" ]; then
        cp "$BACKUP_CONFIG_DIR/request.ts.backup.${backup_name}" "$FRONTEND_CONFIG_FILE"
        log_success "前端配置已回滚"
        
        # 热重载前端
        hot_reload_frontend
        
        return 0
    else
        log_error "未找到备份文件: $backup_name"
        return 1
    fi
}

# 列出可用备份
list_backups() {
    log_info "可用的配置备份:"
    
    if [ -d "$BACKUP_CONFIG_DIR" ]; then
        ls -la "$BACKUP_CONFIG_DIR"/*.backup.* 2>/dev/null | while read line; do
            echo "  $line"
        done
    else
        log_warning "未找到备份目录"
    fi
}

# 查看当前配置
show_current_config() {
    log_info "当前后端配置:"
    echo "=================================="
    
    # 查看当前端口
    if [ -f "current.backend.port" ]; then
        local current_port=$(cat "current.backend.port")
        log_info "当前后端端口: $current_port"
    else
        log_info "当前后端端口: 未设置"
    fi
    
    # 查看前端配置中的端口
    if [ -f "$FRONTEND_CONFIG_FILE" ]; then
        local config_port=$(grep -o "localhost:710[0-9]" "$FRONTEND_CONFIG_FILE" | head -1 | cut -d: -f2)
        if [ -n "$config_port" ]; then
            log_info "前端配置端口: $config_port"
        else
            log_warning "无法从前端配置中获取端口"
        fi
    fi
    
    # 检查后端健康状态
    if [ -n "$current_port" ]; then
        if check_backend_health $current_port; then
            log_success "当前后端服务健康"
        else
            log_error "当前后端服务不健康"
        fi
    fi
    
    echo "=================================="
}

# 测试后端连接
test_backend_connection() {
    local port=$1
    
    log_info "测试后端连接 (端口: $port)..."
    
    # 测试基本连接
    if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        log_success "后端连接测试通过"
        
        # 获取健康状态详情
        local health_info=$(curl -s "http://localhost:$port/actuator/health")
        log_info "健康状态: $health_info"
        
        return 0
    else
        log_error "后端连接测试失败"
        return 1
    fi
}

# 批量测试端口
test_multiple_ports() {
    local ports=("$@")
    
    log_info "批量测试后端端口..."
    echo "=================================="
    
    for port in "${ports[@]}"; do
        echo -n "端口 $port: "
        if test_backend_connection $port; then
            echo -e "${GREEN}✓ 健康${NC}"
        else
            echo -e "${RED}✗ 不健康${NC}"
        fi
    done
    
    echo "=================================="
}

# 显示帮助信息
show_help() {
    echo "易邦ERP后端切换脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [端口] [环境]           # 切换到指定端口"
    echo "  $0 [命令] [参数]           # 执行特定命令"
    echo ""
    echo "切换命令:"
    echo "  [端口] [环境]              切换到指定端口 (dev/prod)"
    echo ""
    echo "管理命令:"
    echo "  status                     查看当前配置"
    echo "  test [端口]                测试后端连接"
    echo "  test-all [端口1] [端口2]   批量测试端口"
    echo "  rollback [备份名]          回滚配置"
    echo "  list-backups               列出可用备份"
    echo "  help                       显示帮助信息"
    echo ""
    echo "使用示例:"
    echo "  $0 7103 dev                # 切换到端口7103 (开发环境)"
    echo "  $0 7104 prod               # 切换到端口7104 (生产环境)"
    echo "  $0 status                  # 查看当前配置"
    echo "  $0 test 7103               # 测试端口7103"
    echo "  $0 test-all 7102 7103 7104 # 批量测试端口"
    echo "  $0 rollback dev_20240101_120000 # 回滚配置"
    echo "  $0 list-backups            # 列出备份"
    echo ""
    echo "注意事项:"
    echo "  - 切换前会自动检查后端健康状态"
    echo "  - 开发环境支持热重载，无需重启前端"
    echo "  - 生产环境需要重新部署前端"
    echo "  - 所有操作都会自动备份配置"
}

# 主函数
main() {
    local arg1=$1
    local arg2=$2
    
    # 如果没有参数，显示帮助
    if [ -z "$arg1" ]; then
        show_help
        exit 0
    fi
    
    # 检查第一个参数是否为数字（端口号）
    if [[ "$arg1" =~ ^[0-9]+$ ]]; then
        # 第一个参数是端口号
        local port=$arg1
        local environment=${arg2:-dev}
        
        auto_switch_backend $port $environment
    else
        # 第一个参数是命令
        local command=$arg1
        local param=$arg2
        
        case $command in
            "status")
                show_current_config
                ;;
            "test")
                if [ -n "$param" ]; then
                    test_backend_connection $param
                else
                    log_error "请指定要测试的端口"
                    exit 1
                fi
                ;;
            "test-all")
                shift
                test_multiple_ports "$@"
                ;;
            "rollback")
                if [ -n "$param" ]; then
                    rollback_config $param
                else
                    log_error "请指定要回滚的备份名称"
                    exit 1
                fi
                ;;
            "list-backups")
                list_backups
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
    fi
}

# 执行主函数
main "$@"
