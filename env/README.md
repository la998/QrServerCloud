### 项目启动顺序

* 一键启动
```shell
# 开发环境
  ./redeploy.sh

# 生产环境
  ./redeploy.sh prod
```

* 1.创建名为 qr_network 的桥接网络（持久化存储配置）
```PowerShell
  docker network create --driver bridge qr_network
```
* 2.启动mysql和nacos集群
```PowerShell
    docker-compose -f nacos-docker/nacos/cluster-hostname.yaml up
```
* 3.启动Redis
```PowerShell
    docker-compose -f redis/redis-docker-compose.yml up

```
* 4.启动sentinel
```PowerShell
    docker-compose -f sentinel/sentinel-docker-compose.yml up
```
浏览器打开：http://localhost:8858
使用配置的用户名密码登录（示例中为 sentinel/sentinel123456）

* 5.启动sentinel
```PowerShell
    docker-compose -f seata/seata-docker-compose.yml up
```
* 6.启动rmq
```PowerShell
    docker-compose -f rmq/rmq-docker-compose.yml up
```
