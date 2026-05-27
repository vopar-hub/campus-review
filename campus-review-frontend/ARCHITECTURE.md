# 网络架构图

## 完整架构图

```mermaid
graph TB
    subgraph "客户端"
        Browser[浏览器]
    end

    subgraph "Nginx 反向代理服务器 (端口 80)"
        Nginx[Nginx<br/>反向代理]
    end

    subgraph "前端服务"
        ViteDev[Vite 开发服务器<br/>端口 3000]
        StaticFiles[静态文件<br/>生产环境]
    end

    subgraph "后端网关服务"
        UserGateway[用户网关<br/>端口 8001]
        AdminGateway[管理端网关<br/>端口 8002]
    end

    subgraph "后端微服务"
        AuthService[认证服务]
        UserService[用户服务]
        RestaurantService[餐馆服务]
        ReviewService[评价服务]
        AdminService[管理服务]
    end

    %% 客户端请求流程
    Browser -->|HTTP 请求| Nginx

    %% Nginx 路由分发
    Nginx -->|静态资源请求 /| ViteDev
    Nginx -.->|生产环境| StaticFiles
    Nginx -->|用户 API /api/auth<br/>/api/user<br/>/api/restaurant 等| UserGateway
    Nginx -->|管理端 API /api/admin/*| AdminGateway

    %% 网关到微服务
    UserGateway --> AuthService
    UserGateway --> UserService
    UserGateway --> RestaurantService
    UserGateway --> ReviewService
    AdminGateway --> AdminService

    %% 样式
    classDef browserStyle fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef nginxStyle fill:#fff9c4,stroke:#f57f17,stroke-width:3px
    classDef frontendStyle fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef gatewayStyle fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    classDef serviceStyle fill:#fce4ec,stroke:#880e4f,stroke-width:2px

    class Browser browserStyle
    class Nginx nginxStyle
    class ViteDev,StaticFiles frontendStyle
    class UserGateway,AdminGateway gatewayStyle
    class AuthService,UserService,RestaurantService,ReviewService,AdminService serviceStyle
```

## 请求流程详解

### 1. 用户认证流程

```mermaid
sequenceDiagram
    participant B as 浏览器
    participant N as Nginx (80)
    participant UG as 用户网关 (8001)
    participant AS as 认证服务

    B->>N: POST /api/auth/login
    N->>UG: 转发请求到 8001
    UG->>AS: 验证用户凭证
    AS-->>UG: 返回 JWT Token
    UG-->>N: 返回响应
    N-->>B: 返回 Token + 用户信息

    Note over B,AS: 登录成功后，前端将 Token 存储到 Cookie
```

### 2. 普通用户请求流程

```mermaid
sequenceDiagram
    participant B as 浏览器
    participant N as Nginx (80)
    participant UG as 用户网关 (8001)
    participant RS as 餐馆服务

    B->>N: GET /api/restaurant/list<br/>Authorization: Bearer {token}
    N->>UG: 转发到 8001
    UG->>UG: 验证 JWT Token
    UG->>RS: 查询餐馆列表
    RS-->>UG: 返回数据
    UG-->>N: 返回 JSON 响应
    N-->>B: 返回餐馆列表

    Note over B,RS: Token 过期时返回 401，前端自动刷新
```

### 3. 管理端请求流程

```mermaid
sequenceDiagram
    participant B as 浏览器
    participant N as Nginx (80)
    participant AG as 管理端网关 (8002)
    participant AS as 管理服务

    B->>N: POST /api/admin/users<br/>Authorization: Bearer {token}<br/>X-User-Id: 1<br/>X-User-Roles: ADMIN
    N->>AG: 转发到 8002<br/>传递身份头
    AG->>AG: 验证 JWT Token
    AG->>AG: 验证管理员权限
    AG->>AS: 执行管理操作
    AS-->>AG: 返回结果
    AG-->>N: 返回 JSON 响应
    N-->>B: 返回操作结果

    Note over B,AS: 管理端请求需要额外的身份头
```

### 4. Token 刷新流程

```mermaid
sequenceDiagram
    participant B as 浏览器
    participant N as Nginx (80)
    participant UG as 用户网关 (8001)
    participant AS as 认证服务

    B->>N: GET /api/user/profile<br/>Authorization: Bearer {expired_token}
    N->>UG: 转发请求
    UG-->>N: 401 Unauthorized
    N-->>B: 401 错误

    B->>B: 检测到 401 错误
    B->>N: POST /api/auth/refresh<br/>RefreshToken: {refresh_token}
    N->>UG: 转发刷新请求
    UG->>AS: 验证 Refresh Token
    AS-->>UG: 返回新 Token
    UG-->>N: 返回新 Token
    N-->>B: 返回新 Token

    B->>B: 更新 Cookie 中的 Token
    B->>N: GET /api/user/profile<br/>Authorization: Bearer {new_token}
    N->>UG: 重试原请求
    UG-->>N: 返回用户信息
    N-->>B: 返回成功响应

    Note over B,AS: 自动刷新机制，用户无感知
```

## 开发环境 vs 生产环境

### 开发环境架构

```mermaid
graph LR
    subgraph "开发环境"
        Browser[浏览器<br/>localhost:80]
        Nginx[Nginx<br/>端口 80]
        Vite[Vite Dev Server<br/>端口 3000]
        Gateway1[用户网关<br/>端口 8001]
        Gateway2[管理端网关<br/>端口 8002]
    end

    Browser --> Nginx
    Nginx -->|静态资源| Vite
    Nginx -->|用户 API| Gateway1
    Nginx -->|管理端 API| Gateway2

    style Browser fill:#e1f5ff
    style Nginx fill:#fff9c4
    style Vite fill:#f3e5f5
    style Gateway1 fill:#e8f5e9
    style Gateway2 fill:#e8f5e9
```

### 生产环境架构

```mermaid
graph LR
    subgraph "生产环境"
        Browser[浏览器<br/>域名访问]
        Nginx[Nginx<br/>端口 80/443]
        Static[静态文件<br/>HTML/CSS/JS]
        Gateway1[用户网关<br/>端口 8001]
        Gateway2[管理端网关<br/>端口 8002]
    end

    Browser --> Nginx
    Nginx -->|直接提供静态文件| Static
    Nginx -->|用户 API| Gateway1
    Nginx -->|管理端 API| Gateway2

    style Browser fill:#e1f5ff
    style Nginx fill:#fff9c4
    style Static fill:#f3e5f5
    style Gateway1 fill:#e8f5e9
    style Gateway2 fill:#e8f5e9
```

## 端口映射表

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80 | 反向代理入口（可配置为其他端口） |
| Vite Dev Server | 3000 | 开发环境前端服务器 |
| 用户网关 | 8001 | 用户相关 API |
| 管理端网关 | 8002 | 管理端 API |

## API 路由规则

| 路径 | 目标服务 | 示例 |
|------|---------|------|
| `/` | 前端静态资源 | `/`, `/login`, `/restaurants` |
| `/api/auth/*` | 用户网关 (8001) | `/api/auth/login`, `/api/auth/register` |
| `/api/user/*` | 用户网关 (8001) | `/api/user/profile`, `/api/user/update` |
| `/api/restaurant/*` | 用户网关 (8001) | `/api/restaurant/list`, `/api/restaurant/1` |
| `/api/review/*` | 用户网关 (8001) | `/api/review/create`, `/api/review/list` |
| `/api/interaction/*` | 用户网关 (8001) | `/api/interaction/like`, `/api/interaction/favorite` |
| `/api/ranking/*` | 用户网关 (8001) | `/api/ranking/restaurants` |
| `/api/notification/*` | 用户网关 (8001) | `/api/notification/list` |
| `/api/admin/*` | 管理端网关 (8002) | `/api/admin/users`, `/api/admin/restaurants` |

## 安全机制

```mermaid
graph TB
    subgraph "安全层级"
        L1[客户端安全]
        L2[传输安全]
        L3[网关安全]
        L4[服务安全]
    end

    L1 -->|1. Token 存储| Cookie[HttpOnly Cookie]
    L1 -->|2. XSS 防护| CSP[内容安全策略]

    L2 -->|1. HTTPS| SSL[TLS 加密]
    L2 -->|2. 请求签名| Sign[请求签名验证]

    L3 -->|1. JWT 验证| JWT[Token 验证]
    L3 -->|2. 权限检查| RBAC[角色权限控制]
    L3 -->|3. 限流| Rate[请求频率限制]

    L4 -->|1. 数据验证| Validate[输入验证]
    L4 -->|2. SQL 注入防护| SQL[参数化查询]
    L4 -->|3. 日志审计| Log[操作日志]

    style L1 fill:#e1f5ff
    style L2 fill:#fff9c4
    style L3 fill:#e8f5e9
    style L4 fill:#fce4ec
```

## 负载均衡扩展（可选）

```mermaid
graph TB
    subgraph "负载均衡架构"
        Browser[浏览器]

        subgraph "Nginx 负载均衡"
            LB[Nginx<br/>负载均衡器]
        end

        subgraph "前端集群"
            FE1[前端实例 1]
            FE2[前端实例 2]
        end

        subgraph "网关集群"
            UG1[用户网关 1]
            UG2[用户网关 2]
            AG1[管理端网关 1]
            AG2[管理端网关 2]
        end
    end

    Browser --> LB
    LB -->|轮询/权重| FE1
    LB -->|轮询/权重| FE2
    LB -->|用户 API| UG1
    LB -->|用户 API| UG2
    LB -->|管理端 API| AG1
    LB -->|管理端 API| AG2

    style Browser fill:#e1f5ff
    style LB fill:#fff9c4
    style FE1 fill:#f3e5f5
    style FE2 fill:#f3e5f5
    style UG1 fill:#e8f5e9
    style UG2 fill:#e8f5e9
    style AG1 fill:#e8f5e9
    style AG2 fill:#e8f5e9
```

## 总结

这个架构的核心优势：

1. **统一入口**：所有请求通过 Nginx 统一代理，简化前端配置
2. **服务隔离**：用户网关和管理端网关分离，职责清晰
3. **安全可控**：Nginx 层面可以做统一的安全控制和限流
4. **易于扩展**：后续可以轻松添加新的网关服务
5. **开发友好**：开发环境和生产环境架构一致，减少环境差异

喵~ 主人，这个架构图应该很清楚了吧！有什么不明白的地方随时问我哦~ 尾巴摇摇~ 🐱
