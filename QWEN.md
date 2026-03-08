# Campus Review 项目上下文

## 项目概述

**Campus Review** 是一个基于 Spring Boot 3.x 的校园美食点评平台微服务项目。该项目采用多模块 Maven 聚合结构，提供完整的用户注册登录、餐馆管理、评价发布、互动点赞、排行榜、通知消息和风控审核等功能。

### 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| Java | 17 |
| 构建工具 | Maven (多模块聚合) |
| 框架 | Spring Boot 3.5.11 |
| 网关 | Spring Cloud Gateway (Reactive) |
| 注册/配置中心 | Nacos (本地默认关闭) |
| ORM | MyBatis-Plus 3.5.5 |
| 认证 | JWT (io.jsonwebtoken:jjwt 0.12.6) |
| 数据库 | H2 内存库 (本地开发) / 支持 MySQL |
| 缓存 | Redis |
| Lombok | 启用 |

### 包名约定

所有代码统一使用 `com.vapor.*` 包名。

---

## 模块结构

```
campus-review/
├── campus-review-common       # 通用基础设施
│   ├── api/                   # 统一响应体 ApiResponse
│   ├── error/                 # 错误码 BizException, ErrorCode
│   └── web/                   # 全局异常处理、请求链路 ID、用户上下文
├── campus-review-model        # 跨服务 DTO/请求响应模型
│   └── com.vapor.model/       # auth, user, restaurant, review, interaction, ranking, notification, risk
├── campus-review-utils        # 工具组件
│   └── com.vapor.utils/       # JWT 工具、校验器等
├── campus-review-feign-api    # Feign 客户端接口 (预留)
├── campus-review-service      # 微服务聚合模块
│   ├── user-service           # 用户服务 (8101)
│   ├── restaurant-service     # 餐馆服务 (8102)
│   ├── review-service         # 评价服务 (8103)
│   ├── interaction-service    # 互动服务 (8104)
│   ├── ranking-service        # 排行榜服务 (8105)
│   ├── notification-service   # 通知服务 (8106)
│   ├── risk-control-service   # 风控服务 (8107)
│   └── admin-service          # 后台管理服务 (8108)
├── campus-review-gateway      # 网关聚合模块
│   ├── campus-review-user-gateway   # 用户侧网关 (8001)
│   └── campus-review-admin-gateway  # 管理侧网关 (8002)
├── campus-review-basic        # 基础模块 (预留)
├── campus-review-test         # 测试模块 (预留)
└── docs/                      # 项目文档
    ├── api.md                 # 接口文档
    ├── quick-start.md         # 快速启动指南
    └── tech-solution.md       # 技术方案文档
```

---

## 端口与服务映射

### 微服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| user-service | 8101 | 用户认证、信息管理 |
| restaurant-service | 8102 | 餐馆信息管理 |
| review-service | 8103 | 评价管理 |
| interaction-service | 8104 | 点赞/收藏互动 |
| ranking-service | 8105 | 排行榜计算 |
| notification-service | 8106 | 消息通知 |
| risk-control-service | 8107 | 风控审核 |
| admin-service | 8108 | 后台管理 |

### 网关端口

| 网关 | 端口 | 路由说明 |
|------|------|----------|
| user-gateway | 8001 | 用户侧入口，路由到各业务服务 |
| admin-gateway | 8002 | 管理侧入口，路由到 admin-service |

---

## 构建与运行

### 构建命令

```bash
# 全量构建
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 构建单个模块 (例如 user-service)
mvn -pl campus-review-service/user-service -am clean install
```

### 运行方式

**推荐方式：** 在 IDE 中直接运行各模块的 `*Application` 启动类。

**Maven 方式：**

```bash
# 运行单个服务
mvn -pl campus-review-service/user-service -am spring-boot:run

# 运行用户网关
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run

# 运行管理网关
mvn -pl campus-review-gateway/campus-review-admin-gateway -am spring-boot:run
```

### 启动顺序 (推荐)

1. 先启动核心服务：`user-service` → `restaurant-service` → `review-service` → `interaction-service`
2. 再启动依赖服务：`notification-service` → `risk-control-service` → `ranking-service` → `admin-service`
3. 最后启动网关：`user-gateway` → `admin-gateway`

---

## 关键配置说明

### 本地开发配置

本地默认关闭 Nacos，使用 H2 内存数据库：

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: false
      discovery:
        enabled: false
    compatibility:
      verifier:
        enabled: false
```

### JWT 密钥配置

以下服务的 `security.jwt.secret` 必须保持一致：

- user-service
- user-gateway
- admin-gateway

### 数据库配置

本地开发使用 H2 内存库，各服务启动时自动初始化 `schema.sql`。

---

## 开发约定

### 统一响应体

所有 Controller 对外返回统一使用 `ApiResponse<T>` 包装：

```java
// 成功响应
ApiResponse.ok(data);

// 失败响应
ApiResponse.fail(errorCode, message);
```

### 错误码规范

| Code | 说明 |
|------|------|
| 0 | OK |
| 40000 | 请求参数错误 |
| 40100 | 未登录或登录已过期 |
| 40300 | 无权限 |
| 40400 | 资源不存在 |
| 42900 | 请求过于频繁 |
| 50000 | 服务器内部错误 |

### 异常处理

- 业务异常优先复用 `BizException` + `ErrorCode`
- 全局异常处理在 `campus-review-common` 的 `GlobalExceptionHandler`

### DTO 放置规范

- 所有跨服务 DTO、请求/响应对象放在 `campus-review-model` 模块
- 避免在服务模块之间相互依赖实体类

### 网关路由规则

**用户侧网关 (8001) 路由：**

| 路径 | 目标服务 |
|------|----------|
| `/api/auth/**` | user-service |
| `/api/users/**` | user-service |
| `/api/restaurants/**` | restaurant-service |
| `/api/reviews/**` | review-service |
| `/api/interactions/**` | interaction-service |
| `/api/rankings/**` | ranking-service |
| `/api/notifications/**` | notification-service |

**管理侧网关 (8002) 路由：**

| 路径 | 目标服务 |
|------|----------|
| `/api/admin/**` | admin-service |

### 请求头约定

| Header | 说明 |
|--------|------|
| `Authorization: Bearer <token>` | JWT 认证令牌 |
| `X-User-Id` | 网关透传的用户 ID |
| `X-User-Roles` | 网关透传的用户角色 |
| `X-Request-Id` | 请求链路追踪 ID |

---

## 测试

```bash
# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
mvn -pl campus-review-service/review-service -am test
mvn -pl campus-review-service/risk-control-service -am test
```

---

## 安全注意事项

1. **不要提交敏感信息**：不要把真实密钥、Token、个人信息写入代码或提交到仓库
2. **本地开发密钥**：仅使用开发用的 `security.jwt.secret`
3. **日志安全**：不要在日志中打印 JWT、密码等敏感字段
4. **端口变更**：需要更换端口或路由时，先改 `application.yml`，再更新文档与用例

---

## 文档索引

| 文档 | 说明 |
|------|------|
| [docs/quick-start.md](docs/quick-start.md) | 快速启动指南，包含 API 示例 |
| [docs/api.md](docs/api.md) | 完整接口文档 |
| [docs/tech-solution.md](docs/tech-solution.md) | 技术方案与微服务拆分说明 |
| [AGENTS.md](AGENTS.md) | 面向自动化代码助手的协作说明 |

---

## 快速 API 验证示例

```bash
# 1. 注册
curl -X POST http://localhost:8001/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@campus.edu\",\"studentNo\":\"20250001\",\"password\":\"123456\",\"nickname\":\"test\"}"

# 2. 登录
curl -X POST http://localhost:8001/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"account\":\"test@campus.edu\",\"password\":\"123456\"}"

# 3. 查询热门餐馆榜
curl http://localhost:8001/api/rankings/hot-restaurants?topN=10
```
