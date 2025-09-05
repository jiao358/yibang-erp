#!/bin/bash

# Blue/Green zero-downtime switch for Yibang ERP (aaPanel Nginx, systemd backend)
# Usage:
#   bluegreen-switch.sh                 # auto-pick free port and switch
#   bluegreen-switch.sh <new_port>      # use specific port and switch
#   bluegreen-switch.sh rollback        # rollback to previous port and stop new
#   bluegreen-switch.sh status          # show current Nginx port and instances
#   bluegreen-switch.sh -h|--help|help  # show help
#
# Behavior:
# - Starts a second JVM instance on a new port with prod config
# - Waits for health to PASS
# - Atomically updates Nginx proxy_pass to the new port and reloads Nginx
# - Stops the old instance (systemd yibang-erp if old_port=7102; or PID by file)
#
# Requirements:
# - JAVA 17 at /www/server/java/jdk-17.0.8/bin/java
# - JAR at /opt/yibang-erp/yibang-erp-1.0.0.jar
# - External prod config at /opt/yibang-erp/config/
# - Nginx vhost: /www/server/panel/vhost/nginx/yibang-erp.conf with proxy_pass http://localhost:PORT;

set -euo pipefail

JAVA_BIN="/www/server/java/jdk-17.0.8/bin/java"
JAR_PATH="/opt/yibang-erp/yibang-erp-1.0.0.jar"
CONFIG_DIR="/opt/yibang-erp/config/"
NGINX_VHOST="/www/server/panel/vhost/nginx/yibang-erp.conf"
NGINX_BIN="/www/server/nginx/sbin/nginx"
RUN_DIR="/opt/yibang-erp/run"
LOG_DIR="/opt/yibang-erp/logs"
HEALTH_PATH="/api/health"
DEFAULT_BASE_PORT=7102

mkdir -p "$RUN_DIR" "$LOG_DIR"

log() { echo -e "\033[0;34m[SWITCH]\033[0m $*"; }
succ(){ echo -e "\033[0;32m[SUCCESS]\033[0m $*"; }
warn(){ echo -e "\033[1;33m[WARN]\033[0m $*"; }
err() { echo -e "\033[0;31m[ERROR]\033[0m $*"; }

usage(){ cat <<USAGE
Blue/Green zero-downtime switch (aaPanel Nginx + systemd)

Commands:
  $0                   # 自动选择空闲端口，拉起新实例，健康通过后切换Nginx并停止旧实例
  $0 <new_port>        # 使用指定端口进行切换（示例: $0 7103）
  $0 rollback          # 回滚到上一个端口，并停止新实例
  $0 status            # 查看当前Nginx指向的端口与本机实例情况
  $0 -h|--help|help    # 查看帮助

流程概述：
  1) 在新端口启动新的JVM实例（读取 /opt/yibang-erp/config/）
  2) 健康检查通过 (/api/健康) 后，原子切换Nginx到新端口
  3) 停止旧实例（旧端口=7102时通过systemctl停止 yibang-erp）

注意：执行前请先将新JAR上传覆盖 /opt/yibang-erp/yibang-erp-1.0.0.jar
USAGE
}

preflight(){
  log "预检环境..."
  [ -x "$JAVA_BIN" ] || { err "JAVA_BIN 不存在: $JAVA_BIN"; exit 1; }
  [ -f "$JAR_PATH" ] || { err "JAR 不存在: $JAR_PATH"; exit 1; }
  [ -d "$CONFIG_DIR" ] || { err "配置目录不存在: $CONFIG_DIR"; exit 1; }
  [ -f "$NGINX_VHOST" ] || { err "Nginx站点配置不存在: $NGINX_VHOST"; exit 1; }
  [ -x "$NGINX_BIN" ] || { err "Nginx执行文件不存在: $NGINX_BIN"; exit 1; }
  succ "预检通过"
}

current_port() {
  local p
  p=$(grep -Eo "proxy_pass[[:space:]]+http://localhost:[0-9]+;" "$NGINX_VHOST" | tail -n1 | grep -Eo "[0-9]+" || true)
  echo "$p"
}

is_port_free() {
  local port=$1
  ! ss -tln | awk '{print $4}' | grep -q ":$port$"
}

pick_port() {
  local start=${1:-7103}
  local end=$((start+20))
  for p in $(seq "$start" "$end"); do
    if is_port_free "$p"; then echo "$p"; return; fi
  done
  err "No free port found in range $start-$end"; exit 1
}

start_instance() {
  local port=$1
  local pid_file="$RUN_DIR/erp-$port.pid"
  local log_file="$LOG_DIR/erp-$port.log"

  if ! is_port_free "$port"; then
    warn "端口 $port 已被占用；视为实例已运行"
    return 0
  fi

  log "在端口 :$port 启动新实例 (读取 $CONFIG_DIR)"
  nohup "$JAVA_BIN" -jar \
    -Dspring.profiles.active=prod \
    --spring.config.additional-location="$CONFIG_DIR" \
    --server.port="$port" \
    "$JAR_PATH" > "$log_file" 2>&1 &
  echo $! > "$pid_file"
  log "新实例日志: $log_file  (PID文件: $pid_file)"
}

wait_health() {
  local port=$1
  local tries=40
  local sleep_s=1
  local url="http://127.0.0.1:${port}${HEALTH_PATH}"
  log "等待健康检查通过: $url"
  for i in $(seq 1 "$tries"); do
    if curl -sf "$url" >/dev/null 2>&1; then succ "健康检查通过 (:${port})"; return 0; fi
    sleep "$sleep_s"
  done
  err "健康检查失败 (:${port})"; return 1
}

nginx_switch() {
  local new_port=$1
  local backup="$NGINX_VHOST.bak.$(date +%Y%m%d_%H%M%S)"
  cp "$NGINX_VHOST" "$backup"
  sed -i "s|proxy_pass[[:space:]]\+http://localhost:[0-9]\+;|proxy_pass http://localhost:${new_port};|g" "$NGINX_VHOST"
  "$NGINX_BIN" -t
  "$NGINX_BIN" -s reload
  succ "Nginx 已切换到 :$new_port (备份: $backup)"
}

stop_instance() {
  local port=$1
  local pid_file="$RUN_DIR/erp-$port.pid"
  if [ "$port" = "$DEFAULT_BASE_PORT" ]; then
    log "停止 systemd 主服务 (端口 $port)"
    systemctl stop yibang-erp || true
    return 0
  fi
  if [ -f "$pid_file" ]; then
    local pid
    pid=$(cat "$pid_file")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "停止实例 (PID $pid, 端口 $port)"
      kill "$pid" || true
      sleep 1
      if kill -0 "$pid" 2>/dev/null; then kill -9 "$pid" || true; fi
    fi
    rm -f "$pid_file"
  else
    warn "未找到PID文件，可能已停止 (:${port})"
  fi
}

status(){
  local cur=$(current_port)
  echo "当前Nginx指向端口: ${cur:-未知}"
  echo "本机监听端口:"
  ss -tln | awk 'NR>1{print $4}' | sed -n 's/.*:\([0-9][0-9]*\)$/\1/p' | sort -n | uniq | sed 's/^/  :/'
  echo "运行中的ERP实例(PID文件):"
  ls -1 "$RUN_DIR"/erp-*.pid 2>/dev/null | sed 's/^/  /' || echo "  无"
}

banner_switch(){
  local old_port=$1
  local new_port=$2
  cat <<BANNER
================ 蓝绿切换即将执行 ================
旧端口: $old_port
新端口: $new_port
步骤:
  1) 在 :$new_port 启动新实例
  2) 健康检查通过后切换Nginx到 :$new_port
  3) 停止旧实例 (:${old_port})
===================================================
BANNER
}

recap(){
  local old_port=$1
  local new_port=$2
  cat <<RECAP
================ 切换完成 ================
旧端口: $old_port -> 新端口: $new_port
回滚:   $(basename "$0") rollback
状态:   $(basename "$0") status
查看:
  tail -n 100 $LOG_DIR/erp-$new_port.log | cat
  curl -v http://127.0.0.1:${new_port}${HEALTH_PATH} | cat
=========================================
RECAP
}

rollback() {
  local prev_port_file="$RUN_DIR/prev.port"
  local new_port_file="$RUN_DIR/current.port"
  if [ ! -f "$prev_port_file" ] || [ ! -f "$new_port_file" ]; then
    err "无可用的切换历史，无法回滚"; exit 1
  fi
  local prev_port=$(cat "$prev_port_file")
  local new_port=$(cat "$new_port_file")
  log "回滚 Nginx 到 :$prev_port"
  nginx_switch "$prev_port"
  log "停止新实例 :$new_port"
  stop_instance "$new_port"
  succ "回滚完成"
}

# Command dispatch
case "${1:-switch}" in
  -h|--help|help)
    usage; exit 0;;
  status)
    preflight; status; exit 0;;
  rollback)
    preflight; rollback; exit 0;;
  *) : ;; # continue to switch flow
esac

preflight

# Determine ports
OLD_PORT=$(current_port)
if [ -z "$OLD_PORT" ]; then
  warn "无法从 $NGINX_VHOST 解析当前端口，默认 $DEFAULT_BASE_PORT"
  OLD_PORT=$DEFAULT_BASE_PORT
fi
NEW_PORT_ARG=${1:-}
if [[ "$NEW_PORT_ARG" =~ ^[0-9]+$ ]]; then
  NEW_PORT=$NEW_PORT_ARG
else
  NEW_PORT=$(pick_port 7103)
fi

if [ "$NEW_PORT" = "$OLD_PORT" ]; then
  err "新端口与当前端口相同 ($OLD_PORT)，请指定其它端口"; exit 1
fi

banner_switch "$OLD_PORT" "$NEW_PORT"

start_instance "$NEW_PORT"
wait_health "$NEW_PORT"

# Record history
echo "$OLD_PORT" > "$RUN_DIR/prev.port"
echo "$NEW_PORT" > "$RUN_DIR/current.port"

nginx_switch "$NEW_PORT"

# Give a brief drain period then stop old
sleep 1
stop_instance "$OLD_PORT"

succ "Blue/Green switch complete: $OLD_PORT -> $NEW_PORT"
recap "$OLD_PORT" "$NEW_PORT"
