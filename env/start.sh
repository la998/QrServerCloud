#!/bin/bash
# start.sh - 启动所有服务（兼容Bash 3）

source "$(dirname "$0")/common.sh"

SCRIPT_DIR=$(get_script_dir)
cd "$SCRIPT_DIR/" || exit 1

check_docker

# 服务启动顺序及检测配置（使用普通数组替代关联数组）
SERVICES=(
    "mysql 3306 tcp"
    "nacos 8848 http"
    "seata 8091 tcp"
    "sentinel 8858 http"
    "redis 6379 tcp"
)

main() {
    show_header "服务启动流程"

    # 创建专用网络
    if ! docker network inspect qr_network &>/dev/null; then
        show $YELLOW "▶ 创建Docker网络..."
        docker network create qr_network
    fi

    # 按顺序启动服务
    for service_info in "${SERVICES[@]}"; do
        local service=$(echo "$service_info" | awk '{print $1}')
        local port=$(echo "$service_info" | awk '{print $2}')
        local type=$(echo "$service_info" | awk '{print $3}')

        show $YELLOW "▶ 启动 $service"
        case $service in
            mysql)
                docker-compose -f mysql/mysql-docker-compose.yaml up -d
                ;;
            nacos)
                docker-compose -f nacos-docker/nacos/cluster-hostname.yaml up -d
                ;;
            seata)
                docker-compose -f seata/seata-docker-compose.yml up -d
                ;;
            sentinel)
                docker-compose -f sentinel/sentinel-docker-compose.yml up -d
                ;;
            redis)
                docker-compose -f redis/redis-docker-compose.yml up -d
                ;;
        esac

        wait_for_service $service $port $type || exit 1
    done

    show_header "服务访问信息"
    echo "Nacos控制台:    http://localhost:8848/nacos (nacos/nacos)"
    echo "Sentinel面板:   http://localhost:8858"
    echo "Seata控制台:    http://localhost:7091"
}

trap 'show $RED "✗ 启动过程中断"; exit 2' INT TERM
main "$@"