# restaurant-service API 接口文档

**服务端口**: 8102

**生成时间**: 2026-03-29 15:08:20

---

## AdminRestaurant

### GET `/api/admin/restaurants`

**getRestaurantList**

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### DELETE `/api/admin/restaurants/{id}`

**delete**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | query | Long | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/admin/restaurants`

**createRestaurant**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | RestaurantCreateRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## BasicError

### GET `/error`

**error**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/error`

**errorHtml**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |
| response | query | HttpServletResponse | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## FileUpload

### POST `/api/files/upload`

**upload**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| file | query | MultipartFile | 是 | - |
| dir | query | String | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## OpenApiWebMvcResource

### GET `/v3/api-docs.yaml`

**openapiYaml**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |
| apiDocsUrl | query | String | 是 | - |
| locale | query | Locale | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/v3/api-docs`

**openapiJson**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |
| apiDocsUrl | query | String | 是 | - |
| locale | query | Locale | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## Ranking

### GET `/api/rankings/hot-restaurants`

**hotRestaurants**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| topN | query | int | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## Restaurant

### GET `/api/restaurants`

**search**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| name | query | String | 是 | - |
| campus | query | String | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/api/restaurants/by-ids`

**getByIds**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| ids | query | String | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/restaurants/with-image`

**createWithImage**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| name | query | String | 是 | - |
| campus | query | String | 是 | - |
| address | query | String | 是 | - |
| description | query | String | 是 | - |
| coverImage | query | MultipartFile | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/api/restaurants/{id}`

**getById**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | query | Long | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/restaurants`

**create**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | RestaurantCreateRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## SwaggerConfigResource

### GET `/v3/api-docs/swagger-config`

**openapiJson**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

## SwaggerWelcomeWebMvc

### GET `/swagger-ui.html`

**redirectToUi**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | HttpServletRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

