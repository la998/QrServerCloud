#!/bin/bash
# start.sh - 启动所有服务
set -eo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # 重置颜色

wait_for_service() {
  local service=$1
  local port=$2
  local max_retry=30
  local try=0

  echo -ne "等待 ${YELLOW}${service}${NC} 就绪..."
  while ! nc -z localhost $port && [ $try -lt $max_retry ]; do
    sleep 2
    echo -n "."
    try=$((try + 1))
  done

  if [ $try -eq $max_retry ]; then
    echo -e " ${RED}✗ 服务启动超时${NC}"
    exit 1
  else
    echo -e " ${GREEN}✓${NC}"
  fi
}

case "$1" in
  prod)
    echo "生产环境模式"
    export COMPOSE_PROFILES="prod"
    ;;
  *)
    echo "开发环境模式"
    export COMPOSE_PROFILES="dev"
    ;;
esac

echo -e "${YELLOW}▶ 创建 Docker 网络...${NC}"
docker network create --driver bridge qr_network || true

echo -e "${YELLOW}▶ 启动 mysql...${NC}"
docker-compose -f mysql/mysql-docker-compose.yaml up -d
wait_for_service "Mysql" 3306

echo -e "${YELLOW}▶ 启动 Nacos 集群...${NC}"
docker-compose -f nacos-docker/nacos/cluster-hostname.yaml up -d
wait_for_service "Nacos" 8848

echo -e "${YELLOW}▶ 启动 Seata...${NC}"
docker-compose -f seata/seata-docker-compose.yml up -d
wait_for_service "Seata" 8091

echo -e "${YELLOW}▶ 启动 Sentinel...${NC}"
docker-compose -f sentinel/sentinel-docker-compose.yml up -d
wait_for_service "Sentinel" 8858

echo -e "${YELLOW}▶ 启动 Redis...${NC}"
docker-compose -f redis/redis-docker-compose.yml up -d
wait_for_service "Redis" 6379

echo -e "${YELLOW}▶ 启动 RocketMQ...${NC}"
docker-compose -f rmq/rmq-docker-compose.yml up -d
wait_for_service "RocketMQ" 9876

echo -e "\n${YELLOW}========== 访问信息 ==========${NC}"
echo -e "${GREEN}Nacos:    http://localhost:8848/nacos (用户名: nacos)"
echo "Sentinel: http://localhost:8858"
echo "Seata: http://localhost:7091/#/login"
echo -e "${YELLOW}==============================${NC}"

trap 'echo -e "${RED}✗ 脚本执行中断，请检查错误！${NC}"; exit 1' INT TERM