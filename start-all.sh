#!/bin/bash

# 易邦ERP完整系统启动脚本
# 使用方法: ./start-all.sh [dev|test|prod] [start|stop|restart|status]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

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

log_system() {
    echo -e "${PURPLE}[SYSTEM]${NC} $1"
}

# 检查脚本文件
check_scripts() {
    log_info "检查启动脚本..."
    
    if [ ! -f "start-backend.sh" ]; then
        log_error "未找到后端启动脚本: start-backend.sh"
        exit 1
    fi
    
    if [ ! -f "start-frontend.sh" ]; then
        log_error "未找到前端启动脚本: start-frontend.sh"
        exit 1
    fi
    
    # 设置脚本执行权限
    chmod +x start-backend.sh
    chmod +x start-frontend.sh
    
    log_success "启动脚本检查通过"
}

# 检查系统服务
check_system_services() {
    log_info "检查系统服务..."
    
    # 检查MySQL
    if command -v systemctl &> /dev/null; then
        if systemctl is-active --quiet mysql; then
            log_success "MySQL服务正在运行"
        else
            log_warning "MySQL服务未运行，请启动MySQL服务"
            log_info "启动命令: sudo systemctl start mysql"
        fi
        
        # 检查Redis
        if systemctl is-active --quiet redis-server; then
            log_success "Redis服务正在运行"
        else
            log_warning "Redis服务未运行，请启动Redis服务"
            log_info "启动命令: sudo systemctl start redis-server"
        fi
    else
        log_warning "无法检查系统服务状态（systemctl不可用）"
        log_info "请确保MySQL和Redis服务已启动"
    fi
}

# 启动后端
start_backend() {
    local profile=$1
    log_system "启动后端服务..."
    ./start-backend.sh $profile
}

# 启动前端
start_frontend() {
    log_system "启动前端服务..."
    ./start-frontend.sh dev
}

# 停止后端
stop_backend() {
    log_system "停止后端服务..."
    ./start-backend.sh stop
}

# 停止前端
stop_frontend() {
    log_system "停止前端服务..."
    ./start-frontend.sh stop
}

# 重启后端
restart_backend() {
    local profile=$1
    log_system "重启后端服务..."
    ./start-backend.sh stop
    sleep 2
    ./start-backend.sh $profile
}

# 重启前端
restart_frontend() {
    log_system "重启前端服务..."
    ./start-frontend.sh stop
    sleep 2
    ./start-frontend.sh dev
}

# 查看系统状态
show_status() {
    log_system "查看系统状态..."
    echo "=================================="
    
    echo -e "\n${PURPLE}后端服务状态:${NC}"
    ./start-backend.sh status
    
    echo -e "\n${PURPLE}前端服务状态:${NC}"
    ./start-frontend.sh status
    
    echo -e "\n${PURPLE}系统服务状态:${NC}"
    check_system_services
    
    echo "=================================="
}

# 启动完整系统
start_system() {
    local profile=$1
    log_system "启动易邦ERP完整系统 (环境: $profile)"
    echo "=================================="
    
    check_scripts
    check_system_services
    
    # 启动后端
    start_backend $profile
    
    # 等待后端启动
    log_info "等待后端服务启动..."
    sleep 5
    
    # 启动前端
    start_frontend
    
    echo "=================================="
    log_success "易邦ERP系统启动完成！"
    echo ""
    log_info "访问地址:"
    log_info "  前端: http://localhost:7101"
    log_info "  后端: http://localhost:7102"
    log_info "  健康检查: http://localhost:7102/actuator/health"
    echo ""
    log_info "管理命令:"
    log_info "  查看状态: $0 $profile status"
    log_info "  停止系统: $0 $profile stop"
    log_info "  重启系统: $0 $profile restart"
}

# 停止完整系统
stop_system() {
    log_system "停止易邦ERP完整系统"
    echo "=================================="
    
    stop_frontend
    stop_backend
    
    echo "=================================="
    log_success "易邦ERP系统已停止！"
}

# 重启完整系统
restart_system() {
    local profile=$1
    log_system "重启易邦ERP完整系统 (环境: $profile)"
    echo "=================================="
    
    stop_system
    sleep 3
    start_system $profile
}

# 构建生产版本
build_production() {
    log_system "构建易邦ERP生产版本"
    echo "=================================="
    
    check_scripts
    
    # 构建后端
    log_info "构建后端应用..."
    ./start-backend.sh prod
    
    # 构建前端
    log_info "构建前端应用..."
    ./start-frontend.sh build
    
    echo "=================================="
    log_success "生产版本构建完成！"
    log_info "后端JAR文件: backend/target/yibang-erp-*.jar"
    log_info "前端构建文件: frontend/dist/"
}

# 查看日志
show_logs() {
    local service=${1:-all}
    
    case $service in
        "backend"|"be")
            log_info "查看后端日志..."
            ./start-backend.sh logs
            ;;
        "frontend"|"fe")
            log_info "查看前端日志..."
            ./start-frontend.sh logs dev
            ;;
        "all")
            log_info "查看所有服务日志..."
            echo -e "\n${PURPLE}后端日志:${NC}"
            if [ -f "backend.log" ]; then
                tail -20 backend.log
            else
                log_warning "后端日志文件不存在"
            fi
            
            echo -e "\n${PURPLE}前端日志:${NC}"
            if [ -f "frontend.log" ]; then
                tail -20 frontend.log
            else
                log_warning "前端日志文件不存在"
            fi
            ;;
        *)
            log_error "未知服务: $service"
            log_info "可用选项: backend, frontend, all"
            ;;
    esac
}

# 清理系统
clean_system() {
    log_system "清理易邦ERP系统"
    echo "=================================="
    
    # 停止所有服务
    stop_system
    
    # 清理构建文件
    log_info "清理构建文件..."
    ./start-frontend.sh clean
    
    # 清理日志文件
    log_info "清理日志文件..."
    rm -f backend.log frontend.log preview.log
    rm -f backend.pid frontend.pid preview.pid
    
    # 清理后端构建文件
    if [ -d "backend/target" ]; then
        log_info "清理后端构建文件..."
        rm -rf backend/target
    fi
    
    echo "=================================="
    log_success "系统清理完成！"
}

# 显示帮助信息
show_help() {
    echo "易邦ERP完整系统启动脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [环境] [命令]"
    echo ""
    echo "环境选项:"
    echo "  dev     开发环境 (默认)"
    echo "  test    测试环境"
    echo "  prod    生产环境"
    echo ""
    echo "命令选项:"
    echo "  start     启动完整系统 (默认)"
    echo "  stop      停止完整系统"
    echo "  restart   重启完整系统"
    echo "  status    查看系统状态"
    echo "  build     构建生产版本"
    echo "  logs [服务] 查看日志 (backend/frontend/all)"
    echo "  clean     清理系统"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                    # 启动开发环境"
    echo "  $0 dev start          # 启动开发环境"
    echo "  $0 prod start         # 启动生产环境"
    echo "  $0 dev stop           # 停止开发环境"
    echo "  $0 dev restart        # 重启开发环境"
    echo "  $0 dev status         # 查看系统状态"
    echo "  $0 build              # 构建生产版本"
    echo "  $0 logs backend       # 查看后端日志"
    echo "  $0 clean              # 清理系统"
    echo ""
    echo "单独服务管理:"
    echo "  ./start-backend.sh [环境] [命令]   # 后端服务管理"
    echo "  ./start-frontend.sh [命令]        # 前端服务管理"
}

# 主函数
main() {
    local profile=${1:-dev}
    local command=${2:-start}
    
    case $command in
        "stop")
            stop_system
            ;;
        "restart")
            restart_system $profile
            ;;
        "status")
            show_status
            ;;
        "build")
            build_production
            ;;
        "logs")
            show_logs $3
            ;;
        "clean")
            clean_system
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        "start")
            start_system $profile
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
