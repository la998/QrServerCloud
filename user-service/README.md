### 常用命令
```shell
  ./gradlew :user-service:clean
  ./gradlew :user-service:compileKotlin
  ./gradlew :user-service:build
```

### 测试用户服务接口
```shell
  # 创建用户（需要管理员权限）
curl -X POST http://localhost:8083/api/user/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{"username":"admin","password":"Admin@123"}'

  # 获取当前用户信息
curl -X GET http://localhost:8083/api/user/v1/users/me \
  -H "Authorization: Bearer your_jwt_token"

  # 搜索用户
curl -X GET "http://localhost:8083/api/user/v1/users?keyword=admin" \
  -H "Authorization: Bearer your_jwt_token"
  
  #创建权限
curl -X POST http://localhost:8083/api/user/v1/permissions \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "user:read",
    "name": "查看用户"
  }'
  
  #创建角色并分配权限
  # 创建角色
curl -X POST http://localhost:8083/api/user/v1/roles \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ROLE_ADMIN",
    "name": "系统管理员"
  }'

# 分配权限
curl -X POST http://localhost:8083/api/user/v1/roles/ROLE_ADMIN/permissions \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '["user:read", "user:write"]'
```