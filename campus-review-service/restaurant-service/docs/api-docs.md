# OpenAPI definition

**版本**: v0

**服务端口**: 8102

**生成时间**: 2026-03-29 15:08:20

---

## 服务器

- http://localhost:8102

---

## 排行榜

### GET `/api/rankings/hot-restaurants`

**热门餐馆排行榜**

查询热门餐馆排行榜 Top N

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| topN | query | integer | 否 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

## 文件管理

### POST `/api/files/upload`

**上传文件**

上传文件到 MinIO 并返回访问 URL

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| dir | query | string | 否 | - |

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "required" : [ "file" ],
  "type" : "object",
  "properties" : {
    "file" : {
      "type" : "string",
      "format" : "binary"
    }
  }
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

## 餐厅管理

### GET `/api/admin/restaurants`

**获取餐厅列表**

获取所有餐厅列表

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/admin/restaurants`

**创建餐厅**

添加新的餐厅信息

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/RestaurantCreateRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### DELETE `/api/admin/restaurants/{id}`

**删除餐厅**

删除指定餐厅

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | path | integer | 是 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

## 餐馆管理

### GET `/api/restaurants`

**搜索餐馆**

根据名称和校区搜索餐馆

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| name | query | string | 否 | - |
| campus | query | string | 否 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/restaurants`

**创建餐馆**

创建新的餐馆记录（JSON 格式）

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "$ref" : "#/components/schemas/RestaurantCreateRequest"
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### POST `/api/restaurants/with-image`

**创建餐馆（带图片上传）**

创建新的餐馆记录并同时上传封面图片

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| name | query | string | 是 | - |
| campus | query | string | 是 | - |
| address | query | string | 否 | - |
| description | query | string | 否 | - |

**请求体**:

Content-Type: `application/json`

Schema:
```json
{
  "type" : "object",
  "properties" : {
    "coverImage" : {
      "type" : "string",
      "format" : "binary"
    }
  }
}
```

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### GET `/api/restaurants/{id}`

**查询餐馆**

根据 ID 查询餐馆详细信息

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | path | integer | 是 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

### GET `/api/restaurants/by-ids`

**批量查询餐馆**

根据 ID 列表批量查询餐馆信息

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| ids | query | string | 是 | - |

**响应**:

| 状态码 | 说明 |
|--------|------|
| 200 | OK |

---

