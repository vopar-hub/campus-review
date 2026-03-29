# Campus Review - 校园美食点评平台

基于 Spring Boot 3.x 的微服务校园美食点评系统，支持餐馆展示、评价审核、互动点赞、热门榜单等功能。

## 技术栈

| 技术           | 版本       | 说明          |
|--------------|----------|-------------|
| Java         | 17       | 开发语言        |
| Spring Boot  | 3.2.12   | 应用框架        |
| Spring Cloud | 2023.0.5 | 微服务框架       |
| Spring Cloud Alibaba | 2022.0.0.0 | Nacos 注册/配置中心 |
| MyBatis-Plus | 3.5.5    | ORM 框架      |
| MySQL        | 8.0      | 关系型数据库      |
| Redis        | 7.x      | 分布式缓存和限流    |
| MinIO        | -        | 对象存储（图片上传）  |
| JWT          | 0.12.6   | 身份认证        |
| Nacos        | 2.x      | 注册/配置中心     |
| Flyway       | 9.22.3   | 数据库版本管理     |
| SpringDoc    | 2.3.0    | API 文档      |

## 模块结构

```
campus-review/
├── campus-review-common       # 通用基础设施
├── campus-review-model        # 跨服务 DTO/模型
├── campus-review-utils        # 工具组件（JWT 等）
├── campus-review-service      # 微服务聚合（3 个核心服务）
│   ├── user-service           # 用户服务 (8104) - 包含通知功能
│   ├── restaurant-service     # 餐馆服务 (8102) - 包含排行榜功能
│   └── review-service         # 评价服务 (8103) - 包含互动功能
├── campus-review-gateway      # 网关聚合
│   ├── campus-review-user-gateway   # 用户网关 (8001)
│   └── campus-review-admin-gateway  # 管理网关 (8002)
├── static/                    # 静态资源和配置文件
│   ├── nacos-config/          # Nacos 配置中心文件
│   │   ├── user-service.yaml
│   │   ├── restaurant-service.yaml
│   │   ├── review-service.yaml
│   │   ├── user-gateway.yaml
│   │   └── admin-gateway.yaml
│   └── init-databases.sql     # 数据库初始化脚本
└── docs                       # 项目文档
```

---

## 快速开始（开发环境）

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+
- Nacos 2.x+（用于服务注册）
- MinIO（可选，用于图片上传）

### 1. 克隆项目

```bash
git clone https://github.com/your-username/campus-review.git
cd campus-review
```

### 2. 创建数据库

#### 方式一：使用 SQL 脚本（推荐）

```bash
# 执行数据库初始化脚本
mysql -u root -p < static/init-databases.sql
```

#### 方式二：手动创建

```sql
-- 创建数据库
CREATE DATABASE campus_review_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE campus_review_restaurant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE campus_review_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**说明**: 数据库表结构由 Flyway 自动创建，无需手动执行建表语句。

### 3. 启动基础设施

#### 启动 MySQL

确保 MySQL 服务运行在 `localhost:3306`，用户名 `root`，密码 `1234`

**验证数据库创建**:

```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'campus_review_%';"
```

#### 启动 Redis

```bash
redis-server
```

#### 启动 Nacos（注册中心）

```bash
# 下载 Nacos: https://github.com/alibaba/nacos/releases
cd nacos/bin
sh startup.sh -m standalone  # Linux/Mac
startup.cmd -m standalone     # Windows

# 访问 Nacos 控制台: http://localhost:8848/nacos
# 默认账号密码: nacos/nacos
```

### 4. 编译构建

```bash
# 全量构建
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests
```

### 5. 启动微服务（开发环境）

**重要**: 开发环境使用 `dev` profile，配置文件为 `application-dev.yml`

#### 启动顺序

1. **启动 user-service**

```bash
cd campus-review-service/user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

服务地址: http://localhost:8104

2. **启动 restaurant-service**

```bash
cd campus-review-service/restaurant-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

服务地址: http://localhost:8102

3. **启动 review-service**

```bash
cd campus-review-service/review-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

服务地址: http://localhost:8103

4. **启动 user-gateway**

```bash
cd campus-review-gateway/campus-review-user-gateway
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

网关地址: http://localhost:8001

5. **启动 admin-gateway**

```bash
cd campus-review-gateway/campus-review-admin-gateway
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

网关地址: http://localhost:8002

### 6. 验证服务

#### 检查 Nacos 注册中心

访问 http://localhost:8848/nacos，在"服务管理 > 服务列表"中应该看到以下服务：

- user-service
- restaurant-service
- review-service
- user-gateway
- admin-gateway

#### 检查 Flyway 迁移

```sql
-- 检查 Flyway 迁移历史
SELECT * FROM campus_review_user.flyway_schema_history;
SELECT * FROM campus_review_restaurant.flyway_schema_history;
SELECT * FROM campus_review_review.flyway_schema_history;
```

#### 测试接口

```bash
# 健康检查
curl http://localhost:8104/actuator/health

# 用户注册
curl -X POST http://localhost:8001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@campus.edu",
    "studentNo": "20250001",
    "password": "test123456",
    "nickname": "test"
  }'

# 用户登录
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "test@campus.edu",
    "password": "test123456"
  }'
```

### 开发环境配置说明

开发环境配置特点：

- ✅ **Nacos 注册中心**: 启用（网关需要通过注册中心发现服务）
- ❌ **Nacos 配置中心**: 禁用（配置写在本地 application-dev.yml）
- ✅ **Flyway**: 启用（自动创建数据库表）
- ✅ **数据库**: localhost:3306, root/1234
- ✅ **Redis**: localhost:6379
- ✅ **MinIO**: localhost:9000, minioadmin/minioadmin
- ✅ **JWT**: 硬编码密钥（仅限开发环境）

---

## 生产环境部署

### 1. 环境准备

#### 1.1 启动 Nacos（注册中心 + 配置中心）

```bash
cd nacos/bin
sh startup.sh -m standalone  # Linux/Mac
startup.cmd -m standalone     # Windows
```

#### 1.2 创建 Nacos 配置

访问 Nacos 控制台: http://localhost:8848/nacos

**配置管理 → 配置列表 → 创建配置**

**配置文件位置**: `static/nacos-config/` 目录下包含 5 个配置文件：

- `user-service.yaml` - 用户服务配置
- `restaurant-service.yaml` - 餐厅服务配置
- `review-service.yaml` - 评价服务配置
- `user-gateway.yaml` - 用户网关配置
- `admin-gateway.yaml` - 管理网关配置

按照以下步骤创建 5 个配置：

##### 配置 1: user-service.yaml

- **Data ID**: `user-service.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `static/nacos-config/user-service.yaml` 的内容

##### 配置 2: restaurant-service.yaml

- **Data ID**: `restaurant-service.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `static/nacos-config/restaurant-service.yaml` 的内容

##### 配置 3: review-service.yaml

- **Data ID**: `review-service.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `static/nacos-config/review-service.yaml` 的内容

##### 配置 4: user-gateway.yaml

- **Data ID**: `user-gateway.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `static/nacos-config/user-gateway.yaml` 的内容

##### 配置 5: admin-gateway.yaml

- **Data ID**: `admin-gateway.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `static/nacos-config/admin-gateway.yaml` 的内容

### 2. 设置环境变量

在生产环境服务器上设置以下环境变量：

```bash
# Nacos 地址
export NACOS_SERVER_ADDR=localhost:8848

# 数据库配置
export DB_URL="jdbc:mysql://your-db-host:3306/campus_review_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
export DB_USERNAME="your_db_user"
export DB_PASSWORD="your_db_password"

# Redis 配置
export REDIS_HOST="your-redis-host"
export REDIS_PORT="6379"
export REDIS_PASSWORD="your_redis_password"
export REDIS_DATABASE="0"

# JWT 密钥（必须至少 32 个字符）
export JWT_SECRET="your-production-jwt-secret-key-at-least-32-characters-long"

# MinIO 配置
export MINIO_ENDPOINT="http://your-minio-host:9000"
export MINIO_ACCESS_KEY="your-access-key"
export MINIO_SECRET_KEY="your-secret-key"
export MINIO_BUCKET="campus-review"

# JWT 过期时间（可选，默认 86400000 毫秒 = 24 小时）
export JWT_EXPIRATION="86400000"
```

**重要提示**:
- JWT_SECRET 必须至少 32 个字符
- 所有微服务和网关必须使用相同的 JWT_SECRET
- 生产环境必须修改所有默认密码

### 3. 启动微服务（生产环境）

**重要**: 生产环境使用 `prod` profile，配置文件为 `application-prod.yml`

#### 启动顺序

```bash
# 1. 启动 user-service
cd campus-review-service/user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 2. 启动 restaurant-service
cd campus-review-service/restaurant-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 3. 启动 review-service
cd campus-review-service/review-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 4. 启动 user-gateway
cd campus-review-gateway/campus-review-user-gateway
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 5. 启动 admin-gateway
cd campus-review-gateway/campus-review-admin-gateway
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### 4. 验证生产环境

#### 检查 Nacos 服务注册

访问 Nacos 控制台，确认所有服务已注册：

- user-service
- restaurant-service
- review-service
- user-gateway
- admin-gateway

#### 检查配置加载

查看服务启动日志，确认从 Nacos 配置中心加载配置：

```
Located property source: [BootstrapPropertySource {name='bootstrapProperties-user-service.yaml,DEFAULT_GROUP'}]
```

#### 测试接口

```bash
# 通过网关访问服务
curl http://your-gateway-host:8001/actuator/health
curl http://your-gateway-host:8002/actuator/health
```

### 生产环境配置说明

生产环境配置特点：

- ✅ **Nacos 注册中心**: 启用
- ✅ **Nacos 配置中心**: 启用（配置存储在 Nacos）
- ✅ **Flyway**: 启用（`clean-disabled: true` 禁止清空数据库）
- ✅ **敏感信息**: 从环境变量读取
- ✅ **日志级别**: INFO（减少日志输出）

---

## 配置文件说明

### 配置分层结构

每个微服务和网关都有三层配置：

1. **application.yml**: 通用配置（所有环境共享）
2. **application-dev.yml**: 开发环境配置
3. **application-prod.yml**: 生产环境配置

### 配置加载优先级

```
application-prod.yml > nacos-config/xxx.yaml > application-dev.yml > application.yml
```

### 关键配置项

#### JWT 配置

```yaml
security:
  jwt:
    secret: ${JWT_SECRET}  # 生产环境从环境变量读取
    expiration: ${JWT_EXPIRATION:86400000}  # 默认 24 小时
```

**重要**: user-service, user-gateway, admin-gateway 必须使用相同的 JWT_SECRET

#### 数据库配置

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/db_name}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      max-lifetime: 600000
      connection-timeout: 30000
```

#### Flyway 配置

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true  # 允许在非空数据库上执行
    locations: classpath:db/migration
    validate-on-migrate: false
    clean-disabled: true  # 生产环境禁止 clean
```

#### 网关路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service  # 使用 Nacos 服务发现
          predicates:
            - Path=/api/auth/**, /api/users/**, /api/notifications/**
```

---

## API 文档

每个服务都集成了 SpringDoc，启动后可访问：

| 服务 | Swagger UI | 说明 |
|------|-----------|------|
| user-service | http://localhost:8104/swagger-ui.html | 用户认证、通知功能 |
| restaurant-service | http://localhost:8102/swagger-ui.html | 餐厅管理、排行榜 |
| review-service | http://localhost:8103/swagger-ui.html | 评价、点赞、收藏 |
| user-gateway | http://localhost:8001/swagger-ui.html | 用户侧网关路由 |
| admin-gateway | http://localhost:8002/swagger-ui.html | 后台管理网关路由 |

---

## 数据库架构

| 服务 | 数据库 | 主要表 |
|------|--------|--------|
| user-service | campus_review_user | users, messages |
| restaurant-service | campus_review_restaurant | restaurants, hot_restaurant_rank |
| review-service | campus_review_review | reviews, likes, favorites |

### 数据库初始化

服务启动时，Flyway 会自动执行迁移脚本：

- `user-service/src/main/resources/db/migration/V1_0_0__initial_schema.sql`
- `restaurant-service/src/main/resources/db/migration/V1_0_0__initial_schema.sql`
- `review-service/src/main/resources/db/migration/V1_0_0__initial_schema.sql`

---

## 监控与可观测性

### Actuator 端点

每个服务都提供了 Actuator 监控端点：

| 端点 | 说明 | 示例 |
|------|------|------|
| health | 健康检查 | http://localhost:8104/actuator/health |
| info | 应用信息 | http://localhost:8104/actuator/info |
| metrics | 性能指标 | http://localhost:8104/actuator/metrics |
| prometheus | Prometheus 格式指标 | http://localhost:8104/actuator/prometheus |

### 健康检查探针

```bash
# 存活探针（Liveness Probe）
curl http://localhost:8104/actuator/health/liveness

# 就绪探针（Readiness Probe）
curl http://localhost:8104/actuator/health/readiness

# 业务健康检查
curl http://localhost:8104/api/health
```

---

## 项目特性

- **配置分层**: application.yml / application-dev.yml / application-prod.yml 三层配置
- **环境隔离**: 开发环境本地配置，生产环境 Nacos 配置中心
- **敏感信息保护**: 生产环境敏感信息从环境变量读取
- **数据库迁移**: Flyway 版本管理和自动迁移
- **服务发现**: Nacos 服务注册与发现（支持负载均衡）
- **API 网关**: Spring Cloud Gateway 路由和鉴权
- **JWT 认证**: 统一身份认证
- **请求追踪**: 每个请求自动生成 requestId，支持全链路日志追踪
- **全局异常处理**: 统一异常处理与响应格式
- **API 文档**: 所有控制器已集成 OpenAPI 注解
- **健康检查**: 提供 `/actuator/health` 端点
- **Redis 缓存**: 排行榜接口使用 Redis ZSet 缓存
- **对象存储**: MinIO 图片上传
- **安全防护**: CORS、安全响应头、JWT 鉴权

---

## 测试

```bash
# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
```

---

## 部署

### Docker 部署

```bash
# 构建镜像
docker build -t campus-review .

# 使用 docker-compose 启动所有服务
docker-compose up -d
```

---

## 相关文档

- [快速开始](docs/quick-start.md)
- [API 文档](docs/api.md)
- [技术方案](docs/tech-solution.md)
- [微服务架构分析](campus-review-service/docs/微服务架构分析.md)
- [测试数据脚本](docs/test-data.sql)
- [Postman 接口测试集合](docs/postman-collection.json)
- [贡献指南](CONTRIBUTING.md)
- [更新日志](CHANGELOG.md)
- [AGENTS.md](AGENTS.md) - 代码助手协作说明

---

## 后续优化方向

1. **链路追踪**: 集成 SkyWalking 或 Zipkin 实现分布式追踪
2. **消息队列**: 引入 Kafka/RabbitMQ 实现异步解耦（通知发送、评价审核）
3. **弹性 resilience**: 集成 Resilience4j 实现熔断降级
4. **API 版本管理**: 添加 URI 版本前缀支持
5. **GraphQL 支持**: 为复杂查询场景提供 GraphQL API

---

## 更新日志

### v2.1.0 (2026-03-29) - 配置重构

**配置优化**
- ✅ 配置分层：application.yml / application-dev.yml / application-prod.yml
- ✅ 开发环境：使用 Nacos 注册中心，不使用配置中心
- ✅ 生产环境：使用 Nacos 配置中心，敏感信息从环境变量读取
- ✅ Flyway 启用：自动数据库迁移
- ✅ Nacos 配置文件：创建 5 个服务的 Nacos 配置

**文档更新**
- ✅ 删除微服务架构演进部分
- ✅ 更新快速开始（开发环境）
- ✅ 补充生产环境部署步骤
- ✅ 添加 Nacos 配置说明

### v2.0.0 (2026-03-28) - 微服务架构优化

**架构重构**
- ✅ 移除 admin-service，前端直接调用下游服务
- ✅ 合并 interaction-service 到 review-service
- ✅ 合并 ranking-service 到 restaurant-service
- ✅ 合并 notification-service 到 user-service
- ✅ 微服务数量：8 → 3

**配置优化**
- ✅ 更新 Nacos 配置（8 份 → 5 份）
- ✅ 添加 Flyway 自动迁移配置
- ✅ 添加 Redis 排行榜配置
- ✅ 启用 @EnableScheduling 定时任务

**数据库变更**
- ✅ likes 表（从 interaction-service 合并）
- ✅ favorites 表（从 interaction-service 合并）
- ✅ messages 表（从 notification-service 合并）
- ✅ hot_restaurant_rank 表（从 ranking-service 合并）
