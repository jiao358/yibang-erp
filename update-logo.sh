#!/bin/bash

# Logo更新脚本
# 使用方法: ./update-logo.sh [logo文件路径] [favicon文件路径]

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

# 检查文件是否存在
check_file() {
    local file=$1
    if [ ! -f "$file" ]; then
        log_error "文件不存在: $file"
        return 1
    fi
    return 0
}

# 创建public目录
create_public_dir() {
    log_info "创建public目录..."
    mkdir -p frontend/public
    log_success "public目录创建完成"
}

# 更新favicon
update_favicon() {
    local favicon_file=$1
    
    if [ -z "$favicon_file" ]; then
        log_warning "未提供favicon文件，跳过favicon更新"
        return 0
    fi
    
    if ! check_file "$favicon_file"; then
        return 1
    fi
    
    log_info "更新favicon..."
    
    # 复制favicon文件
    cp "$favicon_file" frontend/public/favicon.ico
    
    # 更新HTML文件
    local html_file="frontend/index.html"
    if [ -f "$html_file" ]; then
        # 备份原文件
        cp "$html_file" "${html_file}.backup"
        
        # 更新favicon链接
        sed -i.tmp 's|<link rel="icon"[^>]*>|<link rel="icon" type="image/x-icon" href="/favicon.ico" />|g' "$html_file"
        rm -f "${html_file}.tmp"
        
        log_success "favicon更新完成"
    else
        log_error "HTML文件不存在: $html_file"
        return 1
    fi
}

# 更新侧边栏logo
update_sidebar_logo() {
    local logo_file=$1
    
    if [ -z "$logo_file" ]; then
        log_warning "未提供logo文件，跳过侧边栏logo更新"
        return 0
    fi
    
    if ! check_file "$logo_file"; then
        return 1
    fi
    
    log_info "更新侧边栏logo..."
    
    # 复制logo文件
    cp "$logo_file" frontend/public/logo.png
    
    # 更新Vue组件
    local vue_file="frontend/src/layout/MainLayout.vue"
    if [ -f "$vue_file" ]; then
        # 备份原文件
        cp "$vue_file" "${vue_file}.backup"
        
        # 更新logo部分
        sed -i.tmp 's|<div class="logo-text">懿邦ERP</div>|<img src="/logo.png" alt="懿邦ERP" class="logo-image" />\n        <div class="logo-text">懿邦ERP</div>|g' "$vue_file"
        rm -f "${vue_file}.tmp"
        
        # 添加CSS样式
        if ! grep -q "logo-image" "$vue_file"; then
            # 在CSS部分添加样式
            sed -i.tmp '/\.logo-text {/i\
.logo-image {\
  width: 32px;\
  height: 32px;\
  margin-right: 8px;\
  vertical-align: middle;\
}\
\
.logo {\
  display: flex;\
  align-items: center;\
}' "$vue_file"
            rm -f "${vue_file}.tmp"
        fi
        
        log_success "侧边栏logo更新完成"
    else
        log_error "Vue组件文件不存在: $vue_file"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "Logo更新脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [logo文件] [favicon文件]"
    echo ""
    echo "参数说明:"
    echo "  logo文件     侧边栏logo文件路径 (PNG格式)"
    echo "  favicon文件  浏览器标签页图标文件路径 (ICO格式)"
    echo ""
    echo "使用示例:"
    echo "  $0 logo.png favicon.ico                    # 更新logo和favicon"
    echo "  $0 logo.png                                # 只更新侧边栏logo"
    echo "  $0 '' favicon.ico                          # 只更新favicon"
    echo ""
    echo "注意事项:"
    echo "  - logo文件建议尺寸: 32x32 或 64x64 像素"
    echo "  - favicon文件建议尺寸: 16x16, 32x32, 48x48 像素"
    echo "  - 支持PNG, ICO, SVG格式"
    echo "  - 更新后需要重新构建前端"
}

# 主函数
main() {
    local logo_file=$1
    local favicon_file=$2
    
    if [ "$1" = "help" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_help
        exit 0
    fi
    
    log_info "开始更新logo配置..."
    echo "=================================="
    
    # 创建public目录
    create_public_dir
    
    # 更新favicon
    update_favicon "$favicon_file"
    
    # 更新侧边栏logo
    update_sidebar_logo "$logo_file"
    
    echo "=================================="
    log_success "Logo更新完成！"
    
    if [ -n "$logo_file" ] || [ -n "$favicon_file" ]; then
        echo ""
        log_info "下一步操作:"
        log_info "1. 重新构建前端: ./start-frontend.sh build"
        log_info "2. 或者重启开发服务器: ./start-frontend.sh dev"
        echo ""
        log_info "文件位置:"
        if [ -n "$logo_file" ]; then
            log_info "  侧边栏logo: frontend/public/logo.png"
        fi
        if [ -n "$favicon_file" ]; then
            log_info "  浏览器图标: frontend/public/favicon.ico"
        fi
    fi
}

# 执行主函数
main "$@"
