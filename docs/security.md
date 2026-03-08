# 安全配置指南

本文档介绍 Campus Review 项目的安全配置和最佳实践。

## 安全特性概览

项目已实现以下安全特性：

| 特性 | 位置 | 说明 |
|------|------|------|
| JWT 认证 | 网关层 | 基于 Token 的身份验证 |
| CORS 配置 | 网关层 | 跨域请求控制 |
| 安全响应头 | 网关层 | 防止常见 Web 攻击 |
| 密码强度校验 | 用户服务 | 确保密码安全性 |
| 内容审核 | 风控服务 | 敏感词过滤 |
| 分布式限流 | 风控服务 | 防止暴力攻击 |

## 网关安全配置

### 1. JWT 认证

**配置位置：**
- `campus-review-user-gateway/src/main/java/com/vapor/gateway/user/filter/JwtAuthGlobalFilter.java`
- `campus-review-admin-gateway/src/main/java/com/vapor/gateway/admin/filter/AdminJwtAuthGlobalFilter.java`

**白名单路由：**
```java
// 用户网关白名单
- /api/auth/login        // 登录
- /api/auth/register     // 注册
- /api/restaurants       // 餐馆列表（GET）
- /api/rankings/hot-restaurants  // 热门榜单（GET）
```

**Token 传递：**
```
Authorization: Bearer <token>
```

**用户信息透传：**
```
X-User-Id: <用户 ID>
X-User-Roles: <角色列表>
```

### 2. CORS 配置

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            maxAge: 3600
            allowCredentials: true
```

**生产环境建议：**
```yaml
allowedOrigins:
  - "https://your-domain.com"
  - "https://www.your-domain.com"
```

### 3. 安全响应头

**已添加的安全头：**

| 响应头 | 值 | 作用 |
|--------|-----|------|
| `X-Frame-Options` | `DENY` | 防止点击劫持 |
| `X-XSS-Protection` | `1; mode=block` | 启用 XSS 防护 |
| `X-Content-Type-Options` | `nosniff` | 防止 MIME 类型嗅探 |
| `Content-Security-Policy` | `default-src 'self'` | 内容安全策略 |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | 控制 Referrer 信息 |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=()` | 限制浏览器功能 |

### 4. 请求体大小限制

```yaml
server:
  http:
    max-header-size: 8KB
    max-initial-line-length: 4KB
```

## 密码安全

### 强度要求

```java
// 密码必须满足：
- 至少 6 个字符
- 包含字母和数字
```

### 加密存储

```java
// 使用 BCrypt 加密
password_hash = BCrypt.hash(password)
```

## 限流配置

### Redis 滑动窗口限流

**配置位置：**
- `risk-control-service/src/main/java/com/vapor/riskcontrol/service/RedisSlidingWindowRateLimiter.java`

**Lua 脚本保证原子性：**
```lua
-- 1. 移除窗口外的旧记录
redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart)

-- 2. 统计当前窗口内的记录数
local count = redis.call('ZCARD', key)

-- 3. 如果未超过限制，添加新记录
if count < limit then
    redis.call('ZADD', key, now, now .. '-' .. math.random(1, 1000000))
    redis.call('EXPIRE', key, math.ceil(windowMs / 1000) + 1)
    return {1, limit - count - 1}
else
    return {0, 0}
end
```

### 限流策略建议

| 接口类型 | 窗口大小 | 限制次数 |
|----------|----------|----------|
| 登录 | 1 分钟 | 5 次 |
| 注册 | 1 小时 | 3 次 |
| 发布评价 | 1 分钟 | 10 次 |
| 点赞 | 1 分钟 | 30 次 |
| 一般查询 | 1 秒 | 100 次 |

## 内容安全

### 敏感词过滤

**配置位置：**
```yaml
risk:
  keywords: spam，广告，恶意词汇
```

**使用方式：**
```java
RiskAuditResult result = riskControlAppService.auditContent(content);
if (!result.passed()) {
    throw new BizException(ErrorCode.CONTENT_VIOLATION);
}
```

## 生产环境安全检查清单

### 认证与授权
- [ ] JWT_SECRET 已更改为强随机字符串
- [ ] JWT 过期时间设置合理（建议 24 小时）
- [ ] 管理接口有独立的认证机制
- [ ] 敏感操作需要二次验证

### 网络安全
- [ ] CORS 配置限制为特定域名
- [ ] 启用 HTTPS
- [ ] 配置防火墙规则
- [ ] 隐藏服务器版本信息

### 数据安全
- [ ] 数据库连接使用加密
- [ ] 敏感数据脱敏显示
- [ ] 定期备份数据
- [ ] 启用 SQL 审计日志

### 应用安全
- [ ] 更新所有依赖到最新版本
- [ ] 移除调试端点
- [ ] 配置 Actuator 安全认证
- [ ] 启用安全日志审计

### Actuator 安全配置

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

## 安全测试

### OWASP Top 10 防护

| 风险 | 防护措施 |
|------|----------|
| 注入 | 使用参数化查询，MyBatis-Plus 自动防护 |
| 失效的身份认证 | JWT 认证 + 密码强度校验 |
| 敏感信息泄露 | 安全响应头 + 数据脱敏 |
| XML 外部实体 | 不使用 XML 解析 |
| 失效的访问控制 | 网关层统一鉴权 |
| 安全配置错误 | 安全检查清单 |
| 跨站脚本 (XSS) | Content-Security-Policy + X-XSS-Protection |
| 不安全的反序列化 | 不使用 Java 原生序列化 |
| 使用有漏洞的组件 | 定期更新依赖 |
| 不足的日志和监控 | Actuator + Prometheus 监控 |

## 安全事件响应

### 日志审计

```bash
# 查看认证失败日志
grep "鉴权失败" logs/application.log

# 查看限流触发日志
grep "限流拦截" logs/application.log

# 查看敏感词拦截日志
grep "内容审核拦截" logs/application.log
```

### 监控告警

```yaml
# Prometheus 告警规则
groups:
  - name: security
    rules:
      - alert: HighAuthFailureRate
        expr: rate(auth_failures_total[5m]) > 10
        annotations:
          summary: "认证失败率过高"

      - alert: RateLimitTriggered
        expr: rate_limit_triggered_total > 100
        annotations:
          summary: "限流触发频繁"
```

## 安全依赖更新

定期检查并更新安全相关依赖：

```bash
# 检查依赖漏洞
mvn org.owasp:dependency-check-maven:check

# 更新依赖版本
mvn versions:display-dependency-updates
```

## 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security 文档](https://spring.io/projects/spring-security)
- [CWE/SANS Top 25](https://cwe.mitre.org/top25/)
