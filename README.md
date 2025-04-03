## 基于Spring Cloud Alibaba 的二维码服务端

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

### 模块
  * gate-service 网关服务
  * auth-service 认证服务
  * 用户服务
  * 二维码服务
  * 标签服务
 

### 常用编译命令
```shell
  ./gradlew clean build  #清理并重新构建项目
  ./gradlew clean   # 清理项目构建缓存
  ./gradlew build --refresh-dependencies # 强制刷新依赖并重新下载
  ./gradlew bootRun --args='--debug'  #启用调试模式获取详细日志

  rm -rf ~/.gradle/caches/  # 可选：清理全局 Gradle 缓存（谨慎操作）
  ./gradlew :gateway:build
  ./gradlew clean build -x test #完成开发后，使用命令打包
  
  #重新编译
  ./gradlew :gateway:compileKotlin  
  ./gradlew :auth-service:compileKotlin

  #批量创建目录
  mkdir -p {mysql/data,mysql/conf,mysql/init,redis/data,redis/conf}
  
  # 生成安全密钥
  openssl rand -base64 32
```

