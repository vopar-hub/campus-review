#!/bin/bash

# 校园美食点评平台 - 微服务批量启动脚本
# 使用方法：./start-services.sh [all|core|gateway]

SERVICES=("user-service" "restaurant-service" "review-service" "interaction-service" "ranking-service" "notification-service" "risk-control-service" "admin-service")
GATEWAYS=("campus-review-user-gateway" "campus-review-admin-gateway")

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_ROOT/logs"

mkdir -p "$LOG_DIR"

start_service() {
    local service=$1
    local module_path=""

    if [[ "$service" == *"gateway"* ]]; then
        module_path="campus-review-gateway/$service"
    else
        module_path="campus-review-service/$service"
    fi

    echo "Starting $service..."
    cd "$PROJECT_ROOT"
    nohup mvn -pl "$module_path" -am spring-boot:run > "$LOG_DIR/$service.log" 2>&1 &
    echo "PID: $!"
}

start_core() {
    echo "=== Starting Core Services ==="
    for service in "${SERVICES[@]}"; do
        start_service "$service"
        sleep 5
    done
}

start_gateways() {
    echo "=== Starting Gateways ==="
    for gateway in "${GATEWAYS[@]}"; do
        start_service "$gateway"
        sleep 3
    done
}

case "${1:-all}" in
    core)
        start_core
        ;;
    gateway)
        start_gateways
        ;;
    all)
        start_core
        echo ""
        start_gateways
        ;;
    *)
        echo "Usage: $0 [all|core|gateway]"
        echo "  all     - Start all services and gateways"
        echo "  core    - Start only core services"
        echo "  gateway - Start only gateways"
        exit 1
        ;;
esac

echo ""
echo "=== Startup Complete ==="
echo "Logs are in: $LOG_DIR"
