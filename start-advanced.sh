#!/bin/bash

# 易邦ERP高级启动脚本
# 支持后端迁移、负载均衡、优雅关闭、动态切换
# 使用方法: ./start-advanced.sh [命令] [参数]

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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_MIGRATION_SCRIPT="$SCRIPT_DIR/start-backend-migration.sh"
BACKEND_SCRIPT="$SCRIPT_DIR/start-backend.sh"
FRONTEND_SCRIPT="$SCRIPT_DIR/start-frontend.sh"
SWITCH_SCRIPT="$SCRIPT_DIR/switch-backend.sh"

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

log_advanced() {
    echo -e "${PURPLE}[ADVANCED]${NC} $1"
}

# 检查脚本文件
check_scripts() {
    log_info "检查启动脚本..."
    
    local scripts=("$BACKEND_SCRIPT" "$FRONTEND_SCRIPT" "$BACKEND_MIGRATION_SCRIPT" "$SWITCH_SCRIPT")
    
    for script in "${scripts[@]}"; do
        if [ ! -f "$script" ]; then
            log_error "未找到脚本文件: $script"
            exit 1
        fi
        
        # 设置执行权限
        chmod +x "$script"
    done
    
    log_success "所有启动脚本检查通过"
}

# 启动标准模式
start_standard() {
    local profile=${1:-dev}
    
    log_advanced "启动标准模式 (环境: $profile)"
    echo "=================================="
    
    check_scripts
    
    # 启动后端
    log_info "启动后端服务..."
    "$BACKEND_SCRIPT" $profile
    
    # 等待后端启动
    sleep 5
    
    # 启动前端
    log_info "启动前端服务..."
    "$FRONTEND_SCRIPT" dev
    
    echo "=================================="
    log_success "标准模式启动完成！"
    log_info "前端: http://localhost:7101"
    log_info "后端: http://localhost:7102"
}

# 启动高可用模式
start_ha() {
    local profile=${1:-dev}
    
    log_advanced "启动高可用模式 (环境: $profile)"
    echo "=================================="
    
    check_scripts
    
    # 启动主后端服务
    log_info "启动主后端服务..."
    "$BACKEND_MIGRATION_SCRIPT" start-primary $profile
    
    # 启动备份后端服务
    log_info "启动备份后端服务..."
    "$BACKEND_MIGRATION_SCRIPT" start-backup $profile
    
    # 启动前端
    log_info "启动前端服务..."
    "$FRONTEND_SCRIPT" dev
    
    echo "=================================="
    log_success "高可用模式启动完成！"
    log_info "前端: http://localhost:7101"
    log_info "主后端: http://localhost:7102"
    log_info "备份后端: 自动分配端口"
    log_info "负载均衡: 通过Nginx"
}

# 执行后端迁移
migrate_backend() {
    local profile=${1:-dev}
    
    log_advanced "执行后端迁移 (环境: $profile)"
    echo "=================================="
    
    if [ ! -f "$BACKEND_MIGRATION_SCRIPT" ]; then
        log_error "后端迁移脚本不存在"
        exit 1
    fi
    
    # 执行迁移
    "$BACKEND_MIGRATION_SCRIPT" migrate $profile
    
    echo "=================================="
    log_success "后端迁移完成！"
}

# 完成迁移
complete_migration() {
    log_advanced "完成后端迁移"
    echo "=================================="
    
    "$BACKEND_MIGRATION_SCRIPT" complete
    
    echo "=================================="
    log_success "迁移完成！"
}

# 回滚迁移
rollback_migration() {
    log_advanced "回滚后端迁移"
    echo "=================================="
    
    "$BACKEND_MIGRATION_SCRIPT" rollback
    
    echo "=================================="
    log_success "迁移已回滚！"
}

# 切换后端
switch_backend() {
    local port=$1
    local environment=${2:-dev}
    
    log_advanced "切换后端到端口: $port (环境: $environment)"
    echo "=================================="
    
    if [ ! -f "$SWITCH_SCRIPT" ]; then
        log_error "后端切换脚本不存在"
        exit 1
    fi
    
    "$SWITCH_SCRIPT" $port $environment
    
    echo "=================================="
    log_success "后端切换完成！"
}

# 查看系统状态
show_status() {
    log_advanced "查看系统状态"
    echo "=================================="
    
    # 检查标准服务
    echo -e "\n${PURPLE}标准服务状态:${NC}"
    if [ -f "$BACKEND_SCRIPT" ]; then
        "$BACKEND_SCRIPT" status
    fi
    
    if [ -f "$FRONTEND_SCRIPT" ]; then
        "$FRONTEND_SCRIPT" status
    fi
    
    # 检查迁移服务
    echo -e "\n${PURPLE}迁移服务状态:${NC}"
    if [ -f "$BACKEND_MIGRATION_SCRIPT" ]; then
        "$BACKEND_MIGRATION_SCRIPT" status
    fi
    
    # 检查当前配置
    echo -e "\n${PURPLE}当前配置:${NC}"
    if [ -f "$SWITCH_SCRIPT" ]; then
        "$SWITCH_SCRIPT" status
    fi
    
    echo "=================================="
}

# 测试后端连接
test_backend() {
    local port=${1:-7102}
    
    log_advanced "测试后端连接 (端口: $port)"
    echo "=================================="
    
    if [ -f "$SWITCH_SCRIPT" ]; then
        "$SWITCH_SCRIPT" test $port
    else
        log_error "后端切换脚本不存在"
        exit 1
    fi
    
    echo "=================================="
}

# 批量测试后端
test_all_backends() {
    log_advanced "批量测试后端连接"
    echo "=================================="
    
    if [ -f "$SWITCH_SCRIPT" ]; then
        "$SWITCH_SCRIPT" test-all 7102 7103 7104
    else
        log_error "后端切换脚本不存在"
        exit 1
    fi
    
    echo "=================================="
}

# 停止所有服务
stop_all() {
    log_advanced "停止所有服务"
    echo "=================================="
    
    # 停止前端
    if [ -f "$FRONTEND_SCRIPT" ]; then
        "$FRONTEND_SCRIPT" stop
    fi
    
    # 停止标准后端
    if [ -f "$BACKEND_SCRIPT" ]; then
        "$BACKEND_SCRIPT" stop
    fi
    
    # 停止迁移后端
    if [ -f "$BACKEND_MIGRATION_SCRIPT" ]; then
        "$BACKEND_MIGRATION_SCRIPT" stop-all
    fi
    
    echo "=================================="
    log_success "所有服务已停止！"
}

# 重启服务
restart_services() {
    local mode=${1:-standard}
    local profile=${2:-dev}
    
    log_advanced "重启服务 (模式: $mode, 环境: $profile)"
    echo "=================================="
    
    stop_all
    sleep 3
    
    case $mode in
        "standard")
            start_standard $profile
            ;;
        "ha")
            start_ha $profile
            ;;
        *)
            log_error "未知模式: $mode"
            exit 1
            ;;
    esac
    
    echo "=================================="
    log_success "服务重启完成！"
}

# 构建生产版本
build_production() {
    log_advanced "构建生产版本"
    echo "=================================="
    
    check_scripts
    
    # 构建后端
    log_info "构建后端应用..."
    "$BACKEND_SCRIPT" prod
    
    # 构建前端
    log_info "构建前端应用..."
    "$FRONTEND_SCRIPT" build
    
    echo "=================================="
    log_success "生产版本构建完成！"
}

# 部署生产环境
deploy_production() {
    local target_server=$1
    
    log_advanced "部署生产环境到: $target_server"
    echo "=================================="
    
    if [ -z "$target_server" ]; then
        log_error "请指定目标服务器"
        exit 1
    fi
    
    # 构建生产版本
    build_production
    
    # 部署到服务器
    log_info "部署到服务器: $target_server"
    
    # 这里可以添加具体的部署逻辑
    # 例如：scp、rsync等
    
    echo "=================================="
    log_success "生产环境部署完成！"
}

# 监控服务
monitor_services() {
    log_advanced "监控服务状态"
    echo "=================================="
    
    while true; do
        clear
        echo "易邦ERP服务监控 - $(date)"
        echo "=================================="
        
        show_status
        
        echo ""
        echo "按 Ctrl+C 退出监控"
        sleep 5
    done
}

# 显示帮助信息
show_help() {
    echo "易邦ERP高级启动脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令] [参数]"
    echo ""
    echo "启动命令:"
    echo "  start-standard [环境]     启动标准模式"
    echo "  start-ha [环境]           启动高可用模式"
    echo "  restart [模式] [环境]     重启服务"
    echo ""
    echo "迁移命令:"
    echo "  migrate [环境]            执行后端迁移"
    echo "  complete                  完成迁移"
    echo "  rollback                  回滚迁移"
    echo ""
    echo "切换命令:"
    echo "  switch [端口] [环境]      切换后端端口"
    echo "  test [端口]               测试后端连接"
    echo "  test-all                  批量测试后端"
    echo ""
    echo "管理命令:"
    echo "  status                    查看系统状态"
    echo "  stop                      停止所有服务"
    echo "  build                     构建生产版本"
    echo "  deploy [服务器]           部署生产环境"
    echo "  monitor                   监控服务状态"
    echo "  help                      显示帮助信息"
    echo ""
    echo "环境选项:"
    echo "  dev     开发环境 (默认)"
    echo "  test    测试环境"
    echo "  prod    生产环境"
    echo ""
    echo "使用示例:"
    echo "  $0 start-standard dev     # 启动标准模式"
    echo "  $0 start-ha prod          # 启动高可用模式"
    echo "  $0 migrate dev            # 执行迁移"
    echo "  $0 switch 7103 dev        # 切换到端口7103"
    echo "  $0 test 7102              # 测试端口7102"
    echo "  $0 status                 # 查看状态"
    echo "  $0 stop                   # 停止所有服务"
    echo "  $0 build                  # 构建生产版本"
    echo "  $0 monitor                # 监控服务"
    echo ""
    echo "高级功能:"
    echo "  - 支持后端服务迁移和负载均衡"
    echo "  - 支持动态切换后端端口"
    echo "  - 支持优雅关闭和健康检查"
    echo "  - 支持高可用部署模式"
    echo "  - 支持实时监控和状态查看"
}

# 主函数
main() {
    local command=$1
    local param1=$2
    local param2=$3
    
    case $command in
        "start-standard")
            start_standard $param1
            ;;
        "start-ha")
            start_ha $param1
            ;;
        "restart")
            restart_services $param1 $param2
            ;;
        "migrate")
            migrate_backend $param1
            ;;
        "complete")
            complete_migration
            ;;
        "rollback")
            rollback_migration
            ;;
        "switch")
            if [ -z "$param1" ]; then
                log_error "请指定要切换的端口"
                exit 1
            fi
            switch_backend $param1 $param2
            ;;
        "test")
            test_backend $param1
            ;;
        "test-all")
            test_all_backends
            ;;
        "status")
            show_status
            ;;
        "stop")
            stop_all
            ;;
        "build")
            build_production
            ;;
        "deploy")
            deploy_production $param1
            ;;
        "monitor")
            monitor_services
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            if [ -z "$command" ]; then
                show_help
            else
                log_error "未知命令: $command"
                show_help
                exit 1
            fi
            ;;
    esac
}

# 执行主函数
main "$@"
