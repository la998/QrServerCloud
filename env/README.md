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

### docker 常用命令
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
  
  docker-compose -f rmq/rmq-docker-compose.yml down #删除 rmq 服务
  docker-compose -f rmq/rmq-docker-compose.yml up -d #重新部署 rmq 服务
  

  
  docker-compose config  #运行以下命令检查.env变量是否被正确替换到docker-compose.yml
  
  #docker 常用命令
  docker stats [容器ID或名称]  # 查看指定容器
  docker stats               # 查看所有容器
  docker stats seata --no-stream --format "{{.Name}}: {{.MemUsage}}"    # 查看容器内存使用
  docker exec seata jcmd 1 VM.native_memory summary   # 查看JVM内存状态
  
  docker stats nacos1 --no-stream --format "{{.Name}}: {{.MemUsage}}" # 查看容器内存使用
  docker exec nacos1 jstat -gcutil 1 1s 5 # 查看JVM内存状态

  docker inspect <容器ID> --format='{{.HostConfig.Memory}}'  # 查看内存限制
  docker inspect <容器ID> | grep -i Memory                   # 提取内存相关字段
  docker ps          # 查看所有运行中的容器
  docker-compose ps  # 查看当前项目的容器状态
 
```
### Redis
```shell
  
  docker network inspect qr_network  # 1. 检查容器网络连通性
  docker logs redis # 2. 检查 Redis 日志
  docker exec -it gateway-service curl redis:6379  # 3. 手动测试连接
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

```shell

  # 下载 Nacos 官方 SQL 文件
 curl https://github.com/alibaba/nacos/blob/master/distribution/conf/mysql-schema.sql -O ./mysql/init/nacos-mysql.sql
  
  # 下载 Seata 官方 SQL 文件
  curl https://github.com/seata/seata/blob/develop/script/server/db/mysql.sql -O ./mysql/init/seata-mysql.sql
```

