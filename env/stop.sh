#!/bin/bash
# stop.sh - 停止所有服务（保留容器和数据）
set -eo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # 重置颜色

check_sudo() {
  echo -e "${YELLOW}▶ 检查sudo权限...${NC}"
  sudo -v || { echo -e "${RED}✗ 需要sudo权限执行操作${NC}"; exit 1; }
  while true; do sudo -n true; sleep 60; kill -0 "$$" || exit; done 2>/dev/null &
}

stop_containers() {
  echo -e "${YELLOW}▶ 正在停止容器（保留容器和数据）...${NC}"
  # 使用 stop 代替 down，保留容器和数据
  docker-compose -f rmq/rmq-docker-compose.yml stop || true
  docker-compose -f seata/seata-docker-compose.yml stop || true
  docker-compose -f sentinel/sentinel-docker-compose.yml stop || true
  docker-compose -f redis/redis-docker-compose.yml stop || true
  docker-compose -f nacos-docker/nacos/cluster-hostname.yaml stop || true
  docker-compose -f mysql/mysql-docker-compose.yaml stop || true

  echo -e "${GREEN}✓ 容器已停止，容器和数据保留${NC}"
}

trap 'echo -e "${RED}✗ 脚本执行中断，请检查错误！${NC}"; exit 1' INT TERM

check_sudo
stop_containers