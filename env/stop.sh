#!/bin/bash
# stop.sh - 停止所有服务

source "$(dirname "$0")/common.sh"

SCRIPT_DIR=$(get_script_dir)
cd "$SCRIPT_DIR/" || exit 1

check_docker

show_header "停止服务"
for file in "${COMPOSE_FILES[@]}"; do
    if [[ -f $file ]]; then
        show $YELLOW "▷ 停止 ${file##*/}"
        docker-compose -f "$file" stop
    fi
done