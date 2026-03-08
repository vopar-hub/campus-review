# 服务发现配置指南

本文档介绍 Campus Review 项目的服务发现配置和使用方法。

## 概述

项目使用 **Nacos** 作为服务注册与发现中心，支持：

- **服务自动注册** - 服务启动时自动注册到 Nacos
- **服务发现** - 通过服务名进行负载均衡调用
- **配置中心** - 集中管理微服务配置（可选）
- **健康检查** - 自动剔除不健康的服务实例

## 架构设计

```
                    ┌─────────────────┐
                    │   Nacos Server  │
                    │  (localhost:    │
                    │     8848)       │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │  user-gateway   │ │  admin-gateway  │ │  user-service   │
    │    (8001)       │ │    (8002)       │ │    (8101)       │
    │  lb://user-     │ │  lb://admin-    │ │  注册到 Nacos   │
    │    service      │ │    service      │ │                 │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 服务注册与发现

### 服务列表

| 服务名 | 端口 | 说明 |
|--------|------|------|
| user-service | 8101 | 用户服务 |
| restaurant-service | 8102 | 餐馆服务 |
| review-service | 8103 | 评价服务 |
| interaction-service | 8104 | 互动服务 |
| ranking-service | 8105 | 排行榜服务 |
| notification-service | 8106 | 通知服务 |
| risk-control-service | 8107 | 风控服务 |
| admin-service | 8108 | 管理服务 |
| user-gateway | 8001 | 用户网关 |
| admin-gateway | 8002 | 管理网关 |

### 配置方式

#### 开发环境（本地）

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: false  # 本地开发关闭配置中心
      discovery:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
  application:
    name: user-service  # 每个服务名称不同
```

#### 生产环境（Docker/K8s）

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:nacos-server:8848}
        namespace: ${NACOS_NAMESPACE:prod}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
      discovery:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:nacos-server:8848}
        namespace: ${NACOS_NAMESPACE:prod}
```

### Gateway 路由配置

Gateway 使用 `lb://` 前缀实现服务发现负载均衡：

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true  # 启用服务发现
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service  # lb 表示负载均衡
          predicates:
            - Path=/api/auth/**,/api/users/**
        - id: restaurant-service
          uri: lb://restaurant-service
          predicates:
            - Path=/api/restaurants/**
```

### 服务间调用

使用 `RestTemplate` 或 `WebClient` 进行服务间调用时，使用服务名：

```java
@RestController
public class RankingController {

    @Value("${downstream.restaurant-service-base-url:lb://restaurant-service}")
    private String restaurantServiceUrl;

    private final RestTemplate restTemplate;

    // 使用服务名调用
    public List<RestaurantDTO> getRestaurants() {
        return restTemplate.getForObject(
            "lb://restaurant-service/api/restaurants",  // 服务名调用
            List.class
        );
    }
}
```

## 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `NACOS_SERVER_ADDR` | `localhost:8848` | Nacos 服务器地址 |
| `NACOS_NAMESPACE` | `public` | 命名空间 ID |
| `NACOS_GROUP` | `DEFAULT_GROUP` | 配置分组 |
| `NACOS_USERNAME` | - | Nacos 用户名（可选） |
| `NACOS_PASSWORD` | - | Nacos 密码（可选） |

## 启动方式

### 本地开发

#### 方式一：使用 Maven

```bash
# 启动 Nacos（确保已安装）
cd nacos/bin
startup.cmd -m standalone

# 启动服务
mvn -pl campus-review-service/user-service -am spring-boot:run
```

#### 方式二：使用 Docker

```bash
# 启动 Nacos
docker run -d \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  --name nacos \
  nacos/nacos-server:2.2.0

# 启动服务
mvn -pl campus-review-service/user-service -am spring-boot:run
```

### Docker Compose 部署

```yaml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:2.2.0
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"
      - "9848:9848"

  user-service:
    build: ./campus-review-service/user-service
    environment:
      - NACOS_SERVER_ADDR=nacos:8848
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - nacos

  user-gateway:
    build: ./campus-review-gateway/campus-review-user-gateway
    environment:
      - NACOS_SERVER_ADDR=nacos:8848
    ports:
      - "8001:8001"
    depends_on:
      - user-service
      - nacos
```

## 验证服务注册

### 访问 Nacos 控制台

浏览器访问：`http://localhost:8848/nacos`

- 默认账号：`nacos`
- 默认密码：`nacos`

### 查看服务列表

```bash
# 通过 API 查询已注册服务
curl -X GET "http://localhost:8848/nacos/v1/ns/catalog/services"
```

### 服务健康检查

```bash
# 查询特定服务实例
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service"
```

## 负载均衡策略

Spring Cloud Gateway 默认使用 **Ribbon** 或 **Spring Cloud LoadBalancer** 进行客户端负载均衡：

### 轮询策略（默认）

```yaml
# application.yml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: true
```

### 自定义策略

```java
@Configuration
public class LoadBalancerConfig {

    @Bean
    public ReactorServiceInstanceLoadBalancer loadBalancer() {
        return new RoundRobinLoadBalancer(); // 轮询
        // return new RandomLoadBalancer();  // 随机
        // return new StickyLoadBalancer();  // 粘性
    }
}
```

## 服务降级与熔断

### 配置服务降级

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/auth/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCB
                fallbackUri: forward:/fallback/user
```

### 配置超时

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
```

## 常见问题

### 1. 服务无法注册到 Nacos

检查 Nacos 服务是否启动：

```bash
curl http://localhost:8848/nacos/
```

检查服务配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: localhost:8848  # 确保地址正确
```

### 2. 服务间调用失败

确保使用 `lb://` 前缀：

```yaml
# ✅ 正确
downstream:
  user-service-base-url: lb://user-service

# ❌ 错误
downstream:
  user-service-base-url: http://localhost:8101
```

### 3. Gateway 无法转发请求

检查 Gateway 是否启用了服务发现：

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
```

### 4. Nacos 性能优化

对于大规模部署：

```yaml
# 增加心跳间隔
spring:
  cloud:
    nacos:
      discovery:
        heart-beat-interval: 5000  # 默认 5000ms
        heart-beat-timeout: 15000  # 默认 15000ms
        ip-delete-timeout: 30000   # 默认 30000ms
```

## 最佳实践

1. **开发环境**：可关闭 Nacos，使用直连方式测试
2. **测试环境**：使用独立 Nacos 实例
3. **生产环境**：使用 Nacos 集群，配置多节点
4. **服务命名**：统一使用小写字母 + 中划线格式
5. **配置外部化**：敏感配置使用环境变量或 Nacos 配置中心
6. **健康检查**：配置合理的超时和重试机制
7. **灰度发布**：使用 Nacos 元数据支持版本路由

## 参考资料

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Spring Cloud Alibaba 参考文档](https://sca.alibaba.com/)
- [Spring Cloud Gateway 文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
