#!/bin/bash
# clean.sh - 清理容器和数据（带选项）

source "$(dirname "$0")/common.sh"

SCRIPT_DIR=$(get_script_dir)
cd "$SCRIPT_DIR/" || exit 1

VOLUME_FLAG=""
FORCE_CLEAN=false

while getopts "vf" opt; do
    case $opt in
        v) VOLUME_FLAG="--volumes" ;;
        f) FORCE_CLEAN=true ;;
        *) exit 1 ;;
    esac
done

show_header "清理环境"
for file in "${COMPOSE_FILES[@]}"; do
    if [[ -f $file ]]; then
        show $YELLOW "▷ 清理 ${file##*/}"
        docker-compose -f "$file" down $VOLUME_FLAG --remove-orphans --timeout 30
    fi
done

if $FORCE_CLEAN; then
    show $YELLOW "▶ 清理数据文件"
    sudo rm -rfv data/*
fi

# 清理网络
if docker network inspect qr_network &>/dev/null; then
    docker network rm qr_network
fi