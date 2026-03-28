# Campus Review - 校园美食点评平台

基于 Spring Boot 3.x 的微服务校园美食点评系统，支持餐馆展示、评价审核、互动点赞、热门榜单等功能。

## 技术栈

| 技术           | 版本       | 说明          |
|--------------|----------|-------------|
| Java         | 17       | 开发语言        |
| Spring Boot  | 3.5.11   | 应用框架        |
| Spring Cloud | 2023.0.5 | 微服务框架       |
| MyBatis-Plus | 3.5.5    | ORM 框架      |
| H2           | 1.4.x    | 内存数据库（开发）   |
| Redis        | 7.x      | 分布式缓存和限流    |
| JWT          | 0.12.6   | 身份认证        |
| Nacos        | 2.x      | 注册/配置中心（可选） |
| Actuator     | 3.5.11   | 应用监控        |
| Prometheus   | 2.x      | 指标收集        |

## 模块结构

```
campus-review/
├── campus-review-common       # 通用基础设施
├── campus-review-model        # 跨服务 DTO/模型
├── campus-review-utils        # 工具组件（JWT 等）
├── campus-review-service      # 微服务聚合（3 个核心服务）
│   ├── user-service           # 用户服务 (8101) - 包含通知功能
│   ├── restaurant-service     # 餐馆服务 (8102) - 包含排行榜功能
│   └── review-service         # 评价服务 (8103) - 包含互动功能
├── campus-review-gateway      # 网关聚合
│   ├── campus-review-user-gateway   # 用户网关 (8001)
│   └── campus-review-admin-gateway  # 管理网关 (8002)
└── docs                       # 项目文档
```

### 微服务架构演进

**v2.0+ 架构（当前）** - 从 8 个微服务优化为 3 个核心服务：

| 原服务 | 状态 | 合并目标 |
|--------|------|----------|
| admin-service | ✅ 已移除 | 前端直接调用各服务 /api/admin/* 接口 |
| interaction-service | ✅ 已合并 | review-service |
| ranking-service | ✅ 已合并 | restaurant-service |
| notification-service | ✅ 已合并 | user-service |

**架构优化收益：**
- 降低运维复杂度（8 服务 → 3 服务）
- 减少网络调用开销（本地调用替代 Feign）
- 简化部署配置（Nacos 配置减少 5 份）
- 保持功能完整性和接口兼容性

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Git

### 编译构建

```bash
# 全量构建
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests
```

### 启动服务

```bash
# 启动用户服务
mvn -pl campus-review-service/user-service -am spring-boot:run

# 启动用户网关
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run
```

### 启动顺序（推荐）

1. 核心服务：user-service → restaurant-service → review-service
2. 网关：user-gateway → admin-gateway

### 数据库初始化

服务启动时会自动执行 Flyway 迁移脚本，创建以下数据库：

| 服务 | 数据库 | 主要表 |
|------|--------|--------|
| user-service | campus_user | users, messages |
| restaurant-service | campus_restaurant | restaurants, hot_restaurant_rank |
| review-service | campus_review | reviews, likes, favorites |

**注意：** 启动前请确保 MySQL 和 Redis 服务已运行。

## API 文档

每个服务都集成了 SpringDoc/Swagger，启动后可访问：

| 服务 | Swagger UI |
|------|-----------|
| 用户服务（含通知） | http://localhost:8101/swagger-ui.html |
| 餐馆服务（含排行榜） | http://localhost:8102/swagger-ui.html |
| 评价服务（含互动） | http://localhost:8103/swagger-ui.html |

**注意：** 后台管理接口已合并到各服务中，直接通过 admin-gateway 访问：
- 用户管理：http://localhost:8002/api/admin/users
- 餐厅管理：http://localhost:8002/api/admin/restaurants

互动功能（点赞/收藏）已合并到 review-service，接口保持不变：
- 互动操作：http://localhost:8103/api/interactions/*

排行榜功能已合并到 restaurant-service：
- 热门餐馆榜：http://localhost:8102/api/rankings/hot-restaurants

通知功能已合并到 user-service：
- 站内消息：http://localhost:8101/api/notifications/*

## 接口示例

### 用户注册

```bash
curl -X POST http://localhost:8001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@campus.edu",
    "studentNo": "20250001",
    "password": "test123456",
    "nickname": "test"
  }'
```

### 用户登录

```bash
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "test@campus.edu",
    "password": "test123456"
  }'
```

### 健康检查

```bash
curl http://localhost:8101/api/health
```

## 配置说明

### 本地开发配置

- 默认关闭 Nacos（`spring.cloud.nacos.*.enabled=false`）
- 使用 H2 内存数据库
- JWT 密钥通过环境变量 `JWT_SECRET` 配置

### 关键配置项

```yaml
# JWT 密钥（user-service、user-gateway、admin-gateway 需保持一致）
security:
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-to-32-chars-min}
    ttl-seconds: 86400

# 热度权重（restaurant-service 排行榜功能）
ranking:
  hot-restaurants:
    like-weight: 2.0
    favorite-weight: 3.0
    review-weight: 5.0
    rating-weight: 10.0
    min-review-count: 5       # 最小评论数要求
    min-rating: 3.0           # 最小评分要求
    refresh-ms: 60000         # 定时刷新间隔（毫秒）
    redis-key: ranking:hot-restaurants

# Redis 配置（restaurant-service）
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}

# Flyway 配置（各服务）
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: false
```

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

### Prometheus + Grafana 集成

1. 配置 Prometheus 抓取各服务指标
2. 导入 Grafana 仪表盘进行可视化
3. 配置告警规则

```yaml
# Prometheus 配置示例
scrape_configs:
  - job_name: 'campus-review'
    static_configs:
      - targets: ['localhost:8101', 'localhost:8102', 'localhost:8103']
    metrics_path: '/actuator/prometheus'
```

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

## 测试

```bash
# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
```

## 部署

### Docker 部署

```bash
# 构建镜像
docker build -t campus-review .

# 使用 docker-compose 启动所有服务
docker-compose up -d
```

## 相关文档

- [快速开始](docs/quick-start.md)
- [API 文档](docs/api.md)
- [技术方案](docs/tech-solution.md)
- [测试数据脚本](docs/test-data.sql)
- [Postman 接口测试集合](docs/postman-collection.json)
- [贡献指南](CONTRIBUTING.md)
- [更新日志](CHANGELOG.md)
- [AGENTS.md](AGENTS.md) - 代码助手协作说明
- [部署指南](docs/deployment.md)
- [性能优化指南](docs/performance.md)
- [安全配置指南](docs/security.md)
- [日志配置指南](docs/logging.md)
- [Flyway 迁移指南](docs/flyway.md)
- [服务发现配置](docs/service-discovery.md)

## 后续优化方向

1. **链路追踪**: 集成 SkyWalking 或 Zipkin 实现分布式追踪
2. **消息队列**: 引入 Kafka/RabbitMQ 实现异步解耦
3. **弹性 resilience**: 集成 Resilience4j 实现熔断降级
4. **API 版本管理**: 添加 URI 版本前缀支持
5. **GraphQL 支持**: 为复杂查询场景提供 GraphQL API
