#!/bin/bash
set -exo pipefail

# 等待服务就绪函数
wait_for_nacos_user() {
  echo "Waiting for Nacos user initialization..."
  until curl -s -u "nacos:nacos123456" "http://nacos:8848/nacos/v1/auth/users?username=nacos" | grep -q "nacos"; do
    sleep 2
  done
}
wait_for_nacos() {
  until nc -z nacos 8848; do
    echo "Waiting for Nacos to start..."
    sleep 2
  done

  until curl -s "http://nacos:8848/nacos/" | grep -q "Nacos"; do
    echo "Waiting for Nacos API..."
    sleep 2
  done
}

# 主逻辑
main() {
  # 等待 Nacos 端口开放
  wait_for_nacos

  # 等待用户数据就绪
  wait_for_nacos_user

  # 生成配置内容
  config_content=$(cat <<EOF
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://mysql:3306/seata?useUnicode=true%26characterEncoding=utf8%26connectTimeout=1000%26socketTimeout=3000%26autoReconnect=true%26useSSL=false
store.db.user=seata
store.db.password=seata123456
store.db.minConn=1
store.db.maxConn=20
store.db.maxWait=5000
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.lockTable=lock_table
store.db.queryLimit=100
EOF
  )

# 推送事务组映射配置
curl -X POST -H "${NACOS_AUTH_IDENTITY_KEY}: ${NACOS_AUTH_IDENTITY_VALUE}" \
  "http://nacos:8848/nacos/v1/cs/configs?namespace=public" \
  -d "dataId=service.vgroupMapping.default_tx_group&group=SEATA_GROUP&content=default" \
  -u "nacos:${NACOS_AUTH_PASSWORD}"

# 推送Seata服务端配置
curl -X POST -H "${NACOS_AUTH_IDENTITY_KEY}: ${NACOS_AUTH_IDENTITY_VALUE}" \
  "http://nacos:8848/nacos/v1/cs/configs?namespace=public" \
  -d "dataId=seataServer.properties&group=SEATA_GROUP&content=$config_content" \
  -u "nacos:${NACOS_AUTH_PASSWORD}"

  echo "Configuration pushed successfully"
  echo "Seata 配置已推送至 Nacos"

  # 创建健康检查标志文件（容器内路径）
  echo "Creating health check flag..."
  touch /app/init-complete.flag
}
main "$@"