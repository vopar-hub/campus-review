# 部署指南

本文档介绍 Campus Review 项目的部署方式和步骤。

## 部署方式

项目支持以下部署方式：

1. **本地开发部署** - 使用 H2 内存数据库
2. **Docker 容器化部署** - 使用 docker-compose 编排所有服务
3. **生产环境部署** - 使用 MySQL + Redis + Nacos

## 本地开发部署

### 前置条件

- JDK 17+
- Maven 3.8+
- 可选：Redis 7.x（用于缓存和限流）

### 启动步骤

```bash
# 1. 编译项目
mvn clean install -DskipTests

# 2. 启动用户服务
mvn -pl campus-review-service/user-service -am spring-boot:run

# 3. 启动用户网关（新终端）
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run
```

### 验证部署

```bash
# 健康检查
curl http://localhost:8101/api/health

# 访问 Swagger UI
open http://localhost:8101/swagger-ui.html
```

## Docker 容器化部署

### 前置条件

- Docker 20.x+
- Docker Compose 2.x+

### 启动步骤

```bash
# 1. 构建所有服务的 Docker 镜像
docker-compose build

# 2. 启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f user-service
```

### 服务端口

| 服务 | 容器名 | 端口 |
|------|--------|------|
| 用户网关 | user-gateway | 8001 |
| 管理网关 | admin-gateway | 8002 |
| 用户服务 | user-service | 8101 |
| 餐馆服务 | restaurant-service | 8102 |
| 评价服务 | review-service | 8103 |
| 互动服务 | interaction-service | 8104 |
| 排行榜服务 | ranking-service | 8105 |
| 通知服务 | notification-service | 8106 |
| 风控服务 | risk-control-service | 8107 |
| 管理服务 | admin-service | 8108 |

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除卷
docker-compose down -v
```

## 生产环境部署

### 架构说明

```
                    ┌─────────────────┐
                    │   Nginx/K8s     │
                    │   Ingress       │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
    ┌───────▼────────┐ ┌────▼────────┐ ┌────▼────────┐
    │ User Gateway   │ │ Admin       │ │  Prometheus │
    │ (8001)         │ │ Gateway     │ │  Grafana    │
    └───────┬────────┘ │ (8002)      │ │             │
            │          └─────┬───────┘ └─────────────┘
            │                │
    ┌───────▼────────────────▼───────┐
    │      Service Mesh/Pod          │
    └────────────────────────────────┘
            │                │                │
    ┌───────▼───────┐ ┌─────▼──────┐ ┌──────▼──────┐
    │    MySQL      │ │   Redis    │ │    Nacos    │
    │    Cluster    │ │  Cluster   │ │   Cluster   │
    └───────────────┘ └────────────┘ └─────────────┘
```

### 前置条件

- MySQL 8.0+ 集群
- Redis 7.x 集群
- Nacos 2.x 集群（可选，用于服务发现）
- Kubernetes 集群或 VM 资源

### 配置说明

#### 1. 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql-cluster:3306/campus_review?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  sql:
    init:
      mode: never
```

#### 2. Redis 配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      database: 0
      cluster:
        nodes:
          - redis-node1:6379
          - redis-node2:6379
          - redis-node3:6379
```

#### 3. Nacos 配置

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: true
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE}
        group: DEFAULT_GROUP
      discovery:
        enabled: true
        server-addr: ${NACOS_SERVER:localhost:8848}
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| JWT_SECRET | JWT 签名密钥 | - |
| REDIS_HOST | Redis 主机地址 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| REDIS_PASSWORD | Redis 密码 | - |
| DB_USERNAME | 数据库用户名 | - |
| DB_PASSWORD | 数据库密码 | - |
| NACOS_SERVER | Nacos 服务器地址 | localhost:8848 |

### Kubernetes 部署

```yaml
# deployment.yaml 示例
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: campus-review/user-service:latest
        ports:
        - containerPort: 8101
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8101
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8101
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

## 监控部署

### Prometheus 配置

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'campus-review'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'user-service:8101'
        - 'restaurant-service:8102'
        - 'review-service:8103'
        - 'interaction-service:8104'
        - 'ranking-service:8105'
        - 'notification-service:8106'
        - 'risk-control-service:8107'
        - 'admin-service:8108'
```

### Grafana 仪表盘

导入以下 Dashboard ID：
- JVM Micrometer: 4701
- Spring Boot Statistics: 11378
- Custom Campus Review Dashboard（自定义）

## 故障排查

### 常见问题

#### 1. 服务启动失败

```bash
# 检查日志
docker-compose logs <service-name>

# 查看应用日志
tail -f logs/application.log
```

#### 2. Redis 连接失败

```bash
# 测试 Redis 连接
redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} ping

# 预期输出：PONG
```

#### 3. 数据库连接失败

```bash
# 测试 MySQL 连接
mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USERNAME} -p

# 检查数据库是否存在
SHOW DATABASES LIKE 'campus_review';
```

#### 4. 内存溢出

调整 JVM 参数：
```bash
JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"
```

## 性能调优

### JVM 参数建议

```bash
# 生产环境推荐配置
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/heapdump.hprof"
```

### Redis 缓存配置

```yaml
# ranking-service 缓存优化
spring:
  cache:
    redis:
      time-to-live: 300000  # 5 分钟
      cache-null-values: false
```

### 数据库连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 回滚策略

```bash
# Docker 回滚
docker-compose pull <service>:<previous-version>
docker-compose up -d <service>

# Kubernetes 回滚
kubectl rollout undo deployment/user-service
```

## 安全建议

1. **生产环境必须修改 JWT_SECRET**
2. **启用 HTTPS**
3. **配置防火墙规则**
4. **定期更新依赖**
5. **启用 Actuator 安全认证**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /actuator
  security:
    enabled: true
    username: admin
    password: ${ACTUATOR_PASSWORD}
```
