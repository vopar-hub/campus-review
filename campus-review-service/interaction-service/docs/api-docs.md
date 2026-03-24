# OpenAPI definition

**版本**: v0

**服务端口**: 8104

**生成时间**: 2026-03-21 19:21:45

---

## 服务器

- http://localhost:8104

---

## 互动管理

### POST `/api/interactions/unlike`

**取消点赞**

取消对目标对象的点赞（幂等操作）

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/InteractRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/interactions/unfavorite`

**取消收藏**

取消对目标对象的收藏（幂等操作）

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/InteractRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/interactions/like`

**点赞**

点赞目标对象（幂等操作）

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/InteractRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/interactions/favorite`

**收藏**

收藏目标对象（幂等操作）

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/InteractRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### GET `/api/interactions/count`

**查询互动计数**

查询目标对象的点赞数和收藏数

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| targetType | query | string | 是 | - |
| targetId | query | integer | 是 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

