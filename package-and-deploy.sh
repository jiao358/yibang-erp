#!/bin/bash

# 易邦ERP项目打包和部署脚本
# 使用方法: ./package-and-deploy.sh [命令] [参数]

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
PROJECT_NAME="yibang-erp"
VERSION=$(date +%Y%m%d_%H%M%S)
PACKAGE_DIR="packages"
DEPLOY_DIR="deploy"
BACKUP_DIR="backups"

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

log_package() {
    echo -e "${PURPLE}[PACKAGE]${NC} $1"
}

log_deploy() {
    echo -e "${CYAN}[DEPLOY]${NC} $1"
}

# 检查项目结构
check_project_structure() {
    log_info "检查项目结构..."
    
    local required_dirs=("backend" "frontend" "database" "docs")
    local required_files=("README.md" "start-backend.sh" "start-frontend.sh" "start-all.sh")
    
    for dir in "${required_dirs[@]}"; do
        if [ ! -d "$dir" ]; then
            log_error "缺少必要目录: $dir"
            exit 1
        fi
    done
    
    for file in "${required_files[@]}"; do
        if [ ! -f "$file" ]; then
            log_error "缺少必要文件: $file"
            exit 1
        fi
    done
    
    log_success "项目结构检查通过"
}

# 清理构建文件
clean_build_files() {
    log_info "清理构建文件..."
    
    # 清理后端构建文件
    if [ -d "backend/target" ]; then
        rm -rf backend/target
        log_info "已清理后端构建文件"
    fi
    
    # 清理前端构建文件
    if [ -d "frontend/dist" ]; then
        rm -rf frontend/dist
        log_info "已清理前端构建文件"
    fi
    
    # 清理node_modules
    if [ -d "frontend/node_modules" ]; then
        rm -rf frontend/node_modules
        log_info "已清理前端依赖文件"
    fi
    
    # 清理日志文件
    rm -f *.log
    rm -f *.pid
    rm -f *.port
    
    log_success "构建文件清理完成"
}

# 构建后端
build_backend() {
    local profile=${1:-prod}
    
    log_package "构建后端应用 (环境: $profile)..."
    
    cd backend
    
    # 检查Maven
    if [ -f "./mvnw" ]; then
        MVN_CMD="./mvnw"
    elif command -v mvn &> /dev/null; then
        MVN_CMD="mvn"
    else
        log_error "Maven未找到"
        exit 1
    fi
    
    # 构建应用
    $MVN_CMD clean package -DskipTests -P$profile
    
    if [ $? -eq 0 ]; then
        log_success "后端构建成功"
    else
        log_error "后端构建失败"
        exit 1
    fi
    
    cd ..
}

# 构建前端
build_frontend() {
    log_package "构建前端应用..."
    
    cd frontend
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装"
        exit 1
    fi
    
    # 安装依赖
    log_info "安装前端依赖..."
    npm install
    
    # 构建生产版本
    log_info "构建前端生产版本..."
    npm run build
    
    if [ $? -eq 0 ]; then
        log_success "前端构建成功"
    else
        log_error "前端构建失败"
        exit 1
    fi
    
    cd ..
}

# 创建部署包
create_deployment_package() {
    local package_name="${PROJECT_NAME}-${VERSION}"
    local package_path="${PACKAGE_DIR}/${package_name}"
    
    log_package "创建部署包: $package_name"
    
    # 创建打包目录
    mkdir -p "$PACKAGE_DIR"
    mkdir -p "$package_path"
    
    # 复制项目文件
    log_info "复制项目文件..."
    
    # 复制后端文件
    cp -r backend "$package_path/"
    cp -r database "$package_path/"
    cp -r docs "$package_path/"
    
    # 复制前端构建文件
    cp -r frontend "$package_path/"
    
    # 复制启动脚本
    cp start-*.sh "$package_path/"
    cp switch-*.sh "$package_path/"
    
    # 复制配置文件
    cp README.md "$package_path/"
    cp *.md "$package_path/" 2>/dev/null || true
    
    # 创建部署目录结构
    mkdir -p "$package_path/deploy"
    mkdir -p "$package_path/logs"
    mkdir -p "$package_path/backups"
    
    # 创建部署说明文件
    cat > "$package_path/DEPLOY.md" << EOF
# 易邦ERP部署说明

## 部署包信息
- 项目名称: $PROJECT_NAME
- 版本: $VERSION
- 打包时间: $(date)
- 打包环境: $(uname -a)

## 部署步骤

### 1. 解压部署包
\`\`\`bash
unzip ${package_name}.zip
cd ${package_name}
\`\`\`

### 2. 设置执行权限
\`\`\`bash
chmod +x start-*.sh
chmod +x switch-*.sh
\`\`\`

### 3. 启动服务
\`\`\`bash
# 标准模式
./start-all.sh prod start

# 高可用模式
./start-advanced.sh start-ha prod
\`\`\`

### 4. 验证部署
\`\`\`bash
# 查看状态
./start-all.sh status

# 检查健康状态
curl http://localhost:7102/actuator/health
\`\`\`

## 系统要求
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Nginx (可选，用于负载均衡)

## 配置文件
- 后端配置: backend/src/main/resources/application-prod.yml
- 前端配置: frontend/dist/
- 数据库脚本: database/init/

## 日志文件
- 后端日志: logs/backend.log
- 前端日志: logs/frontend.log
- 系统日志: logs/system.log

## 支持
如有问题，请查看文档或联系技术支持。
EOF
    
    log_success "部署包创建完成: $package_path"
    echo $package_path
}

# 创建ZIP压缩包
create_zip_package() {
    local package_path=$1
    local package_name=$(basename "$package_path")
    local zip_file="${PACKAGE_DIR}/${package_name}.zip"
    
    log_package "创建ZIP压缩包..."
    
    cd "$PACKAGE_DIR"
    zip -r "$package_name.zip" "$package_name" -x "*.git*" "*.DS_Store*" "node_modules/*" "target/*"
    cd ..
    
    if [ -f "$zip_file" ]; then
        log_success "ZIP压缩包创建完成: $zip_file"
        
        # 显示文件大小
        local file_size=$(du -h "$zip_file" | cut -f1)
        log_info "文件大小: $file_size"
        
        echo "$zip_file"
    else
        log_error "ZIP压缩包创建失败"
        exit 1
    fi
}

# 部署到服务器
deploy_to_server() {
    local zip_file=$1
    local server_info=$2
    local deploy_path=${3:-/opt/yibang-erp}
    
    log_deploy "部署到服务器: $server_info"
    
    # 解析服务器信息
    local server_host=$(echo $server_info | cut -d'@' -f2 | cut -d':' -f1)
    local server_user=$(echo $server_info | cut -d'@' -f1)
    local server_port=$(echo $server_info | cut -d':' -f2)
    
    if [ -z "$server_port" ]; then
        server_port=22
    fi
    
    log_info "服务器: $server_user@$server_host:$server_port"
    log_info "部署路径: $deploy_path"
    
    # 检查SSH连接
    log_info "检查SSH连接..."
    if ! ssh -p $server_port -o ConnectTimeout=10 $server_user@$server_host "echo 'SSH连接成功'" > /dev/null 2>&1; then
        log_error "SSH连接失败，请检查服务器信息"
        exit 1
    fi
    
    # 上传ZIP文件
    log_info "上传部署包..."
    scp -P $server_port "$zip_file" $server_user@$server_host:/tmp/
    
    # 在服务器上解压和部署
    log_info "在服务器上部署..."
    ssh -p $server_port $server_user@$server_host << EOF
        # 创建部署目录
        sudo mkdir -p $deploy_path
        sudo chown $server_user:$server_user $deploy_path
        
        # 备份现有部署
        if [ -d "$deploy_path/current" ]; then
            sudo mv $deploy_path/current $deploy_path/backup_\$(date +%Y%m%d_%H%M%S)
        fi
        
        # 解压新部署包
        cd $deploy_path
        unzip -q /tmp/$(basename $zip_file)
        mv $(basename $zip_file .zip) current
        
        # 设置执行权限
        chmod +x current/start-*.sh
        chmod +x current/switch-*.sh
        
        # 清理临时文件
        rm -f /tmp/$(basename $zip_file)
        
        echo "部署完成"
EOF
    
    if [ $? -eq 0 ]; then
        log_success "服务器部署完成"
        log_info "部署路径: $deploy_path/current"
    else
        log_error "服务器部署失败"
        exit 1
    fi
}

# 启动远程服务
start_remote_service() {
    local server_info=$1
    local deploy_path=${2:-/opt/yibang-erp}
    local mode=${3:-standard}
    
    log_deploy "启动远程服务 (模式: $mode)..."
    
    # 解析服务器信息
    local server_host=$(echo $server_info | cut -d'@' -f2 | cut -d':' -f1)
    local server_user=$(echo $server_info | cut -d'@' -f1)
    local server_port=$(echo $server_info | cut -d':' -f2)
    
    if [ -z "$server_port" ]; then
        server_port=22
    fi
    
    # 在服务器上启动服务
    ssh -p $server_port $server_user@$server_host << EOF
        cd $deploy_path/current
        
        case "$mode" in
            "standard")
                ./start-all.sh prod start
                ;;
            "ha")
                ./start-advanced.sh start-ha prod
                ;;
            *)
                echo "未知模式: $mode"
                exit 1
                ;;
        esac
EOF
    
    if [ $? -eq 0 ]; then
        log_success "远程服务启动完成"
    else
        log_error "远程服务启动失败"
        exit 1
    fi
}

# 检查远程服务状态
check_remote_status() {
    local server_info=$1
    local deploy_path=${2:-/opt/yibang-erp}
    
    log_deploy "检查远程服务状态..."
    
    # 解析服务器信息
    local server_host=$(echo $server_info | cut -d'@' -f2 | cut -d':' -f1)
    local server_user=$(echo $server_info | cut -d'@' -f1)
    local server_port=$(echo $server_info | cut -d':' -f2)
    
    if [ -z "$server_port" ]; then
        server_port=22
    fi
    
    # 在服务器上检查状态
    ssh -p $server_port $server_user@$server_host << EOF
        cd $deploy_path/current
        ./start-all.sh status
EOF
}

# 完整打包流程
package_project() {
    local profile=${1:-prod}
    
    log_package "开始项目打包流程..."
    echo "=================================="
    
    check_project_structure
    clean_build_files
    build_backend $profile
    build_frontend
    local package_path=$(create_deployment_package)
    local zip_file=$(create_zip_package "$package_path")
    
    echo "=================================="
    log_success "项目打包完成！"
    log_info "部署包: $zip_file"
    
    echo "$zip_file"
}

# 完整部署流程
deploy_project() {
    local server_info=$1
    local deploy_path=${2:-/opt/yibang-erp}
    local mode=${3:-standard}
    local profile=${4:-prod}
    
    log_deploy "开始项目部署流程..."
    echo "=================================="
    
    # 打包项目
    local zip_file=$(package_project $profile)
    
    # 部署到服务器
    deploy_to_server "$zip_file" "$server_info" "$deploy_path"
    
    # 启动服务
    start_remote_service "$server_info" "$deploy_path" "$mode"
    
    echo "=================================="
    log_success "项目部署完成！"
    
    # 显示访问信息
    local server_host=$(echo $server_info | cut -d'@' -f2 | cut -d':' -f1)
    log_info "访问地址:"
    log_info "  前端: http://$server_host:7101"
    log_info "  后端: http://$server_host:7102"
    log_info "  健康检查: http://$server_host:7102/actuator/health"
}

# 显示帮助信息
show_help() {
    echo "易邦ERP项目打包和部署脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令] [参数]"
    echo ""
    echo "打包命令:"
    echo "  package [环境]             打包项目"
    echo "  build-backend [环境]       构建后端"
    echo "  build-frontend             构建前端"
    echo "  clean                      清理构建文件"
    echo ""
    echo "部署命令:"
    echo "  deploy [服务器] [路径] [模式] [环境]  完整部署流程"
    echo "  upload [ZIP文件] [服务器] [路径]     上传部署包"
    echo "  start-remote [服务器] [路径] [模式]  启动远程服务"
    echo "  status-remote [服务器] [路径]       检查远程状态"
    echo ""
    echo "参数说明:"
    echo "  环境: dev/test/prod (默认: prod)"
    echo "  服务器: user@host:port (默认端口: 22)"
    echo "  路径: 部署路径 (默认: /opt/yibang-erp)"
    echo "  模式: standard/ha (默认: standard)"
    echo ""
    echo "使用示例:"
    echo "  $0 package prod                    # 打包生产版本"
    echo "  $0 deploy user@192.168.1.100      # 部署到服务器"
    echo "  $0 deploy user@server.com /opt/app ha prod  # 高可用部署"
    echo "  $0 upload package.zip user@server  # 上传部署包"
    echo "  $0 start-remote user@server        # 启动远程服务"
    echo "  $0 status-remote user@server       # 检查远程状态"
    echo ""
    echo "注意事项:"
    echo "  - 需要配置SSH密钥认证"
    echo "  - 服务器需要安装Java、Node.js、MySQL、Redis"
    echo "  - 需要sudo权限创建部署目录"
}

# 主函数
main() {
    local command=$1
    local param1=$2
    local param2=$3
    local param3=$4
    local param4=$5
    
    case $command in
        "package")
            package_project $param1
            ;;
        "build-backend")
            build_backend $param1
            ;;
        "build-frontend")
            build_frontend
            ;;
        "clean")
            clean_build_files
            ;;
        "deploy")
            if [ -z "$param1" ]; then
                log_error "请指定服务器信息"
                exit 1
            fi
            deploy_project "$param1" "$param2" "$param3" "$param4"
            ;;
        "upload")
            if [ -z "$param1" ] || [ -z "$param2" ]; then
                log_error "请指定ZIP文件和服务器信息"
                exit 1
            fi
            deploy_to_server "$param1" "$param2" "$param3"
            ;;
        "start-remote")
            if [ -z "$param1" ]; then
                log_error "请指定服务器信息"
                exit 1
            fi
            start_remote_service "$param1" "$param2" "$param3"
            ;;
        "status-remote")
            if [ -z "$param1" ]; then
                log_error "请指定服务器信息"
                exit 1
            fi
            check_remote_status "$param1" "$param2"
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
