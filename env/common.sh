#!/bin/bash
# sh/common.sh - 通用函数和变量

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

MAX_RETRY_ATTEMPTS=12    # 最大重试次数增加到30次
RETRY_INTERVAL=10        # 重试间隔调整为10秒
CRITICAL_SERVICE_RETRY=30 # 关键服务最大重试次数

# 获取脚本真实路径
get_script_dir() {
    echo "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
}

# 显示带颜色的消息
show() {
    local color=$1
    shift
    echo -e "${color}$*${NC}"
}

show_header() {
    echo -e "\n${YELLOW}========== $1 ==========${NC}"
}

# 检查命令是否存在
check_cmd() {
    if ! command -v "$1" &> /dev/null; then
        show $RED "✗ 命令 $1 未安装"
        exit 1
    fi
}

# 检查Docker环境
check_docker() {
    check_cmd docker
    check_cmd docker-compose

    if ! docker info &> /dev/null; then
        show $RED "✗ Docker守护程序未运行"
        exit 1
    fi
}

# 服务检测函数
wait_for_service() {
    local service=$1
    local port=$2
    local type=${3:-http}
    local max_retry=$MAX_RETRY_ATTEMPTS

    # 对关键服务使用更长等待时间
    [[ "$service" =~ ^(mysql|nacos)$ ]] && max_retry=$CRITICAL_SERVICE_RETRY

    show $YELLOW "等待服务 $service 就绪 (最长等待: $((max_retry*RETRY_INTERVAL/60)) 分钟)..."

    for ((try=0; try<max_retry; try++)); do
        case $type in
            http)
                docker run --rm --network=qr_network curlimages/curl -sS --retry 3 --retry-delay 2 "http://${service}:${port}" &>/dev/null && break
                ;;
            tcp)
                docker run --rm --network=qr_network busybox nc -zw 2 $service $port &>/dev/null && break
                ;;
        esac
        sleep $RETRY_INTERVAL
        echo -n "."
    done

    if ((try == max_retry)); then
        show $RED "✗ 服务 $service 连接超时 (已尝试 $max_retry 次)"
        return 1
    else
        show $GREEN "✓ 服务 $service 在 $((try*RETRY_INTERVAL)) 秒后就绪"
    fi
}

# 所有docker-compose文件列表
COMPOSE_FILES=(
    "mysql/mysql-docker-compose.yaml"
    "nacos-docker/nacos/cluster-hostname.yaml"
    "seata/seata-docker-compose.yml"
    "sentinel/sentinel-docker-compose.yml"
    "redis/redis-docker-compose.yml"
    "rmq/rmq-docker-compose.yml"
)