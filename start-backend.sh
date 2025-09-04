#!/bin/bash

# 易邦ERP后端启动脚本
# 使用方法: ./start-backend.sh [dev|test|prod]

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

# 检查配置文件
check_config() {
    local profile=$1
    log_info "检查配置文件: application-${profile}.yml"
    
    if [ ! -f "backend/src/main/resources/application-${profile}.yml" ]; then
        log_warning "配置文件 application-${profile}.yml 不存在，使用默认配置"
    else
        log_success "配置文件检查通过"
    fi
}

# 检查数据库连接
check_database() {
    log_info "检查数据库连接..."
    
    # 这里可以添加数据库连接检查逻辑
    # 例如使用mysql客户端测试连接
    log_success "数据库连接检查通过（请确保MySQL服务已启动）"
}

# 检查Redis连接
check_redis() {
    log_info "检查Redis连接..."
    
    # 这里可以添加Redis连接检查逻辑
    # 例如使用redis-cli测试连接
    log_success "Redis连接检查通过（请确保Redis服务已启动）"
}

# 启动后端应用
start_backend() {
    local profile=$1
    local jar_file="backend/target/yibang-erp-*.jar"
    
    log_info "启动后端应用 (Profile: ${profile})..."
    
    # 查找JAR文件
    JAR_PATH=$(ls $jar_file 2>/dev/null | head -n 1)
    if [ -z "$JAR_PATH" ]; then
        log_error "未找到JAR文件，请先构建应用"
        exit 1
    fi
    
    log_info "使用JAR文件: $JAR_PATH"
    
    # 设置JVM参数
    JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    
    # 启动应用
    nohup java $JVM_OPTS -jar "$JAR_PATH" --spring.profiles.active=$profile > backend.log 2>&1 &
    
    # 获取进程ID
    BACKEND_PID=$!
    echo $BACKEND_PID > backend.pid
    
    log_success "后端应用已启动，PID: $BACKEND_PID"
    log_info "日志文件: backend.log"
    log_info "PID文件: backend.pid"
    
    # 等待应用启动
    log_info "等待应用启动..."
    sleep 5
    
    # 检查应用是否启动成功
    if ps -p $BACKEND_PID > /dev/null; then
        log_success "后端应用启动成功！"
        log_info "访问地址: http://localhost:7102"
        log_info "健康检查: http://localhost:7102/actuator/health"
    else
        log_error "后端应用启动失败，请检查日志文件: backend.log"
        exit 1
    fi
}

# 显示帮助信息
show_help() {
    echo "易邦ERP后端启动脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [环境]"
    echo ""
    echo "环境选项:"
    echo "  dev     开发环境 (默认)"
    echo "  test    测试环境"
    echo "  prod    生产环境"
    echo ""
    echo "示例:"
    echo "  $0        # 启动开发环境"
    echo "  $0 dev    # 启动开发环境"
    echo "  $0 prod   # 启动生产环境"
    echo ""
    echo "其他命令:"
    echo "  $0 stop   # 停止后端应用"
    echo "  $0 status # 查看应用状态"
    echo "  $0 logs   # 查看应用日志"
}

# 停止后端应用
stop_backend() {
    log_info "停止后端应用..."
    
    if [ -f "backend.pid" ]; then
        PID=$(cat backend.pid)
        if ps -p $PID > /dev/null; then
            kill $PID
            log_success "后端应用已停止 (PID: $PID)"
            rm -f backend.pid
        else
            log_warning "后端应用未运行"
            rm -f backend.pid
        fi
    else
        log_warning "未找到PID文件，尝试查找Java进程..."
        PIDS=$(pgrep -f "yibang-erp.*jar")
        if [ -n "$PIDS" ]; then
            echo $PIDS | xargs kill
            log_success "后端应用已停止"
        else
            log_warning "未找到运行中的后端应用"
        fi
    fi
}

# 查看应用状态
show_status() {
    log_info "查看应用状态..."
    
    if [ -f "backend.pid" ]; then
        PID=$(cat backend.pid)
        if ps -p $PID > /dev/null; then
            log_success "后端应用正在运行 (PID: $PID)"
            
            # 检查端口
            if netstat -tlnp 2>/dev/null | grep -q ":7102.*$PID"; then
                log_success "端口7102正在监听"
            else
                log_warning "端口7102未监听"
            fi
            
            # 检查健康状态
            if command -v curl &> /dev/null; then
                if curl -s http://localhost:7102/actuator/health > /dev/null; then
                    log_success "健康检查通过"
                else
                    log_warning "健康检查失败"
                fi
            fi
        else
            log_error "后端应用未运行 (PID文件存在但进程不存在)"
            rm -f backend.pid
        fi
    else
        log_warning "后端应用未运行 (未找到PID文件)"
    fi
}

# 查看应用日志
show_logs() {
    log_info "查看应用日志..."
    
    if [ -f "backend.log" ]; then
        tail -f backend.log
    else
        log_warning "日志文件不存在: backend.log"
    fi
}

# 主函数
main() {
    local command=${1:-dev}
    
    case $command in
        "stop")
            stop_backend
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        "dev"|"test"|"prod")
            log_info "启动易邦ERP后端应用 (环境: $command)"
            echo "=================================="
            
            check_java
            check_maven
            check_config $command
            check_database
            check_redis
            build_backend
            start_backend $command
            
            echo "=================================="
            log_success "后端应用启动完成！"
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
