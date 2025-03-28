#!/bin/bash
# redeploy.sh - 一键重新部署脚本
set -eo pipefail

# 获取脚本所在目录
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

# 定义颜色代码
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # 重置颜色

# 清理函数
cleanup() {
  echo -e "${YELLOW}▶ 正在停止并删除所有容器...${NC}"
  docker-compose down -v

  echo -e "${YELLOW}▶ 清理数据目录...${NC}"
  [ -d "./data/mysql/" ] && sudo rm -rf ./data/mysql/*
  [ -d "./data/redis/" ] && sudo rm -rf ./data/redis/*

  echo -e "${GREEN}✓ 清理完成${NC}"
}

# 部署函数
deploy() {
  echo -e "${YELLOW}▶ 启动服务...${NC}"
  docker-compose up -d

  echo -e "${YELLOW}▶ 等待初始化完成...${NC}"
  docker logs -f init-seata-config &
  tail_pid=$!

  # 等待初始化容器退出
  while docker ps | grep -q init-seata-config; do
    sleep 5
  done

  kill $tail_pid 2>/dev/null

  echo -e "${GREEN}✓ 部署完成${NC}"

  wait_for_service "MySQL" 3306
  wait_for_service "Nacos" 8848

  # 显示访问信息
  echo -e "\n${YELLOW}访问信息："
  echo "Nacos:    http://localhost:8848/nacos (用户名: nacos)"
  echo "Sentinel: http://localhost:8080"
  echo "Seata:    http://localhost:8091"
  echo -e "${NC}"
}
wait_for_service() {
  local service=$1
  local port=$2
  echo -n "等待 $service 就绪..."
  while ! nc -z localhost $port; do
    sleep 1
    echo -n "."
  done
  echo -e " ${GREEN}✓${NC}"
}
# 执行主流程
main() {
  case "$1" in
    prod)
      echo "生产环境模式"
      export COMPOSE_FILE="docker-compose.prod.yml"
      ;;
    *)
      echo "开发环境模式"
      export COMPOSE_FILE="docker-compose.yml"
      ;;
  esac

  cleanup
  deploy
}

# 异常处理
trap 'echo -e "${RED}✗ 脚本执行中断，请检查错误！${NC}"; exit 1' INT TERM

# 执行主函数
main