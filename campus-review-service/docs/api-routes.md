# review-service API 接口文档

**服务端口**: 8103

**生成时间**: 2026-04-06 11:00:36

---

## BasicError

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

## Interaction

### POST `/api/interactions/unfavorite`

**unfavorite**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | InteractRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/api/interactions/count`

**count**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| targetType | query | String | 是 | - |
| targetId | query | Long | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/interactions/favorite`

**favorite**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | InteractRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/interactions/like`

**like**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | InteractRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### POST `/api/interactions/unlike`

**unlike**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | InteractRequest | 是 | - |

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

## Review

### POST `/api/reviews`

**create**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| request | query | ReviewCreateRequest | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/api/reviews`

**listByRestaurant**

**请求参数**:

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| restaurantId | query | Long | 是 | - |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

---

### GET `/api/reviews/me`

**myReviews**

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

