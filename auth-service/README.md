### 常用命令
```shell
  ./gradlew :auth-service:clean
  ./gradlew :auth-service:compileKotlin
```

###测试接口示例

```shell
# 设备登录
curl -X POST http://localhost:8081/api/auth/device-login \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-device-001"}'

# 用户名登录
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Token刷新
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your_refresh_token"}'
```

### 设备登陆流程
```sequenceDiagram
 用户->>+AuthService: 设备ID登录
 AuthService->>UserRepository: 查询设备用户
 alt 用户存在
     UserRepository-->>AuthService: 返回用户
 else 用户不存在
     AuthService->>UserRepository: 创建新用户
     UserRepository-->>AuthService: 新用户
 end
 AuthService->>JwtTokenProvider: 生成双Token
 AuthService->>Redis: 存储RefreshToken
 AuthService-->>-用户: 返回TokenResponse
```

### 整体架构
```text
该项目是一个基于 Spring Boot 和 Spring Security 的认证服务。
使用 JWT（JSON Web Token）进行身份验证和授权。
主要功能包括用户登录、令牌刷新、用户登出等，同时对各种异常情况进行了统一处理。
```

* 实体类（Entity）
  + User：表示用户实体，实现了UserDetails接口，用于 Spring Security 的用户认证。
* 服务类（Service）
  + AuthService：处理用户登录、令牌刷新、用户登出等业务逻辑。
  + TokenBlacklistService：将令牌加入黑名单，使已注销的令牌失效。
* 安全配置类（Config）
  + SecurityConfig：配置 Spring Security 的过滤器链和认证管理器，使用 JWT 进行身份验证。
  + JwtConfig：配置 JWT 的密钥、过期时间等参数。
  + JWT 处理类
  + JwtTokenProvider：生成、验证 JWT 令牌，解析令牌中的用户信息和权限。
* 异常处理类
  + GlobalExceptionHandler：统一处理各种异常，返回标准化的错误响应。
### 关键流程
* 用户登录
  + 用户发送登录请求，包含用户名和密码或设备 ID。
  + AuthService验证用户信息，生成访问令牌和刷新令牌。
  + 将刷新令牌存储到 Redis 中。
* 令牌刷新
  + 用户发送刷新令牌请求。
  + AuthService验证刷新令牌的有效性，生成新的访问令牌和刷新令牌。
  + 使旧的刷新令牌失效。
* 用户登出
  + 用户发送登出请求，包含当前访问令牌。
  + AuthService将访问令牌和刷新令牌加入黑名单。