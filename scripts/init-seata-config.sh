#!/bin/bash
set -eo pipefail

# 等待服务就绪函数
wait_for_nacos_user() {
  echo "Waiting for Nacos user initialization..."
  until curl -s -u "nacos:nacos123456" "http://qr-nacos:8848/nacos/v1/auth/users?username=nacos" | grep -q "nacos"; do
    sleep 2
  done
}

# 主逻辑
main() {
  # 等待 Nacos 端口开放
  while ! nc -z qr-nacos 8848; do
    sleep 2
  done

  # 等待用户数据就绪
  wait_for_nacos_user

  # 生成配置内容
  config_content=$(cat <<EOF
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://qr-mysql:3306/seata?useSSL=false
store.db.user=seata
store.db.password=${SEATA_DB_PASSWORD}
EOF
  )

# 推送事务组映射配置
curl -X POST -H "${NACOS_AUTH_IDENTITY_KEY}: ${NACOS_AUTH_IDENTITY_VALUE}" \
  "http://qr-nacos:8848/nacos/v1/cs/configs" \
  -d "dataId=service.vgroupMapping.default_tx_group&group=SEATA_GROUP&content=default" \
  -u "nacos:${NACOS_AUTH_PASSWORD}"

# 推送Seata服务端配置
curl -X POST -H "${NACOS_AUTH_IDENTITY_KEY}: ${NACOS_AUTH_IDENTITY_VALUE}" \
  "http://qr-nacos:8848/nacos/v1/cs/configs" \
  -d "dataId=seataServer.properties&group=SEATA_GROUP&content=$config_content" \
  -u "nacos:${NACOS_AUTH_PASSWORD}"

  echo "Configuration pushed successfully"
}
main "$@"