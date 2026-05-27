# 校园点评系统 - 前端 API 接口文档

> **生成日期**: 2026-03-30
> **项目**: campus-review-frontend
> **基础 URL**: `http://localhost:8001` (开发环境)

---

## 目录

- [1. 概述](#1-概述)
  - [1.1 统一响应格式](#11-统一响应格式)
  - [1.2 错误码定义](#12-错误码定义)
  - [1.3 认证机制](#13-认证机制)
- [2. 认证模块 (Auth)](#2-认证模块-auth)
- [3. 用户模块 (User)](#3-用户模块-user)
- [4. 餐馆模块 (Restaurant)](#4-餐馆模块-restaurant)
- [5. 评价模块 (Review)](#5-评价模块-review)
- [6. 互动模块 (Interaction)](#6-互动模块-interaction)
- [7. 排行榜模块 (Ranking)](#7-排行榜模块-ranking)
- [8. 通知模块 (Notification)](#8-通知模块-notification)
- [9. 文件模块 (File)](#9-文件模块-file)
- [10. 管理员模块 (Admin)](#10-管理员模块-admin)
- [11. 健康检查模块 (Health)](#11-健康检查模块-health)

---

## 1. 概述

### 1.1 统一响应格式

所有 API 接口均采用统一的响应格式：

```typescript
interface ApiResponse<T> {
  code: number        // 响应码，0 表示成功
  message: string     // 响应消息
  data: T            // 响应数据
  requestId: string | null  // 请求追踪 ID
  timestamp: number  // 响应时间戳
}
```

### 1.2 错误码定义

| 错误码 | 名称 | 说明 |
|--------|------|------|
| 0 | OK | 成功 |
| 40000 | BAD_REQUEST | 请求参数错误 |
| 40100 | UNAUTHORIZED | 未授权或登录已过期 |
| 40300 | FORBIDDEN | 无权限访问 |
| 40400 | NOT_FOUND | 资源不存在 |
| 42900 | TOO_MANY_REQUESTS | 请求过于频繁 |
| 50000 | INTERNAL_ERROR | 服务器内部错误 |

### 1.3 认证机制

- **认证方式**: JWT Bearer Token
- **Token 传递**: 请求头 `Authorization: Bearer <token>`
- **Token 存储**: Cookie (通过 `js-cookie` 库)
- **Token 刷新**: 支持 Refresh Token 自动刷新机制
- **登录失效**: 遇到 401 错误自动跳转到 `/login` 页面

---

## 2. 认证模块 (Auth)

**文件路径**: [src/api/auth.ts](src/api/auth.ts)

### 2.1 用户注册

**接口**: `POST /api/auth/register`

**请求参数**:
```typescript
interface RegisterRequest {
  email: string       // 邮箱地址（需为学校邮箱）
  studentNo: string   // 学号
  password: string    // 密码
  nickname: string    // 昵称
}
```

**响应数据**:
```typescript
interface UserDTO {
  id: number
  email: string
  studentNo: string
  nickname: string
  roles: string[]     // 用户角色列表
  banned: boolean     // 是否被封禁
  createdAt: string   // 创建时间
}
```

**示例**:
```typescript
import { register } from '@/api/auth'

const result = await register({
  email: 'student@example.edu.cn',
  studentNo: '2024001',
  password: 'password123',
  nickname: '美食家'
})
```

---

### 2.2 用户登录

**接口**: `POST /api/auth/login`

**请求参数**:
```typescript
interface LoginRequest {
  account: string   // 账号（邮箱或学号）
  password: string  // 密码
}
```

**响应数据**:
```typescript
interface LoginResponse {
  userId: number
  roles: string[]           // 用户角色列表
  token: string             // JWT Token
  refreshToken: string      // 刷新 Token
  expiresAt: string         // Token 过期时间
  refreshExpiresAt: string  // Refresh Token 过期时间
}
```

**示例**:
```typescript
import { login } from '@/api/auth'

const result = await login({
  account: 'student@example.edu.cn',
  password: 'password123'
})

// 存储 Token
setToken(result.data.token)
setRefreshToken(result.data.refreshToken)
```

---

### 2.3 用户登出

**说明**: 前端本地清除 Token，无需调用后端接口

**示例**:
```typescript
import { removeToken } from '@/utils/storage'

removeToken()  // 清除本地存储的 Token
```

---

## 3. 用户模块 (User)

**文件路径**: [src/api/user.ts](src/api/user.ts)

### 3.1 获取当前用户信息

**接口**: `GET /api/users/me`

**需要认证**: ✅

**响应数据**: `UserDTO`

**示例**:
```typescript
import { getCurrentUser } from '@/api/user'

const result = await getCurrentUser()
console.log(result.data.nickname)
```

---

### 3.2 更新用户资料

**接口**: `PUT /api/users/me`

**需要认证**: ✅

**请求参数**:
```typescript
interface UpdateProfileRequest {
  nickname?: string     // 新昵称
  avatarUrl?: string   // 头像 URL
}
```

**响应数据**: `UserDTO`

**示例**:
```typescript
import { updateProfile } from '@/api/user'

const result = await updateProfile({
  nickname: '新昵称',
  avatarUrl: 'https://example.com/avatar.jpg'
})
```

---

## 4. 餐馆模块 (Restaurant)

**文件路径**: [src/api/restaurant.ts](src/api/restaurant.ts)

### 4.1 创建餐馆

**接口**: `POST /api/restaurants`

**需要认证**: ✅

**请求参数**:
```typescript
interface RestaurantCreateRequest {
  name: string           // 餐馆名称
  campus: string         // 校区
  address: string        // 地址
  description: string    // 描述
  coverImageUrl?: string // 封面图 URL（可选）
}
```

**响应数据**:
```typescript
interface RestaurantDTO {
  id: number
  name: string
  campus: string
  address: string
  description: string
  coverImageUrl: string
  createdAt: string
}
```

**示例**:
```typescript
import { createRestaurant } from '@/api/restaurant'

const result = await createRestaurant({
  name: '美味餐厅',
  campus: '北校区',
  address: '学生食堂 2 楼',
  description: '提供各种美食'
})
```

---

### 4.2 创建餐馆（带图片上传）

**接口**: `POST /api/restaurants/with-image`

**需要认证**: ✅

**Content-Type**: `multipart/form-data`

**请求参数**:
```typescript
{
  name: string
  campus: string
  address?: string
  description?: string
  file?: File  // 图片文件
}
```

**响应数据**: `RestaurantDTO`

**示例**:
```typescript
import { createRestaurantWithImage } from '@/api/restaurant'

const file = document.querySelector('input[type="file"]').files[0]
const result = await createRestaurantWithImage({
  name: '美味餐厅',
  campus: '北校区',
  address: '学生食堂 2 楼',
  description: '提供各种美食',
  file: file
})
```

---

### 4.3 查询餐馆详情

**接口**: `GET /api/restaurants/:id`

**路径参数**:
- `id`: 餐馆 ID

**响应数据**: `RestaurantDTO`

**示例**:
```typescript
import { getRestaurant } from '@/api/restaurant'

const result = await getRestaurant(1)
console.log(result.data.name)
```

---

### 4.4 搜索餐馆

**接口**: `GET /api/restaurants`

**查询参数**:
```typescript
interface RestaurantSearchParams {
  name?: string    // 餐馆名称（模糊搜索）
  campus?: string  // 校区筛选
}
```

**响应数据**: `RestaurantDTO[]`

**示例**:
```typescript
import { searchRestaurants } from '@/api/restaurant'

const result = await searchRestaurants({
  campus: '北校区',
  name: '美味'
})
```

---

### 4.5 批量查询餐馆

**接口**: `GET /api/restaurants/by-ids`

**查询参数**:
- `ids`: 餐馆 ID 列表，逗号分隔（如 `1,2,3`）

**响应数据**: `RestaurantDTO[]`

**示例**:
```typescript
import { getRestaurantsByIds } from '@/api/restaurant'

const result = await getRestaurantsByIds([1, 2, 3])
```

---

## 5. 评价模块 (Review)

**文件路径**: [src/api/review.ts](src/api/review.ts)

### 5.1 发布评价

**接口**: `POST /api/reviews`

**需要认证**: ✅

**请求参数**:
```typescript
interface ReviewCreateRequest {
  restaurantId: number  // 餐馆 ID
  rating: number        // 评分（1-5）
  content: string       // 评价内容
}
```

**响应数据**:
```typescript
interface ReviewDTO {
  id: number
  restaurantId: number
  userId: number
  rating: number
  content: string
  status: string        // 评价状态：PENDING, APPROVED, REJECTED
  createdAt: string
}
```

**评价状态枚举**:
```typescript
enum ReviewStatus {
  PENDING = 'PENDING',      // 待审核
  APPROVED = 'APPROVED',    // 已通过
  REJECTED = 'REJECTED'     // 已驳回
}
```

**示例**:
```typescript
import { createReview } from '@/api/review'

const result = await createReview({
  restaurantId: 1,
  rating: 5,
  content: '非常好吃！'
})
```

---

### 5.2 查询餐馆的评价列表

**接口**: `GET /api/reviews`

**查询参数**:
- `restaurantId`: 餐馆 ID

**响应数据**: `ReviewDTO[]`

**示例**:
```typescript
import { getReviewsByRestaurant } from '@/api/review'

const result = await getReviewsByRestaurant(1)
```

---

### 5.3 查询我的评价

**接口**: `GET /api/reviews/me`

**需要认证**: ✅

**响应数据**: `ReviewDTO[]`

**示例**:
```typescript
import { getMyReviews } from '@/api/review'

const result = await getMyReviews()
```

---

## 6. 互动模块 (Interaction)

**文件路径**: [src/api/interaction.ts](src/api/interaction.ts)

### 6.1 点赞

**接口**: `POST /api/interactions/like`

**需要认证**: ✅

**请求参数**:
```typescript
interface InteractRequest {
  targetType: 'RESTAURANT' | 'REVIEW'  // 目标类型
  targetId: number                     // 目标 ID
}
```

**响应数据**: `void`

**示例**:
```typescript
import { like } from '@/api/interaction'

await like({
  targetType: 'REVIEW',
  targetId: 1
})
```

---

### 6.2 取消点赞

**接口**: `POST /api/interactions/unlike`

**需要认证**: ✅

**请求参数**: `InteractRequest`

**响应数据**: `void`

---

### 6.3 收藏

**接口**: `POST /api/interactions/favorite`

**需要认证**: ✅

**请求参数**: `InteractRequest`

**响应数据**: `void`

**示例**:
```typescript
import { favorite } from '@/api/interaction'

await favorite({
  targetType: 'RESTAURANT',
  targetId: 1
})
```

---

### 6.4 取消收藏

**接口**: `POST /api/interactions/unfavorite`

**需要认证**: ✅

**请求参数**: `InteractRequest`

**响应数据**: `void`

---

### 6.5 查询互动统计

**接口**: `GET /api/interactions/count`

**查询参数**:
- `targetType`: 目标类型（`RESTAURANT` 或 `REVIEW`）
- `targetId`: 目标 ID

**响应数据**:
```typescript
interface InteractionCountDTO {
  targetType: string
  targetId: number
  likeCount: number      // 点赞数
  favoriteCount: number  // 收藏数
}
```

**示例**:
```typescript
import { getInteractionCount } from '@/api/interaction'

const result = await getInteractionCount('REVIEW', 1)
console.log(`点赞数: ${result.data.likeCount}`)
```

---

## 7. 排行榜模块 (Ranking)

**文件路径**: [src/api/ranking.ts](src/api/ranking.ts)

### 7.1 获取热门餐馆排行榜

**接口**: `GET /api/rankings/hot-restaurants`

**查询参数**:
- `topN`: 返回前 N 名（默认 10）

**响应数据**:
```typescript
interface HotRestaurantRankItemDTO {
  rank: number           // 排名
  restaurantId: number   // 餐馆 ID
  restaurantName: string // 餐馆名称
  score: number          // 综合得分
  avgRating: number      // 平均评分
}
```

**示例**:
```typescript
import { getHotRestaurants } from '@/api/ranking'

const result = await getHotRestaurants(10)
result.data.forEach(item => {
  console.log(`第${item.rank}名: ${item.restaurantName}`)
})
```

---

## 8. 通知模块 (Notification)

**文件路径**: [src/api/notification.ts](src/api/notification.ts)

### 8.1 发送消息

**接口**: `POST /api/notifications/send`

**需要认证**: ✅

**请求参数**:
```typescript
interface SendMessageRequest {
  toUserId: number  // 接收用户 ID
  title: string     // 消息标题
  content: string   // 消息内容
}
```

**响应数据**:
```typescript
interface MessageDTO {
  id: number
  toUserId: number
  title: string
  content: string
  read: boolean      // 是否已读
  createdAt: string
}
```

**示例**:
```typescript
import { sendMessage } from '@/api/notification'

const result = await sendMessage({
  toUserId: 2,
  title: '系统通知',
  content: '您的评价已通过审核'
})
```

---

### 8.2 获取收件箱消息列表

**接口**: `GET /api/notifications/inbox`

**需要认证**: ✅

**响应数据**: `MessageDTO[]`

**示例**:
```typescript
import { getInbox } from '@/api/notification'

const result = await getInbox()
result.data.forEach(msg => {
  console.log(`${msg.title} - ${msg.read ? '已读' : '未读'}`)
})
```

---

### 8.3 标记消息为已读

**接口**: `POST /api/notifications/:id/read`

**需要认证**: ✅

**路径参数**:
- `id`: 消息 ID

**响应数据**: `void`

**示例**:
```typescript
import { markAsRead } from '@/api/notification'

await markAsRead(1)
```

---

## 9. 文件模块 (File)

**文件路径**: [src/api/file.ts](src/api/file.ts)

### 9.1 上传文件

**接口**: `POST /api/files/upload`

**需要认证**: ✅

**Content-Type**: `multipart/form-data`

**请求参数**:
- `file`: 文件对象
- `dir`: 存储目录（可选）

**响应数据**:
```typescript
{
  url: string          // 文件访问 URL
  filename: string     // 文件名
  contentType: string  // 文件类型
  size: number         // 文件大小（字节）
}
```

**示例**:
```typescript
import { uploadFile } from '@/api/file'

const file = document.querySelector('input[type="file"]').files[0]
const result = await uploadFile(file, 'avatars')
console.log(`文件 URL: ${result.data.url}`)
```

---

## 10. 管理员模块 (Admin)

**文件路径**: [src/api/admin.ts](src/api/admin.ts)

**基础路径**: `/api/admin`（所有管理员接口都需要管理员权限）

### 10.1 获取待审核评价列表

**接口**: `GET /api/admin/reviews/pending`

**需要认证**: ✅（管理员）

**响应数据**:
```typescript
interface PendingReviewDTO {
  id: number
  restaurantId: number
  userId: number
  rating: number
  content: string
  status: string
  createdAt: string
}
```

**示例**:
```typescript
import { getPendingReviews } from '@/api/admin'

const result = await getPendingReviews()
```

---

### 10.2 通过评价审核

**接口**: `POST /api/admin/reviews/:id/approve`

**需要认证**: ✅（管理员）

**路径参数**:
- `id`: 评价 ID

**响应数据**: `null`

**示例**:
```typescript
import { approveReview } from '@/api/admin'

await approveReview(1)
```

---

### 10.3 驳回评价

**接口**: `POST /api/admin/reviews/:id/reject`

**需要认证**: ✅（管理员）

**路径参数**:
- `id`: 评价 ID

**响应数据**: `null`

**示例**:
```typescript
import { rejectReview } from '@/api/admin'

await rejectReview(1)
```

---

### 10.4 获取用户列表

**接口**: `GET /api/admin/users`

**需要认证**: ✅（管理员）

**响应数据**: `UserDTO[]`

**示例**:
```typescript
import { getUserList } from '@/api/admin'

const result = await getUserList()
```

---

### 10.5 封禁用户

**接口**: `POST /api/admin/users/:id/ban`

**需要认证**: ✅（管理员）

**路径参数**:
- `id`: 用户 ID

**响应数据**: `null`

**示例**:
```typescript
import { banUser } from '@/api/admin'

await banUser(1)
```

---

### 10.6 解封用户

**接口**: `POST /api/admin/users/:id/unban`

**需要认证**: ✅（管理员）

**路径参数**:
- `id`: 用户 ID

**响应数据**: `null`

**示例**:
```typescript
import { unbanUser } from '@/api/admin'

await unbanUser(1)
```

---

### 10.7 获取餐厅列表

**接口**: `GET /api/admin/restaurants`

**需要认证**: ✅（管理员）

**响应数据**: `RestaurantDTO[]`

**示例**:
```typescript
import { getAdminRestaurants } from '@/api/admin'

const result = await getAdminRestaurants()
```

---

### 10.8 删除餐厅

**接口**: `DELETE /api/admin/restaurants/:id`

**需要认证**: ✅（管理员）

**路径参数**:
- `id`: 餐厅 ID

**响应数据**: `null`

**示例**:
```typescript
import { deleteRestaurant } from '@/api/admin'

await deleteRestaurant(1)
```

---

## 11. 健康检查模块 (Health)

**文件路径**: [src/api/health.ts](src/api/health.ts)

### 11.1 健康检查

**接口**: `GET /api/health/`

**需要认证**: ❌

**响应数据**:
```typescript
interface HealthCheckResponse {
  status: string      // 服务状态
  service: string     // 服务名称
  timestamp: string   // 时间戳
}
```

**示例**:
```typescript
import { healthCheck } from '@/api/health'

const result = await healthCheck()
console.log(`服务状态: ${result.status}`)
```

---

### 11.2 就绪检查

**接口**: `GET /api/health/ready`

**需要认证**: ❌

**响应数据**:
```typescript
interface ReadyCheckResponse {
  status: string               // 服务状态
  service: string              // 服务名称
  checks: Record<string, string>  // 各项检查结果
  timestamp: string            // 时间戳
}
```

**示例**:
```typescript
import { readyCheck } from '@/api/health'

const result = await readyCheck()
console.log(`数据库状态: ${result.checks.database}`)
```

---

## 附录

### A. 请求封装方法

项目提供了统一的请求封装方法（位于 [src/api/request.ts](src/api/request.ts)）：

```typescript
// GET 请求
export function get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>

// POST 请求
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>>

// PUT 请求
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>>

// DELETE 请求
export function del<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
```

### B. 错误处理

所有 API 请求都会经过统一的错误处理：

1. **业务错误**（`code !== 0`）：
   - `40100`: 自动跳转登录页
   - `40300`: 提示无权限
   - `42900`: 提示请求频繁
   - 其他：显示错误消息

2. **HTTP 错误**：
   - `401`: 尝试刷新 Token，失败则跳转登录
   - `400/403/404/500/503`: 显示对应错误提示
   - 网络错误：提示检查网络连接

### C. Token 刷新机制

当请求返回 `401` 错误时：

1. 检查是否有 Refresh Token
2. 如果正在刷新，将请求加入队列
3. 使用 Refresh Token 获取新的 Access Token
4. 重试队列中的所有请求
5. 如果刷新失败，清除 Token 并跳转登录页

### D. 类型定义文件

所有类型定义位于 `src/types/` 目录：

| 文件 | 说明 |
|------|------|
| [api.ts](src/types/api.ts) | API 统一响应格式、错误码 |
| [auth.ts](src/types/auth.ts) | 认证相关类型 |
| [user.ts](src/types/user.ts) | 用户相关类型 |
| [restaurant.ts](src/types/restaurant.ts) | 餐馆相关类型 |
| [review.ts](src/types/review.ts) | 评价相关类型 |
| [interaction.ts](src/types/interaction.ts) | 互动相关类型 |
| [notification.ts](src/types/notification.ts) | 通知相关类型 |
| [ranking.ts](src/types/ranking.ts) | 排行榜相关类型 |
| [admin.ts](src/types/admin.ts) | 管理员相关类型 |
| [health.ts](src/types/health.ts) | 健康检查相关类型 |

---

**文档版本**: 1.0.0
**最后更新**: 2026-03-30
