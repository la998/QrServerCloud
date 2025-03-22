### 架构示意
```angular2html
                      [用户端]
                         │
                         ▼
                  [Spring Cloud Gateway]
                         │
           ┌────────────┼──────────────┐
           ▼            ▼             ▼
    [JWT鉴权]    [Sentinel流控]    [路由转发]
           │            │             │
           └────────────┴─────────────┘
                         │
               ┌─────────┴─────────┐
               ▼                   ▼
       [用户服务]              [二维码服务]──────────┐
               │                   │              │
       [MySQL]◄┘               ┌───┴───┐          │
                           [MySQL]  [OSS]         │
                               │                  │
                               ▼                  ▼
                         [Redis缓存]        [RocketMQ]
                                         │        │
                                         ▼        ▼
                                   [清理服务]  [统计服务]

```
### 工具
* 开发工具
  * Spring Boot 3.2.4 
  * Spring Cloud 2023.0.1 +
  * Spring Cloud Alibaba 2023.0.1.0
  * Sentinel Version 1.8.6 
  * Nacos Version 2.3.2 （）
  * RocketMQ Version 5.1.4 
  * Seata Version 2.0.0 （ https://seata.apache.org/zh-cn/ ）
    		

### 常用编译命令
```shell
./gradlew clean build  #清理并重新构建项目
./gradlew clean   # 清理项目构建缓存
./gradlew build --refresh-dependencies # 强制刷新依赖并重新下载
./gradlew bootRun --args='--debug'  #启用调试模式获取详细日志

# 可选：清理全局 Gradle 缓存（谨慎操作）
rm -rf ~/.gradle/caches/
./gradlew :gateway:compileKotlin  ##重新编译
./gradlew :gateway:build
./gradlew clean build -x test #完成网关开发后，使用命令打包
```
### 部署中间件
```shell

  docker --version          # 检查 Docker 版本
  docker-compose --version  # 检查 Docker Compose 版本
  docker-compose down  # 清理旧容器
  docker-compose up -d     #在根目录执行，启动中间件部署（Docker-compose）
  docker-compose down && docker-compose up -d  #重启所有服务
  docker-compose down -v  # 删除所有关联卷
  rm -rf ./mysql/data/*  # 清空 MySQL 数据目录
  docker-compose up -d  #启动
 
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

* Nacos访问：
    * http://localhost:8848/nacos (默认账号nacos/nacos)

    * 创建命名空间：DEV

    * 导入初始配置文件（示例配置）

* Sentinel访问：
  * http://localhost:8080 (账号sentinel/sentinel@2024)