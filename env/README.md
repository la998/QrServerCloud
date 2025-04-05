### 基础设施启动顺序

* 命令文件权限设置
```shell
chmod +x {stop,start,clean,common}.sh
chmod -R 777 ./data/
```

```shell
./start.sh                #启动服务，启动所有服务，创建并启动所有容器、网络和卷。如果镜像不存在，会自动拉取或构建。
./stop.sh                 #暂停服务，停止容器但保留容器和数据，以便后续恢复。
./stop.sh && ./start.sh   #重启服务，调用stop.sh和start.sh。
./clean.sh                #清理容器和数据（带选项）,clean.sh支持-v（删除卷）和-f（强制删除数据文件）
./clean.sh -vf            #完全清理（删除卷和数据文件）
./clean.sh -vf && ./start.sh  #重新部署，一键部署
```

* 创建名为 qr_network 的桥接网络（持久化存储配置）
```PowerShell
  docker network create --driver bridge qr_network
```
* 启动mysql
```PowerShell
    docker-compose -f mysql/mysql-docker-compose.yaml up -d
```
* 启动nacos集群
```PowerShell
    docker-compose -f nacos-docker/nacos/cluster-hostname.yaml up -d
```
浏览器打开：http://localhost:8848/nacos
使用配置的用户名密码登录（示例中为 nacos/nacos123456）
* 启动seata
```PowerShell
    docker-compose -f seata/seata-docker-compose.yml up -d
```
浏览器打开：http://localhost:7091/
使用配置的用户名密码登录（示例中为 seata/seata）
* 启动sentinel
```PowerShell
    docker-compose -f sentinel/sentinel-docker-compose.yml up -d
```
浏览器打开：http://localhost:8858 
使用配置的用户名密码登录（示例中为 sentinel/sentinel123456）

* 启动Redis
```PowerShell
    docker-compose -f redis/redis-docker-compose.yml up -d

```

* 启动rmq
```PowerShell
    docker-compose -f rmq/rmq-docker-compose.yml up -d
```

### 部署中间件
```shell
  #运行以下命令构建镜像（注意最后的 .）:构建镜像用于初始化seata配置自动写入nacos中
  #避免每次部署容器init-config都需要重新下载
  docker build -f Dockerfile.init-config -t init-seata-to-nacos-config:latest .
  docker images | grep init-seata-to-nacos-config     #验证镜像是否生成
  docker rmi -f init-seata-to-nacos-config:latest .    #删除镜像
 

  docker --version          # 检查 Docker 版本
  docker-compose --version  # 检查 Docker Compose 版本
  docker-compose down  # 清理旧容器
  docker-compose up -d     #在根目录执行，启动中间件部署（Docker-compose）
  docker-compose down && docker-compose up -d  #重新部署所有服务
  
  docker-compose down -v  # 删除所有关联卷
  rm -rf ./mysql/data/*  # 清空 MySQL 数据目录
  rm -rf ./redis/data/*  # 清空 redis 数据目录
  docker-compose up -d  #启动生成容器
  
  rm -rf ./mysql/data/* ./redis/data/*
  docker-compose down -v && docker-compose up -d
  
  docker-compose down --volumes  # 清理旧配置
  docker-compose down redis && docker-compose up -d redis  #重新部署 Redis 服务
  docker-compose down mysql && docker-compose up -d mysql  #重新部署 mysql 服务
  
  docker ps  # 查看所有运行中的容器
  docker-compose ps  # 查看当前项目的容器状态

  docker-compose restart  # 重启所有容器
  docker-compose restart mysql  # 仅重启 MySQL
  docker-compose start  #在 docker-compose.yml 所在目录执行以下命令，启动所有已创建但停止的容器
  docker-compose start <服务名>
  docker start mysql  # 启动 MySQL 容器
  docker start redis  # 启动 Redis 容器
  
  docker-compose config  #运行以下命令检查.env变量是否被正确替换到docker-compose.yml
 
```
### Redis
```shell
  # 1. 检查容器网络连通性
  docker network inspect qr_network 
  # 2. 检查 Redis 日志
  docker logs redis
  # 3. 手动测试连接
  docker exec -it gateway-service curl redis:6379
  
  docker exec -it redis redis-cli -a redis123456 --no-auth-warning
  127.0.0.1:6379> info server
  # 应看到 Redis 版本信息
  
  #检查密码是否生效
  # 进入 Redis CLI 并认证
  docker exec -it redis redis-cli -a redis123456
  # 执行命令验证
  127.0.0.1:6379> PING
  # 应返回 "PONG"
  127.0.0.1:6379> ACL LIST
  # 应显示用户信息
  
```
###问题：Redis 内存不足
```shell
  # 查看内存使用
  docker exec -it redis redis-cli -a redis@2024 --no-auth-warning info memory
  # 解决方案：调整 maxmemory 配置
  vim redis/conf/redis.conf
  # 修改 maxmemory 1024mb
  docker-compose restart redis
```
* 如果仍有问题，请运行以下命令查看详细日志：
```shell
  docker-compose logs [服务名]  # 如 docker-compose logs seata
  docker logs -f nacos  #检查 Nacos 日志
  docker logs -f sentinel  #
  docker logs -f mysql  #
   
  docker pull nacos/nacos-server:v2.5.1
  docker pull bladex/sentinel-dashboard:1.8.6
  docker apache/rocketmq:5.1.4
  docker seataio/seata-server:2.0.0
  
  lsof -i :8080  # Linux/Mac 确认主机端口 8080 未被占用
  docker ps | grep sentinel  #确认 Docker 容器正确映射端口：
  docker logs -f mysql | grep "Initializing database"  #检查 MySQL 初始化状态：
  docker exec -it nacos bash
  curl -X GET 'http://localhost:8848/nacos/v1/ns/health/instance' #验证 Nacos 连接， 应返回健康状态
  
  chmod -R 755 ./mysql/init  # 确保脚本可读
  
  docker exec -it seata sh  #验证 Seata 连接
  telnet mysql 3306  # 应能连通


  # 下载 Nacos 官方 SQL 文件
 curl https://github.com/alibaba/nacos/blob/master/distribution/conf/mysql-schema.sql -O ./mysql/init/nacos-mysql.sql
  
  # 下载 Seata 官方 SQL 文件
  curl https://github.com/seata/seata/blob/develop/script/server/db/mysql.sql -O ./mysql/init/seata-mysql.sql
```

