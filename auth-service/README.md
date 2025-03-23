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