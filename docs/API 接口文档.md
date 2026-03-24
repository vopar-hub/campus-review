# Campus Review 平台 API 接口文档

## 概述

本文档描述了 Campus Review 校园评论平台的所有 REST API 接口，供前端开发和测试使用。

### 基础信息

- **基础路径**: 各服务独立部署，通过网关统一访问
- **数据格式**: JSON
- **字符编码**: UTF-8

### 统一响应格式

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "requestId": "xxx-xxx-xxx",
  "timestamp": 1234567890000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，200 表示成功 |
| message | string | 响应消息 |
| data | object | 响应数据主体 |
| requestId | string | 请求追踪 ID |
| timestamp | long | 时间戳（毫秒） |

### 认证方式

需要登录的接口需要在 HTTP Header 中携带 JWT Token：

```
Authorization: Bearer <your_jwt_token>
```

---

## 1. 用户认证模块

**基础路径**: `/api/auth`

### 1.1 用户注册

- **接口**: `POST /api/auth/register`
- **权限**: 公开（无需登录）

**请求体**:
```json
{
  "email": "student@example.edu.cn",
  "studentNo": "2024001",
  "password": "password123",
  "nickname": "小明"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 是 | 邮箱地址（需有效格式） |
| studentNo | string | 是 | 学号 |
| password | string | 是 | 密码（6-50 字符） |
| nickname | string | 是 | 昵称（1-50 字符） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "email": "student@example.edu.cn",
    "studentNo": "2024001",
    "nickname": "小明",
    "roles": ["USER"],
    "banned": false,
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 1.2 用户登录

- **接口**: `POST /api/auth/login`
- **权限**: 公开（无需登录）

**请求体**:
```json
{
  "account": "student@example.edu.cn",
  "password": "password123"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 登录账号（邮箱或学号） |
| password | string | 是 | 密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "roles": ["USER"],
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2024-01-02T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 2. 用户信息模块

**基础路径**: `/api/users`

### 2.1 获取当前用户信息

- **接口**: `GET /api/users/me`
- **权限**: 需要登录

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "email": "student@example.edu.cn",
    "studentNo": "2024001",
    "nickname": "小明",
    "roles": ["USER"],
    "banned": false,
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 3. 餐馆管理模块

**基础路径**: `/api/restaurants`

### 3.1 创建餐馆

- **接口**: `POST /api/restaurants`
- **权限**: 需要登录（管理员）

**请求体**:
```json
{
  "name": "第一食堂",
  "campus": "南湖校区",
  "address": "校园北区 1 号楼",
  "description": "提供各式家常菜",
  "coverImageUrl": "https://example.com/image.jpg"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 餐馆名称 |
| campus | string | 是 | 校区 |
| address | string | 否 | 详细地址 |
| description | string | 否 | 描述信息 |
| coverImageUrl | string | 否 | 封面图 URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "第一食堂",
    "campus": "南湖校区",
    "address": "校园北区 1 号楼",
    "description": "提供各式家常菜",
    "coverImageUrl": "https://example.com/image.jpg",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 3.2 创建餐馆（带图片上传）

- **接口**: `POST /api/restaurants/with-image`
- **权限**: 需要登录（管理员）
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 餐馆名称 |
| campus | string | 是 | 校区 |
| address | string | 否 | 详细地址 |
| description | string | 否 | 描述信息 |
| coverImage | file | 否 | 封面图片文件 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "第一食堂",
    "campus": "南湖校区",
    "address": "校园北区 1 号楼",
    "description": "提供各式家常菜",
    "coverImageUrl": "http://localhost:9000/campus-review/restaurants/2026/03/22/abc123.jpg",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 3.3 查询餐馆详情

- **接口**: `GET /api/restaurants/{id}`
- **权限**: 公开

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 餐馆 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "第一食堂",
    "campus": "南湖校区",
    "address": "校园北区 1 号楼",
    "description": "提供各式家常菜",
    "coverImageUrl": "https://example.com/image.jpg",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 3.4 搜索餐馆

- **接口**: `GET /api/restaurants`
- **权限**: 公开

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 餐馆名称（模糊匹配） |
| campus | string | 否 | 校区（精确匹配） |

**请求示例**: `GET /api/restaurants?name=食堂&campus=南湖校区`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "第一食堂",
      "campus": "南湖校区",
      "address": "校园北区 1 号楼",
      "description": "提供各式家常菜",
      "coverImageUrl": "https://example.com/image.jpg",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 4. 评价管理模块

**基础路径**: `/api/reviews`

### 4.1 发布评价

- **接口**: `POST /api/reviews`
- **权限**: 需要登录

**请求体**:
```json
{
  "restaurantId": 1,
  "rating": 5,
  "content": "非常好吃，服务也很好！"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| restaurantId | long | 是 | 餐馆 ID |
| rating | int | 是 | 评分（1-5） |
| content | string | 是 | 评价内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "restaurantId": 1,
    "userId": 1,
    "rating": 5,
    "content": "非常好吃，服务也很好！",
    "status": "PENDING",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 4.2 查询餐馆评价列表

- **接口**: `GET /api/reviews?restaurantId={id}`
- **权限**: 公开

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| restaurantId | long | 是 | 餐馆 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "restaurantId": 1,
      "userId": 1,
      "rating": 5,
      "content": "非常好吃，服务也很好！",
      "status": "APPROVED",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 4.3 查询我的评价

- **接口**: `GET /api/reviews/me`
- **权限**: 需要登录

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "restaurantId": 1,
      "userId": 1,
      "rating": 5,
      "content": "非常好吃，服务也很好！",
      "status": "APPROVED",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 5. 互动管理模块

**基础路径**: `/api/interactions`

### 5.1 点赞

- **接口**: `POST /api/interactions/like`
- **权限**: 需要登录

**请求体**:
```json
{
  "targetType": "REVIEW",
  "targetId": 1
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型（RESTAURANT/REVIEW） |
| targetId | long | 是 | 目标 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 5.2 取消点赞

- **接口**: `POST /api/interactions/unlike`
- **权限**: 需要登录

**请求体**: 同点赞接口

---

### 5.3 收藏

- **接口**: `POST /api/interactions/favorite`
- **权限**: 需要登录

**请求体**: 同点赞接口

---

### 5.4 取消收藏

- **接口**: `POST /api/interactions/unfavorite`
- **权限**: 需要登录

**请求体**: 同点赞接口

---

### 5.5 查询互动计数

- **接口**: `GET /api/interactions/count?targetType={type}&targetId={id}`
- **权限**: 公开

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型（RESTAURANT/REVIEW） |
| targetId | long | 是 | 目标 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "targetType": "REVIEW",
    "targetId": 1,
    "likeCount": 10,
    "favoriteCount": 5
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 6. 排行榜模块

**基础路径**: `/api/rankings`

### 6.1 热门餐馆排行榜

- **接口**: `GET /api/rankings/hot-restaurants?topN={n}`
- **权限**: 公开

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| topN | int | 否 | 10 | 返回前 N 名 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "rank": 1,
      "restaurantId": 1,
      "restaurantName": "第一食堂",
      "score": 98.5
    },
    {
      "rank": 2,
      "restaurantId": 2,
      "restaurantName": "第二食堂",
      "score": 95.0
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 7. 通知管理模块

**基础路径**: `/api/notifications`

### 7.1 投递消息

- **接口**: `POST /api/notifications/send`
- **权限**: 需要登录（管理员）

**请求体**:
```json
{
  "toUserId": 1,
  "title": "审核通知",
  "content": "您的评价已通过审核"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| toUserId | long | 是 | 接收用户 ID |
| title | string | 是 | 消息标题 |
| content | string | 是 | 消息内容 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "toUserId": 1,
    "title": "审核通知",
    "content": "您的评价已通过审核",
    "read": false,
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 7.2 查询收件箱

- **接口**: `GET /api/notifications/inbox`
- **权限**: 需要登录

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "toUserId": 1,
      "title": "审核通知",
      "content": "您的评价已通过审核",
      "read": false,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 7.3 标记消息已读

- **接口**: `POST /api/notifications/{id}/read`
- **权限**: 需要登录

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 消息 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 8. 后台管理模块

**基础路径**: `/api/admin`

### 8.1 待审核评价列表

- **接口**: `GET /api/admin/reviews/pending`
- **权限**: 需要登录（管理员）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "restaurantId": 1,
      "userId": 1,
      "rating": 5,
      "content": "非常好吃，服务也很好！",
      "status": "PENDING",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 8.2 通过评价

- **接口**: `POST /api/admin/reviews/{id}/approve`
- **权限**: 需要登录（管理员）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 评价 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 8.3 驳回评价

- **接口**: `POST /api/admin/reviews/{id}/reject`
- **权限**: 需要登录（管理员）

**路径参数**: 同通过评价

---

### 8.4 封禁用户

- **接口**: `POST /api/admin/users/{id}/ban`
- **权限**: 需要登录（管理员）

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户 ID |

---

### 8.5 解封用户

- **接口**: `POST /api/admin/users/{id}/unban`
- **权限**: 需要登录（管理员）

**路径参数**: 同封禁用户

---

## 9. 风控模块

**基础路径**: `/api/risk`

### 9.1 内容审核

- **接口**: `POST /api/risk/audit`
- **权限**: 内部服务调用

**请求体**:
```json
{
  "content": "这是一段待审核的文本"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "allowed": true,
    "reason": null
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

### 9.2 限流判定

- **接口**: `GET /api/risk/ratelimit?key={key}&limit={limit}&windowSeconds={window}`
- **权限**: 内部服务调用

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| key | string | 是 | - | 限流键（用户 ID/IP 等） |
| limit | long | 否 | 60 | 窗口内最大请求数 |
| windowSeconds | long | 否 | 60 | 窗口大小（秒） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "allowed": true,
    "remaining": 59,
    "resetAtEpochMillis": 1234567890000
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 10. 健康检查模块

**基础路径**: `/api/health`

### 10.1 健康检查

- **接口**: `GET /api/health`
- **权限**: 公开

**响应示例**:
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T00:00:00Z",
  "service": "user-service"
}
```

---

### 10.2 就绪检查

- **接口**: `GET /api/health/ready`
- **权限**: 公开

**响应示例**:
```json
{
  "ready": true,
  "timestamp": "2024-01-01T00:00:00Z"
}
```

---

## 11. 文件管理模块

**基础路径**: `/api/files`

### 11.1 上传文件

- **接口**: `POST /api/files/upload`
- **权限**: 需要登录
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| file | file | 是 | - | 要上传的文件 |
| dir | string | 否 | uploads | 存储目录（如：restaurants, avatars） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "http://localhost:9000/campus-review/uploads/2026/03/22/abc123.jpg",
    "filename": "image.jpg",
    "contentType": "image/jpeg",
    "size": "102400"
  },
  "requestId": null,
  "timestamp": 1234567890000
}
```

---

## 附录：状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/认证失败 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 附录：枚举值说明

### 评价状态 (status)
| 值 | 说明 |
|----|------|
| PENDING | 待审核 |
| APPROVED | 已通过 |
| REJECTED | 已驳回 |

### 目标类型 (targetType)
| 值 | 说明 |
|----|------|
| RESTAURANT | 餐馆 |
| REVIEW | 评价 |

### 用户角色 (roles)
| 值 | 说明 |
|----|------|
| USER | 普通用户 |
| ADMIN | 管理员 |
