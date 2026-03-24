# ranking-service API 接口文档

**服务端口**: 8105

**生成时间**: 2026-03-24 21:38:43

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

