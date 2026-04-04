#!/bin/bash

# Campus Review 微服务启动脚本（低内存优化版）
# 适用于 4GB 内存的服务器
# 使用方法: bash start-services.sh {start|stop|restart|status}

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="/home/ubuntu/download/campus-review"
LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/logs"

# 创建日志和 PID 目录
mkdir -p "$LOG_DIR"

# JVM 优化参数（低内存配置）
JVM_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError"

# 服务配置: 名称 端口 JAR路径
declare -A SERVICES=(
    ["user-service"]="8104 $PROJECT_ROOT/campus-review-service/user-service/target/user-service-0.0.1-SNAPSHOT.jar"
    ["restaurant-service"]="8102 $PROJECT_ROOT/campus-review-service/restaurant-service/target/restaurant-service-0.0.1-SNAPSHOT.jar"
    ["review-service"]="8103 $PROJECT_ROOT/campus-review-service/review-service/target/review-service-0.0.1-SNAPSHOT.jar"
    ["user-gateway"]="8001 $PROJECT_ROOT/campus-review-gateway/campus-review-user-gateway/target/campus-review-user-gateway-0.0.1-SNAPSHOT.jar"
    ["admin-gateway"]="8002 $PROJECT_ROOT/campus-review-gateway/campus-review-admin-gateway/target/campus-review-admin-gateway-0.0.1-SNAPSHOT.jar"
)

# 启动顺序（先启动服务，后启动网关）
START_ORDER=("user-service" "restaurant-service" "review-service" "user-gateway" "admin-gateway")

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取服务 PID
get_pid() {
    local service=$1
    local pid_file="$PID_DIR/${service}.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo "$pid"
            return 0
        fi
    fi

    # 尝试通过端口查找
    local port=$(echo "${SERVICES[$service]}" | cut -d' ' -f1)
    local pid=$(netstat -tlnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)
    if [ -n "$pid" ] && [ "$pid" != "-" ]; then
        echo "$pid"
        return 0
    fi

    return 1
}

# 检查服务状态
check_status() {
    local service=$1
    local pid=$(get_pid "$service")

    if [ -n "$pid" ]; then
        return 0
    fi
    return 1
}

# 启动单个服务
start_service() {
    local service=$1
    local config="${SERVICES[$service]}"
    local port=$(echo "$config" | cut -d' ' -f1)
    local jar_path=$(echo "$config" | cut -d' ' -f2)

    if check_status "$service"; then
        log_warn "$service 已在运行 (PID: $(get_pid $service))"
        return 0
    fi

    if [ ! -f "$jar_path" ]; then
        log_error "JAR 文件不存在: $jar_path"
        return 1
    fi

    log_info "启动 $service (端口: $port)..."

    # 后台启动服务
    nohup java $JVM_OPTS \
        -Dspring.profiles.active=dev \
        -Dlogging.file.path="$LOG_DIR" \
        -jar "$jar_path" \
        > "$LOG_DIR/${service}.log" 2>&1 &

    local pid=$!
    echo "$pid" > "$PID_DIR/${service}.pid"

    # 等待服务启动
    sleep 3

    if check_status "$service"; then
        log_info "$service 启动成功 (PID: $pid)"
        return 0
    else
        log_error "$service 启动失败，请检查日志: $LOG_DIR/${service}.log"
        return 1
    fi
}

# 停止单个服务
stop_service() {
    local service=$1

    if ! check_status "$service"; then
        log_warn "$service 未运行"
        return 0
    fi

    local pid=$(get_pid "$service")
    log_info "停止 $service (PID: $pid)..."

    kill "$pid" 2>/dev/null || true

    # 等待进程结束
    local count=0
    while ps -p "$pid" > /dev/null 2>&1 && [ $count -lt 30 ]; do
        sleep 1
        count=$((count + 1))
    done

    if ps -p "$pid" > /dev/null 2>&1; then
        log_warn "$service 未响应，强制终止..."
        kill -9 "$pid" 2>/dev/null || true
    fi

    rm -f "$PID_DIR/${service}.pid"
    log_info "$service 已停止"
}

# 启动所有服务
start_all() {
    log_info "========== 启动所有服务 =========="
    log_info "JVM 参数: $JVM_OPTS"
    echo ""

    local failed=0
    for service in "${START_ORDER[@]}"; do
        if ! start_service "$service"; then
            failed=$((failed + 1))
        fi
    done

    echo ""
    log_info "========== 启动完成 =========="

    if [ $failed -gt 0 ]; then
        log_error "$failed 个服务启动失败"
        return 1
    fi

    show_status
    show_memory
}

# 停止所有服务
stop_all() {
    log_info "========== 停止所有服务 =========="

    # 反向停止（先停网关，后停服务）
    local reversed_order=()
    for ((i=${#START_ORDER[@]}-1; i>=0; i--)); do
        reversed_order+=("${START_ORDER[$i]}")
    done

    for service in "${reversed_order[@]}"; do
        stop_service "$service"
    done

    log_info "========== 所有服务已停止 =========="
}

# 显示服务状态
show_status() {
    echo ""
    echo -e "${BLUE}服务状态:${NC}"
    echo "--------------------------------------------"
    printf "%-20s %-8s %-8s %s\n" "服务" "端口" "状态" "PID"
    echo "--------------------------------------------"

    for service in "${START_ORDER[@]}"; do
        local config="${SERVICES[$service]}"
        local port=$(echo "$config" | cut -d' ' -f1)

        if check_status "$service"; then
            local pid=$(get_pid "$service")
            printf "%-20s %-8s ${GREEN}%-8s${NC} %s\n" "$service" "$port" "运行中" "$pid"
        else
            printf "%-20s %-8s ${RED}%-8s${NC} %s\n" "$service" "$port" "未运行" "-"
        fi
    done

    echo "--------------------------------------------"
}

# 显示内存使用
show_memory() {
    echo ""
    echo -e "${BLUE}内存使用:${NC}"
    free -h
    echo ""

    # 显示 Java 进程内存
    echo -e "${BLUE}Java 进程内存:${NC}"
    ps aux | grep java | grep -v grep | awk '{printf "%-10s %s %s %s\n", $1, $4"%", $6/1024"M", $11}' | head -10
}

# 查看日志
show_logs() {
    local service=$1

    if [ -z "$service" ]; then
        log_info "可用日志文件:"
        ls -la "$LOG_DIR"/*.log 2>/dev/null || echo "无日志文件"
        return
    fi

    local log_file="$LOG_DIR/${service}.log"
    if [ -f "$log_file" ]; then
        tail -100 "$log_file"
    else
        log_error "日志文件不存在: $log_file"
    fi
}

# 主函数
main() {
    local action=$1
    local service=$2

    case "$action" in
        start)
            if [ -n "$service" ]; then
                start_service "$service"
            else
                start_all
            fi
            ;;
        stop)
            if [ -n "$service" ]; then
                stop_service "$service"
            else
                stop_all
            fi
            ;;
        restart)
            if [ -n "$service" ]; then
                stop_service "$service"
                sleep 2
                start_service "$service"
            else
                stop_all
                sleep 2
                start_all
            fi
            ;;
        status)
            show_status
            show_memory
            ;;
        logs)
            show_logs "$service"
            ;;
        memory)
            show_memory
            ;;
        *)
            echo "使用方法: $0 {start|stop|restart|status|logs|memory} [service-name]"
            echo ""
            echo "命令:"
            echo "  start   [service]  - 启动所有服务或指定服务"
            echo "  stop    [service]  - 停止所有服务或指定服务"
            echo "  restart [service]  - 重启所有服务或指定服务"
            echo "  status             - 显示服务状态"
            echo "  logs    [service]  - 查看日志"
            echo "  memory             - 显示内存使用"
            echo ""
            echo "服务列表:"
            for service in "${START_ORDER[@]}"; do
                local config="${SERVICES[$service]}"
                local port=$(echo "$config" | cut -d' ' -f1)
                echo "  - $service (端口: $port)"
            done
            echo ""
            echo "JVM 参数: $JVM_OPTS"
            exit 1
            ;;
    esac
}

main "$@"
