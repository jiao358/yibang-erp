#!/bin/bash

# 易邦ERP前端启动脚本
# 使用方法: ./start-frontend.sh [dev|build|preview]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 检查Node.js环境
check_node() {
    log_info "检查Node.js环境..."
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装，请先安装Node.js 18或更高版本"
        exit 1
    fi
    
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -lt 18 ]; then
        log_error "Node.js版本过低，需要Node.js 18或更高版本"
        exit 1
    fi
    
    log_success "Node.js环境检查通过: $(node -v)"
}

# 检查npm环境
check_npm() {
    log_info "检查npm环境..."
    if ! command -v npm &> /dev/null; then
        log_error "npm未安装"
        exit 1
    fi
    
    log_success "npm环境检查通过: $(npm -v)"
}

# 安装依赖
install_dependencies() {
    log_info "安装前端依赖..."
    cd frontend
    
    if [ ! -f "package.json" ]; then
        log_error "未找到package.json文件"
        exit 1
    fi
    
    # 检查node_modules是否存在
    if [ ! -d "node_modules" ]; then
        log_info "首次安装依赖..."
        npm install
    else
        log_info "检查依赖更新..."
        npm install
    fi
    
    if [ $? -eq 0 ]; then
        log_success "依赖安装完成"
    else
        log_error "依赖安装失败"
        exit 1
    fi
    
    cd ..
}

# 启动开发服务器
start_dev_server() {
    log_info "启动前端开发服务器..."
    cd frontend
    
    # 检查是否已有开发服务器在运行
    if [ -f "frontend.pid" ]; then
        PID=$(cat frontend.pid)
        if ps -p $PID > /dev/null; then
            log_warning "前端开发服务器已在运行 (PID: $PID)"
            log_info "如需重启，请先运行: $0 stop"
            return 0
        else
            rm -f frontend.pid
        fi
    fi
    
    # 启动开发服务器
    nohup npm run dev > ../frontend.log 2>&1 &
    
    # 获取进程ID
    FRONTEND_PID=$!
    echo $FRONTEND_PID > frontend.pid
    
    log_success "前端开发服务器已启动，PID: $FRONTEND_PID"
    log_info "日志文件: frontend.log"
    log_info "PID文件: frontend.pid"
    
    # 等待服务器启动
    log_info "等待开发服务器启动..."
    sleep 3
    
    # 检查服务器是否启动成功
    if ps -p $FRONTEND_PID > /dev/null; then
        log_success "前端开发服务器启动成功！"
        log_info "访问地址: http://localhost:7101"
        log_info "后端API代理: http://localhost:7102"
    else
        log_error "前端开发服务器启动失败，请检查日志文件: frontend.log"
        exit 1
    fi
    
    cd ..
}

# 构建生产版本
build_production() {
    log_info "构建前端生产版本..."
    cd frontend
    
    # 清理之前的构建
    if [ -d "dist" ]; then
        log_info "清理之前的构建文件..."
        rm -rf dist
    fi
    
    # 构建生产版本
    npm run build
    
    if [ $? -eq 0 ]; then
        log_success "前端生产版本构建成功"
        log_info "构建文件位置: frontend/dist/"
        
        # 显示构建文件大小
        if command -v du &> /dev/null; then
            BUILD_SIZE=$(du -sh dist | cut -f1)
            log_info "构建文件大小: $BUILD_SIZE"
        fi
    else
        log_error "前端生产版本构建失败"
        exit 1
    fi
    
    cd ..
}

# 启动预览服务器
start_preview_server() {
    log_info "启动前端预览服务器..."
    cd frontend
    
    # 检查构建文件是否存在
    if [ ! -d "dist" ]; then
        log_error "未找到构建文件，请先运行: $0 build"
        exit 1
    fi
    
    # 检查是否已有预览服务器在运行
    if [ -f "preview.pid" ]; then
        PID=$(cat preview.pid)
        if ps -p $PID > /dev/null; then
            log_warning "前端预览服务器已在运行 (PID: $PID)"
            log_info "如需重启，请先运行: $0 stop-preview"
            return 0
        else
            rm -f preview.pid
        fi
    fi
    
    # 启动预览服务器
    nohup npm run preview > ../preview.log 2>&1 &
    
    # 获取进程ID
    PREVIEW_PID=$!
    echo $PREVIEW_PID > preview.pid
    
    log_success "前端预览服务器已启动，PID: $PREVIEW_PID"
    log_info "日志文件: preview.log"
    log_info "PID文件: preview.pid"
    
    # 等待服务器启动
    log_info "等待预览服务器启动..."
    sleep 3
    
    # 检查服务器是否启动成功
    if ps -p $PREVIEW_PID > /dev/null; then
        log_success "前端预览服务器启动成功！"
        log_info "访问地址: http://localhost:4173"
    else
        log_error "前端预览服务器启动失败，请检查日志文件: preview.log"
        exit 1
    fi
    
    cd ..
}

# 停止开发服务器
stop_dev_server() {
    log_info "停止前端开发服务器..."
    
    if [ -f "frontend.pid" ]; then
        PID=$(cat frontend.pid)
        if ps -p $PID > /dev/null; then
            kill $PID
            log_success "前端开发服务器已停止 (PID: $PID)"
            rm -f frontend.pid
        else
            log_warning "前端开发服务器未运行"
            rm -f frontend.pid
        fi
    else
        log_warning "未找到PID文件，尝试查找Node.js进程..."
        PIDS=$(pgrep -f "vite.*dev")
        if [ -n "$PIDS" ]; then
            echo $PIDS | xargs kill
            log_success "前端开发服务器已停止"
        else
            log_warning "未找到运行中的前端开发服务器"
        fi
    fi
}

# 停止预览服务器
stop_preview_server() {
    log_info "停止前端预览服务器..."
    
    if [ -f "preview.pid" ]; then
        PID=$(cat preview.pid)
        if ps -p $PID > /dev/null; then
            kill $PID
            log_success "前端预览服务器已停止 (PID: $PID)"
            rm -f preview.pid
        else
            log_warning "前端预览服务器未运行"
            rm -f preview.pid
        fi
    else
        log_warning "未找到PID文件，尝试查找Node.js进程..."
        PIDS=$(pgrep -f "vite.*preview")
        if [ -n "$PIDS" ]; then
            echo $PIDS | xargs kill
            log_success "前端预览服务器已停止"
        else
            log_warning "未找到运行中的前端预览服务器"
        fi
    fi
}

# 查看应用状态
show_status() {
    log_info "查看前端应用状态..."
    
    # 检查开发服务器
    if [ -f "frontend.pid" ]; then
        PID=$(cat frontend.pid)
        if ps -p $PID > /dev/null; then
            log_success "前端开发服务器正在运行 (PID: $PID)"
            log_info "访问地址: http://localhost:7101"
        else
            log_warning "前端开发服务器未运行 (PID文件存在但进程不存在)"
            rm -f frontend.pid
        fi
    else
        log_info "前端开发服务器未运行"
    fi
    
    # 检查预览服务器
    if [ -f "preview.pid" ]; then
        PID=$(cat preview.pid)
        if ps -p $PID > /dev/null; then
            log_success "前端预览服务器正在运行 (PID: $PID)"
            log_info "访问地址: http://localhost:4173"
        else
            log_warning "前端预览服务器未运行 (PID文件存在但进程不存在)"
            rm -f preview.pid
        fi
    else
        log_info "前端预览服务器未运行"
    fi
    
    # 检查构建文件
    if [ -d "frontend/dist" ]; then
        log_success "生产构建文件存在: frontend/dist/"
        if command -v du &> /dev/null; then
            BUILD_SIZE=$(du -sh frontend/dist | cut -f1)
            log_info "构建文件大小: $BUILD_SIZE"
        fi
    else
        log_info "生产构建文件不存在"
    fi
}

# 查看应用日志
show_logs() {
    local log_type=${1:-dev}
    
    case $log_type in
        "dev")
            log_info "查看开发服务器日志..."
            if [ -f "frontend.log" ]; then
                tail -f frontend.log
            else
                log_warning "日志文件不存在: frontend.log"
            fi
            ;;
        "preview")
            log_info "查看预览服务器日志..."
            if [ -f "preview.log" ]; then
                tail -f preview.log
            else
                log_warning "日志文件不存在: preview.log"
            fi
            ;;
        *)
            log_error "未知日志类型: $log_type"
            log_info "可用选项: dev, preview"
            ;;
    esac
}

# 清理构建文件
clean_build() {
    log_info "清理构建文件..."
    cd frontend
    
    if [ -d "dist" ]; then
        rm -rf dist
        log_success "构建文件已清理"
    else
        log_info "构建文件不存在，无需清理"
    fi
    
    if [ -d "node_modules" ]; then
        log_info "是否清理node_modules? (y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            rm -rf node_modules
            log_success "node_modules已清理"
        fi
    fi
    
    cd ..
}

# 显示帮助信息
show_help() {
    echo "易邦ERP前端启动脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令]"
    echo ""
    echo "命令选项:"
    echo "  dev         启动开发服务器 (默认)"
    echo "  build       构建生产版本"
    echo "  preview     启动预览服务器"
    echo "  stop        停止开发服务器"
    echo "  stop-preview 停止预览服务器"
    echo "  status      查看应用状态"
    echo "  logs [类型] 查看应用日志 (dev/preview)"
    echo "  clean       清理构建文件"
    echo "  help        显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0           # 启动开发服务器"
    echo "  $0 dev       # 启动开发服务器"
    echo "  $0 build     # 构建生产版本"
    echo "  $0 preview   # 启动预览服务器"
    echo "  $0 stop      # 停止开发服务器"
    echo "  $0 logs dev  # 查看开发服务器日志"
    echo "  $0 clean     # 清理构建文件"
}

# 主函数
main() {
    local command=${1:-dev}
    
    case $command in
        "stop")
            stop_dev_server
            ;;
        "stop-preview")
            stop_preview_server
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs $2
            ;;
        "clean")
            clean_build
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        "dev")
            log_info "启动易邦ERP前端开发服务器"
            echo "=================================="
            
            check_node
            check_npm
            install_dependencies
            start_dev_server
            
            echo "=================================="
            log_success "前端开发服务器启动完成！"
            ;;
        "build")
            log_info "构建易邦ERP前端生产版本"
            echo "=================================="
            
            check_node
            check_npm
            install_dependencies
            build_production
            
            echo "=================================="
            log_success "前端生产版本构建完成！"
            ;;
        "preview")
            log_info "启动易邦ERP前端预览服务器"
            echo "=================================="
            
            check_node
            check_npm
            install_dependencies
            start_preview_server
            
            echo "=================================="
            log_success "前端预览服务器启动完成！"
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
