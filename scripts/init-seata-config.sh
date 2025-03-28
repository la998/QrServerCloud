#!/bin/bash
set -eo pipefail

# 等待服务就绪函数
wait_for_service() {
  local host=$1 port=$2
  echo "Waiting for $host:$port..."
  while ! nc -z $host $port; do
    sleep 1
  done
}

# 主逻辑
main() {
  # 等待Nacos就绪
  wait_for_service qr-nacos 8848

  # 生成配置内容
  config_content=$(cat <<EOF
service.vgroupMapping.default_tx_group=default
store.mode=db
store.db.datasource=seata
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://qr-mysql:3306/seata?useSSL=false
store.db.user=seata
store.db.password=${SEATA_DB_PASSWORD}
EOF
  )

  # 推送配置到Nacos
  curl -X POST "http://qr-nacos:8848/nacos/v1/cs/configs" \
    -d "dataId=seataServer.properties&group=SEATA_GROUP&content=$config_content" \
    -u "nacos:${NACOS_AUTH_PASSWORD}"

  echo "Configuration pushed successfully"
}

main "$@"