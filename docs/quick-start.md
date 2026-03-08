# 快速启动指南

## 项目概述

校园美食点评平台是一个基于 Spring Boot 3.x 的微服务项目，包含 8 个微服务和 2 个网关。

## 端口一览

### 网关
| 服务 | 端口 | 说明 |
|------|------|------|
| user-gateway | 8001 | 用户侧入口 |
| admin-gateway | 8002 | 管理侧入口 |

### 微服务
| 服务 | 端口 | 说明 |
|------|------|------|
| user-service | 8101 | 用户服务 |
| restaurant-service | 8102 | 餐馆服务 |
| review-service | 8103 | 评价服务 |
| interaction-service | 8104 | 互动服务 |
| ranking-service | 8105 | 排行榜服务 |
| notification-service | 8106 | 通知服务 |
| risk-control-service | 8107 | 风控服务 |
| admin-service | 8108 | 管理服务 |

## 前置要求

- JDK 17+
- Maven 3.6+
- (可选) Redis 6+ - 用于分布式缓存和限流
- (可选) Nacos - 用于服务发现（本地开发默认关闭）

## 快速启动

### 方式一：使用 Maven 直接运行

```bash
# 1. 克隆项目
cd campus-review

# 2. 编译项目
mvn clean install -DskipTests

# 3. 启动核心服务（新窗口）
mvn -pl campus-review-service/user-service -am spring-boot:run

# 4. 启动用户网关（新窗口）
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run
```

### 方式二：使用 Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down
```

## 启动顺序（推荐）

1. **核心服务**：user-service → restaurant-service → review-service → interaction-service
2. **依赖服务**：notification-service → risk-control-service → ranking-service → admin-service
3. **网关**：user-gateway → admin-gateway

## 配置说明

### 数据库
- 本地开发：使用 H2 内存数据库（自动初始化）
- 生产环境：配置 MySQL + Flyway 数据库迁移

### JWT 密钥
以下服务的 JWT 密钥配置必须保持一致：
- user-service
- user-gateway
- admin-gateway

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:your-secret-key-here}
```

### Redis 配置（可选）
用于分布式缓存和限流：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 健康检查

所有服务提供以下健康检查端点：

```bash
# Spring Boot Actuator 健康检查
curl http://localhost:8101/actuator/health

# 应用级健康检查
curl http://localhost:8101/api/health
curl http://localhost:8101/api/health/ready
```

## API 文档

启动服务后访问：
- Swagger UI: `http://localhost:8101/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8101/v3/api-docs`

## 关键 API 示例

### 认证接口（通过 user-gateway:8001）

#### 1. 注册

```bash
curl -X POST http://localhost:8001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@campus.edu","studentNo":"20250001","password":"Test123","nickname":"test"}'
```

#### 2. 登录

```bash
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"test@campus.edu","password":"Test123"}'
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

### 业务接口

#### 3. 创建餐馆

```bash
curl -X POST http://localhost:8001/api/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"一食堂","campus":"主校区","address":"学生服务中心一楼","description":"快餐","coverImageUrl":"http://example.com/cover.jpg"}'
```

#### 4. 发布评价

```bash
curl -X POST http://localhost:8001/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"restaurantId":1,"rating":5,"content":"味道很好，价格实惠！"}'
```

#### 5. 点赞

```bash
curl -X POST http://localhost:8001/api/interactions/like \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"targetType":"review","targetId":1}'
```

#### 6. 查询热门排行榜

```bash
curl http://localhost:8001/api/rankings/hot-restaurants
```

### 管理接口（通过 admin-gateway:8002）

后台接口要求用户具备 `ADMIN` 角色。

#### 查询待审核评价

```bash
curl http://localhost:8002/api/admin/reviews/pending \
  -H "Authorization: Bearer <admin-token>"
```

#### 通过评价

```bash
curl -X POST http://localhost:8002/api/admin/reviews/1/approve \
  -H "Authorization: Bearer <admin-token>"
```

## 测试数据

导入测试数据（可选）：
```bash
# 在 H2 控制台执行 docs/test-data.sql
# H2 控制台地址：http://localhost:8101/h2-console
# JDBC URL: jdbc:h2:mem:userdb
```

## 常见问题

### 1. 端口被占用
修改对应服务的 `application.yml` 中的 `server.port`

### 2. JWT 认证失败
检查 JWT 密钥配置是否一致

### 3. 服务无法发现
检查 Nacos 是否启动，或确认已配置 `nacos.discovery.enabled: false`

## 相关文档

- [完整 API 文档](./api.md)
- [部署指南](./deployment.md)
- [性能优化指南](./performance.md)
- [安全配置指南](./security.md)

