#### 常用命令
```shell
  ./gradlew :auth-service:clean
  ./gradlew :auth-service:compileKotlin
```
### 整体架构
```text
此项目是一个基于 Spring Cloud Gateway 的网关服务。
运用了 Nacos 进行服务发现，Sentinel 做流量控制，JWT 来鉴权，同时还配置了跨域支持。
```
- 配置文件 application.yml
   + 服务器端口：设定服务器端口为 8081。
   + JWT 密钥：从配置文件里读取 JWT 密钥。
   + 网关配置：借助 Nacos 开展服务发现，并且定义了路由规则。
   + Sentinel 配置：开启 Sentinel 流量控制，同时指定控制台地址。
   + Redis 配置：对 Redis 连接信息和连接池参数进行配置。
- JwtConfig.kt
   + SecretKey Bean：把配置文件中的密钥字符串转换为 SecretKey 对象。
   + JwtParser Bean：创建 JwtParser 并设置签名密钥。