# Campus Review 项目上下文

## 项目概述

**Campus Review** 是一个基于 Spring Boot 3.x 的微服务校园美食点评平台。该项目采用多模块 Maven 聚合结构，提供完整的用户注册登录、餐馆管理、评价发布、互动点赞、排行榜、通知消息和风控审核等功能。

### 技术栈

| 技术           | 版本/说明                             |
|--------------|-----------------------------------|
| Java         | 17                                |
| 构建工具         | Maven (多模块聚合)                     |
| 框架           | Spring Boot 3.2.12 / 3.5.11       |
| Spring Cloud | 2023.0.5                          |
| 网关           | Spring Cloud Gateway (Reactive)   |
| 注册/配置中心      | Nacos (可选，本地默认关闭)                 |
| ORM          | MyBatis-Plus 3.5.5                |
| 认证           | JWT (io.jsonwebtoken:jjwt 0.12.6) |
| 数据库          | H2 内存库 (本地开发) / MySQL 8.0 (生产)    |
| 缓存           | Redis 7.x                         |
| 数据库迁移        | Flyway 9.22.3                     |
| 监控           | Spring Boot Actuator + Prometheus |
| Lombok       | 启用                                |

### 模块结构

```
campus-review/
├── campus-review-common       # 通用基础设施
│   ├── api/                   # 统一响应体 ApiResponse
│   ├── error/                 # 错误码 ErrorCode
│   ├── enums/                 # 枚举类型
│   ├── util/                  # 工具类
│   ├── validation/            # 校验器
│   └── web/                   # 全局异常处理、请求链路 ID、用户上下文
├── campus-review-model        # 跨服务 DTO/请求响应模型
│   └── com.vapor.model/       # auth, user, restaurant, review, interaction, ranking, notification, risk
├── campus-review-utils        # 工具组件
│   └── com.vapor.utils/       # JWT 工具、校验器等
├── campus-review-service      # 微服务聚合模块
│   ├── user-service           # 用户服务 (8101)
│   ├── restaurant-service     # 餐馆服务 (8102)
│   ├── review-service         # 评价服务 (8103)
│   ├── interaction-service    # 互动服务 (8104)
│   ├── ranking-service        # 排行榜服务 (8105)
│   ├── notification-service   # 通知服务 (8106)
│   └── admin-service          # 后台管理服务 (8108)
├── campus-review-gateway      # 网关聚合模块
│   ├── campus-review-user-gateway   # 用户侧网关 (8001)
│   └── campus-review-admin-gateway  # 管理侧网关 (8002)
└── docs/                      # 项目文档
    ├── api.md                 # 接口文档
    ├── quick-start.md         # 快速启动指南
    ├── tech-solution.md       # 技术方案文档
    ├── deployment.md          # 部署指南
    ├── performance.md         # 性能优化指南
    ├── security.md            # 安全配置指南
    ├── logging.md             # 日志配置指南
    ├── flyway.md              # Flyway 迁移指南
    └── service-discovery.md   # 服务发现配置
```

---

## 端口与服务映射

### 微服务端口

| 服务                   | 端口   | 说明        |
|----------------------|------|-----------|
| user-service         | 8101 | 用户认证、信息管理 |
| restaurant-service   | 8102 | 餐馆信息管理    |
| review-service       | 8103 | 评价管理      |
| interaction-service  | 8104 | 点赞/收藏互动   |
| ranking-service      | 8105 | 排行榜计算     |
| notification-service | 8106 | 消息通知      |
| admin-service        | 8108 | 后台管理      |

### 网关端口

| 网关            | 端口   | 路由说明                    |
|---------------|------|-------------------------|
| user-gateway  | 8001 | 用户侧入口，路由到各业务服务          |
| admin-gateway | 8002 | 管理侧入口，路由到 admin-service |

---

## 构建与运行

### 环境要求

- JDK 17+
- Maven 3.8+
- Git
- (可选) Redis 7.x - 用于分布式缓存和限流
- (可选) Nacos 2.x - 用于服务发现（本地开发默认关闭）

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

1. **核心服务**：user-service → restaurant-service → review-service → interaction-service
2. **依赖服务**：notification-service → ranking-service → admin-service
3. **网关**：user-gateway → admin-gateway

### Docker 部署

```bash
# 使用 docker-compose 启动所有服务（包括 Nacos、Redis、MySQL）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down
```

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

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-to-32-chars-min}
    ttl-seconds: 86400
```

### 数据库配置

- **本地开发**：使用 H2 内存库，各服务启动时自动初始化 `schema.sql`
- **生产环境**：配置 MySQL + Flyway 数据库迁移

### Redis 配置（可选）

用于分布式缓存和限流：

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
```

---

## 开发约定

### 包名约定

所有代码统一使用 `com.vapor.*` 包名。

### 统一响应体

所有 Controller 对外返回统一使用 `ApiResponse<T>` 包装：

```java
// 成功响应
ApiResponse.ok(data);

// 失败响应
ApiResponse.fail(errorCode, message);
```

响应结构：

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "requestId": null,
  "timestamp": 1730000000000
}
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

### 代码规范

- **类名**：大驼峰命名（如 `UserService`）
- **方法名**：小驼峰命名（如 `getUserById`）
- **常量**：全大写下划线分隔（如 `MAX_RETRY_COUNT`）
- **包名**：全小写（如 `com.vapor.user.service`）
- **缩进**：4 个空格
- **注释**：类/方法/字段必须添加 JavaDoc 注释

### 提交规范

提交消息格式：`<type>: <subject>`

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档更新 |
| style | 代码风格调整 |
| refactor | 重构 |
| test | 测试相关 |
| chore | 构建/工具链相关 |

---

## 测试

```bash
# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
mvn -pl campus-review-service/review-service -am test
```

---

## API 文档

### Swagger UI

每个服务都集成了 SpringDoc/Swagger，启动后可访问：

| 服务 | Swagger UI |
|------|-----------|
| 用户服务 | http://localhost:8101/swagger-ui.html |
| 餐馆服务 | http://localhost:8102/swagger-ui.html |
| 评价服务 | http://localhost:8103/swagger-ui.html |
| 互动服务 | http://localhost:8104/swagger-ui.html |
| 排行榜服务 | http://localhost:8105/swagger-ui.html |
| 通知服务 | http://localhost:8106/swagger-ui.html |
| 风控服务 | http://localhost:8107/swagger-ui.html |
| 管理服务 | http://localhost:8108/swagger-ui.html |

### 快速 API 验证示例

```bash
# 1. 注册
curl -X POST http://localhost:8001/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@campus.edu\",\"studentNo\":\"20250001\",\"password\":\"Test123\",\"nickname\":\"test\"}"

# 2. 登录
curl -X POST http://localhost:8001/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"account\":\"test@campus.edu\",\"password\":\"Test123\"}"

# 3. 查询热门餐馆榜
curl http://localhost:8001/api/rankings/hot-restaurants?topN=10
```

---

## 监控与可观测性

### Actuator 端点

每个服务都提供了 Actuator 监控端点：

| 端点 | 说明 | 示例 |
|------|------|------|
| health | 健康检查 | http://localhost:8101/actuator/health |
| info | 应用信息 | http://localhost:8101/actuator/info |
| metrics | 性能指标 | http://localhost:8101/actuator/metrics |
| prometheus | Prometheus 格式指标 | http://localhost:8101/actuator/prometheus |

### 健康检查探针

```bash
# 存活探针（Liveness Probe）
curl http://localhost:8101/actuator/health/liveness

# 就绪探针（Readiness Probe）
curl http://localhost:8101/actuator/health/readiness

# 业务健康检查
curl http://localhost:8101/api/health
```

---

## 安全注意事项

1. **不要提交敏感信息**：不要把真实密钥、Token、个人信息写入代码或提交到仓库
2. **本地开发密钥**：仅使用开发用的 `security.jwt.secret`
3. **日志安全**：不要在日志中打印 JWT、密码等敏感字段
4. **端口变更**：需要更换端口或路由时，先改 `application.yml`，再更新文档与用例
5. **密码强度**：密码至少 6 位且包含字母和数字

---

## 项目特性

- **事务管理**: 所有数据库写操作已添加 `@Transactional` 注解
- **请求追踪**: 每个请求自动生成 requestId，支持全链路日志追踪
- **全局异常处理**: 统一异常处理与响应格式
- **API 文档**: 所有控制器已集成 OpenAPI 注解
- **健康检查**: 提供 `/api/health` 和 `/api/health/ready` 端点
- **数据库优化**: 关键查询已添加复合索引
- **Redis 缓存**: 排行榜接口使用 Redis 缓存（5 分钟 TTL）
- **分布式限流**: 基于 Redis Lua 脚本的滑动窗口限流算法
- **密码安全**: 密码强度校验（至少 6 位且包含字母和数字）
- **应用监控**: 集成 Spring Boot Actuator 和 Prometheus 指标
- **容器化**: 支持 Docker 部署（10 个 Dockerfile）
- **CI/CD**: GitHub Actions 自动构建和测试
- **安全防护**: CORS、安全响应头、JWT 鉴权、敏感词过滤
- **日志管理**: 结构化日志、敏感信息脱敏、异步日志
- **服务发现**: Nacos 服务注册与发现（支持负载均衡）
- **数据库迁移**: Flyway 版本管理和自动迁移

---

## 后续优化方向

1. **链路追踪**: 集成 SkyWalking 或 Zipkin 实现分布式追踪
2. **消息队列**: 引入 Kafka/RabbitMQ 实现异步解耦
3. **弹性 resilience**: 集成 Resilience4j 实现熔断降级
4. **API 版本管理**: 添加 URI 版本前缀支持
5. **GraphQL 支持**: 为复杂查询场景提供 GraphQL API

---

## 文档索引

| 文档 | 说明 |
|------|------|
| [docs/quick-start.md](docs/quick-start.md) | 快速启动指南，包含 API 示例 |
| [docs/api.md](docs/api.md) | 完整接口文档 |
| [docs/tech-solution.md](docs/tech-solution.md) | 技术方案与微服务拆分说明 |
| [docs/deployment.md](docs/deployment.md) | 部署指南 |
| [docs/performance.md](docs/performance.md) | 性能优化指南 |
| [docs/security.md](docs/security.md) | 安全配置指南 |
| [docs/logging.md](docs/logging.md) | 日志配置指南 |
| [docs/flyway.md](docs/flyway.md) | Flyway 迁移指南 |
| [docs/service-discovery.md](docs/service-discovery.md) | 服务发现配置 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 贡献指南 |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 |
| [AGENTS.md](AGENTS.md) | 代码助手协作说明 |
