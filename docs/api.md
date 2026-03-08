# 接口文档（MVP）

本文档描述 campus-review MVP 的 HTTP API。所有接口返回统一使用 `ApiResponse<T>` 包装。

## 1. 基础信息

### 1.1 端口与入口

- 用户侧网关：`http://localhost:8001`
- 管理侧网关：`http://localhost:8002`

微服务端口（通常不建议直接对外暴露）：

- user-service：8101
- restaurant-service：8102
- review-service：8103
- interaction-service：8104
- ranking-service：8105
- notification-service：8106
- risk-control-service：8107
- admin-service：8108

### 1.2 用户侧网关路由

通过用户侧网关（8001）访问以下路径：

- `/api/auth/**`、`/api/users/**` → user-service（8101）
- `/api/restaurants/**` → restaurant-service（8102）
- `/api/reviews/**` → review-service（8103）
- `/api/interactions/**` → interaction-service（8104）
- `/api/rankings/**` → ranking-service（8105）
- `/api/notifications/**` → notification-service（8106）
- `/api/risk/**` → risk-control-service（8107）

### 1.3 管理侧网关路由

通过管理侧网关（8002）访问：

- `/api/admin/**` → admin-service（8108）

## 2. 认证与请求头

### 2.1 Authorization

需要登录的接口必须携带：

- `Authorization: Bearer <token>`

token 来自登录接口 `POST /api/auth/login`。

### 2.2 网关透传用户信息

用户侧网关校验 token 通过后，会向下游追加：

- `X-User-Id`
- `X-User-Roles`

业务侧以该头作为当前用户上下文来源（例如 `/api/users/me`）。

### 2.3 请求链路追踪

网关会生成并回传请求 ID：

- 请求/响应 Header：`X-Request-Id`

## 3. 统一响应结构

响应体统一为：

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "requestId": null,
  "timestamp": 1730000000000
}
```

- `code=0` 表示成功，其它为错误
- `requestId`：用于排查问题（网关侧也会返回 `X-Request-Id`）

### 3.1 错误码

- 0：OK
- 40000：请求参数错误
- 40100：未登录或登录已过期
- 40300：无权限
- 40400：资源不存在
- 42900：请求过于频繁
- 50000：服务器内部错误

## 4. 用户侧接口（通过 user-gateway:8001）

### 4.1 认证（user-service）

#### 4.1.1 注册

- `POST /api/auth/register`
- Content-Type：`application/json`

Request Body：

```json
{
  "email": "test@campus.edu",
  "studentNo": "20250001",
  "password": "123456",
  "nickname": "test"
}
```

Response：`ApiResponse<UserDTO>`

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "test@campus.edu",
    "studentNo": "20250001",
    "nickname": "test",
    "roles": ["USER"],
    "banned": false,
    "createdAt": "2026-02-21T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1730000000000
}
```

curl 示例（Windows）：

```bash
curl -X POST http://localhost:8001/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@campus.edu\",\"studentNo\":\"20250001\",\"password\":\"123456\",\"nickname\":\"test\"}"
```

#### 4.1.2 登录

- `POST /api/auth/login`
- Content-Type：`application/json`

Request Body：

```json
{
  "account": "test@campus.edu",
  "password": "123456"
}
```

Response：`ApiResponse<LoginResponse>`

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "userId": 1,
    "roles": ["USER"],
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2026-02-21T12:00:00Z"
  },
  "requestId": null,
  "timestamp": 1730000000000
}
```

curl 示例：

```bash
curl -X POST http://localhost:8001/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"account\":\"test@campus.edu\",\"password\":\"123456\"}"
```

### 4.2 用户（user-service）

#### 4.2.1 获取当前登录用户

- `GET /api/users/me`
- Header：`Authorization: Bearer <token>`

Response：`ApiResponse<UserDTO>`

```bash
curl http://localhost:8001/api/users/me ^
  -H "Authorization: Bearer <token>"
```

### 4.3 餐馆（restaurant-service）

#### 4.3.1 创建餐馆

- `POST /api/restaurants`
- Header：`Authorization: Bearer <token>`
- Content-Type：`application/json`

Request Body：

```json
{
  "name": "一食堂",
  "campus": "主校区",
  "address": "A1",
  "description": "快餐",
  "coverImageUrl": ""
}
```

Response：`ApiResponse<RestaurantDTO>`

```bash
curl -X POST http://localhost:8001/api/restaurants ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"name\":\"一食堂\",\"campus\":\"主校区\",\"address\":\"A1\",\"description\":\"快餐\",\"coverImageUrl\":\"\"}"
```

#### 4.3.2 查询餐馆详情

- `GET /api/restaurants/{id}`
- 公开接口（无需登录）

#### 4.3.3 按条件搜索餐馆

- `GET /api/restaurants?name=&campus=`
- Query：
  - `name`（可选）
  - `campus`（可选）
- 公开接口（无需登录）

### 4.4 评价（review-service）

#### 4.4.1 发布评价

- `POST /api/reviews`
- Header：`Authorization: Bearer <token>`
- Content-Type：`application/json`

Request Body：

```json
{
  "restaurantId": 1,
  "rating": 5,
  "content": "好吃"
}
```

Response：`ApiResponse<ReviewDTO>`

```bash
curl -X POST http://localhost:8001/api/reviews ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"restaurantId\":1,\"rating\":5,\"content\":\"好吃\"}"
```

#### 4.4.2 按餐馆查询评价列表

- `GET /api/reviews?restaurantId={id}`
- Header：`Authorization: Bearer <token>`

#### 4.4.3 查询我的评价

- `GET /api/reviews/me`
- Header：`Authorization: Bearer <token>`

### 4.5 互动（interaction-service）

#### 4.5.1 点赞/取消点赞

- `POST /api/interactions/like`
- `POST /api/interactions/unlike`
- Header：`Authorization: Bearer <token>`
- Content-Type：`application/json`

Request Body：

```json
{
  "targetType": "restaurant",
  "targetId": 1
}
```

Response：`ApiResponse<Void>`

#### 4.5.2 收藏/取消收藏

- `POST /api/interactions/favorite`
- `POST /api/interactions/unfavorite`
- Header：`Authorization: Bearer <token>`
- Content-Type：`application/json`

Request Body 同上。

#### 4.5.3 查询互动统计

- `GET /api/interactions/count?targetType=&targetId=`
- Header：`Authorization: Bearer <token>`

Response：`ApiResponse<InteractionCountDTO>`

### 4.6 排行（ranking-service）

#### 4.6.1 热门餐馆榜

- `GET /api/rankings/hot-restaurants?topN=10`
- 公开接口（无需登录）

```bash
curl "http://localhost:8001/api/rankings/hot-restaurants?topN=10"
```

### 4.7 通知（notification-service）

#### 4.7.1 发送消息

- `POST /api/notifications/send`
- Header：`Authorization: Bearer <token>`
- Content-Type：`application/json`

Request Body：

```json
{
  "toUserId": 2,
  "title": "系统通知",
  "content": "你的评价已通过审核"
}
```

Response：`ApiResponse<MessageDTO>`

#### 4.7.2 收件箱

- `GET /api/notifications/inbox`
- Header：`Authorization: Bearer <token>`

#### 4.7.3 标记已读

- `POST /api/notifications/{id}/read`
- Header：`Authorization: Bearer <token>`

## 5. 管理侧接口（通过 admin-gateway:8002）

管理侧接口需要 `ADMIN` 角色 token：

- `Authorization: Bearer <admin-token>`

### 5.1 评价审核（admin-service）

#### 5.1.1 查询待审核评价

- `GET /api/admin/reviews/pending`

#### 5.1.2 通过评价

- `POST /api/admin/reviews/{id}/approve`

#### 5.1.3 拒绝评价

- `POST /api/admin/reviews/{id}/reject`

### 5.2 用户封禁（admin-service）

#### 5.2.1 封禁用户

- `POST /api/admin/users/{id}/ban`

#### 5.2.2 解封用户

- `POST /api/admin/users/{id}/unban`

## 6. 风控接口（risk-control-service，直连 8107）

当前未通过网关暴露，直接访问：

- `http://localhost:8107`

### 6.1 文本审核

- `POST /api/risk/audit`
- Content-Type：`application/json`

Request Body：

```json
{
  "content": "这是一段待审核文本"
}
```

Response：`ApiResponse<RiskAuditResult>`

### 6.2 限流判定

- `GET /api/risk/ratelimit?key=&limit=60&windowSeconds=60`

Response：`ApiResponse<RateLimitResult>`

