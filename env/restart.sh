#!/bin/bash
# restart.sh - 重启所有服务
set -eo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

./stop.sh
./start.sh "$@"