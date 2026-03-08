# 快速启动（MVP）

## 端口

- user-service: 8101
- restaurant-service: 8102
- review-service: 8103
- interaction-service: 8104
- ranking-service: 8105
- notification-service: 8106
- risk-control-service: 8107
- admin-service: 8108
- user-gateway: 8001
- admin-gateway: 8002

## 本地运行说明

- 默认使用各服务内置的 H2 内存数据库（启动时自动建表/或关闭初始化）。
- JWT 密钥需要保持一致：
  - [user-service application.yml](file:///e:/_CODE_/campus-review/campus-review-service/user-service/src/main/resources/application.yml)
  - [user-gateway application.yml](file:///e:/_CODE_/campus-review/campus-review-gateway/campus-review-user-gateway/src/main/resources/application.yml)
  - [admin-gateway application.yml](file:///e:/_CODE_/campus-review/campus-review-gateway/campus-review-admin-gateway/src/main/resources/application.yml)

## 启动顺序（建议）

1. 启动 user-service / restaurant-service / review-service / interaction-service
2. 启动 notification-service / risk-control-service / ranking-service / admin-service
3. 启动 user-gateway / admin-gateway

## 关键 API 示例（通过 user-gateway:8001）

### 1) 注册

```bash
curl -X POST http://localhost:8001/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@campus.edu\",\"studentNo\":\"20250001\",\"password\":\"123456\",\"nickname\":\"test\"}"
```

### 2) 登录（获取 token）

```bash
curl -X POST http://localhost:8001/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"account\":\"test@campus.edu\",\"password\":\"123456\"}"
```

### 3) 新增餐馆（需要 Authorization: Bearer）

```bash
curl -X POST http://localhost:8001/api/restaurants ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"name\":\"一食堂\",\"campus\":\"主校区\",\"address\":\"A1\",\"description\":\"快餐\",\"coverImageUrl\":\"\"}"
```

### 4) 发布评价（默认进入 PENDING，需要后台审批后对外可见）

```bash
curl -X POST http://localhost:8001/api/reviews ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"restaurantId\":1,\"rating\":5,\"content\":\"好吃\"}"
```

### 5) 点赞

```bash
curl -X POST http://localhost:8001/api/interactions/like ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"targetType\":\"restaurant\",\"targetId\":1}"
```

### 6) 查询热门餐馆榜（公开）

```bash
curl http://localhost:8001/api/rankings/hot-restaurants
```

## 后台操作（通过 admin-gateway:8002）

后台接口要求 token 中包含 `ADMIN` 角色；本地可通过直接修改 `users.roles` 字段为 `ADMIN` 来模拟管理员。

```bash
curl http://localhost:8002/api/admin/reviews/pending ^
  -H "Authorization: Bearer <admin-token>"
```

