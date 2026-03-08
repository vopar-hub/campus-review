# 日志配置指南

本文档介绍 Campus Review 项目的日志配置和使用规范。

## 日志框架

项目使用 **Logback** 作为日志框架，通过 `logback-spring.xml` 进行配置。

## 日志配置

### 配置文件位置

```
campus-review-common/src/main/resources/logback-spring.xml
```

所有服务继承此通用配置，也可在各自服务中覆盖。

### 日志输出

#### 1. 控制台输出

- 彩色输出，便于本地开发调试
- 包含请求 ID 追踪

**示例输出：**
```
2026-03-08 20:00:00.000 [main] INFO  com.vapor.user.service.UserAccountService - [req-123] 用户注册成功
```

#### 2. 文件输出

| 文件 | 级别 | 保留期 | 大小限制 |
|------|------|--------|----------|
| `{app}-info.log` | INFO | 30 天 | 10GB |
| `{app}-error.log` | ERROR | 90 天 | 5GB |

**滚动策略：**
- 按天滚动
- 单个文件超过 100MB 时拆分

### 异步日志

使用 `AsyncAppender` 提高性能：
- 队列大小：512
- 无丢弃阈值

## 日志级别使用规范

| 级别 | 使用场景 | 示例 |
|------|----------|------|
| ERROR | 系统错误、异常捕获 | 数据库连接失败、外部服务调用失败 |
| WARN | 警告信息、不影响业务流程 | 参数校验失败、缓存未命中 |
| INFO | 关键业务流程节点 | 用户登录成功、订单创建成功 |
| DEBUG | 调试信息、详细参数 | 方法入参出参、SQL 执行详情 |
| TRACE | 详细追踪信息 | 循环迭代、条件判断 |

## 敏感信息脱敏

### 自动脱敏类型

| 类型 | 脱敏规则 | 示例 |
|------|----------|------|
| 手机号 | 138****5678 | 保留前 3 后 4 位 |
| 邮箱 | t**t@example.com | 保留首尾 |
| 身份证 | 110101********1234 | 保留前 6 后 4 位 |
| 密码 | *** | 全部隐藏 |
| JWT Token | Bearer *** | 全部隐藏 |

### 使用 LogUtils 工具类

```java
import com.vapor.common.util.LogUtils;

// 手动脱敏
String maskedPhone = LogUtils.maskSensitive("用户手机号：13812345678");

// 脱敏记录日志
LogUtils.info(log, "用户登录：phone={}, ip={}", userPhone, userIp);

// 格式化输出
String formattedUserId = LogUtils.formatUserId(12345L);  // ****2345
String formattedIp = LogUtils.formatIp("192.168.1.100");  // 192.168.*.*
```

### MDC 脱敏

在过滤器中添加 MDC 上下文时脱敏：

```java
MDCTools.addUserId(String.valueOf(claims.getUserId()));
MDCTools.addUserIp(LogUtils.formatIp(clientIp));
```

## 请求追踪

### 请求 ID 生成

每个请求自动生成唯一 ID：
- 格式：`req-{uuid}`
- 通过 `X-Request-ID` 头传递
- 记录到 MDC 和日志中

### 日志追踪示例

```
2026-03-08 20:00:00.000 [http-nio-8101-exec-1] INFO  c.v.user.controller.AuthController - [req-abc123] 开始处理登录请求
2026-03-08 20:00:00.100 [http-nio-8101-exec-1] INFO  c.v.user.service.UserAccountService - [req-abc123] 查询用户信息
2026-03-08 20:00:00.200 [http-nio-8101-exec-1] INFO  c.v.user.controller.AuthController - [req-abc123] 登录成功
```

## 日志分析

### 日志格式

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [%X{requestId:-}] %msg%n
```

**字段说明：**
- `%d`: 时间戳
- `[%thread]`: 线程名
- `%-5level`: 日志级别
- `%logger{36}`: 类名（最多 36 字符）
- `[%X{requestId:-}]`: 请求 ID（从 MDC 获取）
- `%msg`: 日志消息

### 常用日志分析命令

```bash
# 查看 ERROR 日志
grep -r "ERROR" logs/*-error.log

# 查看特定请求的完整链路
grep "req-abc123" logs/*-info.log

# 查看慢请求（执行时间>1s）
grep "Timing" logs/*-info.log | awk '$NF > 1000'

# 统计各服务的日志量
wc -l logs/*-info.log

# 查看最近 5 分钟的 ERROR
find logs -name "*.log" -mmin -5 -exec grep "ERROR" {} \;
```

### ELK Stack 集成

**Logstash 配置示例：**
```
input {
  file {
    path => "/path/to/logs/*-info.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - \[%{DATA:requestId}\] %{GREEDYDATA:log_message}" }
  }
  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "campus-review-%{+YYYY.MM.dd}"
  }
}
```

## 性能优化

### 异步日志配置

```xml
<appender name="ASYNC_FILE_INFO" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE_INFO"/>
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
</appender>
```

### 避免的性能问题

1. **避免在 DEBUG 级别拼接字符串**
   ```java
   // ❌ 不推荐
   log.debug("用户信息：" + user.toString());

   // ✅ 推荐
   log.debug("用户信息：{}", user);
   ```

2. **避免频繁创建 Logger**
   ```java
   // ✅ 推荐
   private static final Logger log = LoggerFactory.getLogger(MyClass.class);
   ```

3. **合理使用条件判断**
   ```java
   // ✅ 推荐
   if (log.isDebugEnabled()) {
       log.debug("调试信息：{}", expensiveOperation());
   }
   ```

## 故障排查

### 日志不输出

检查配置：
```bash
# 检查日志级别
grep "<root level" logback-spring.xml

# 检查文件权限
ls -la logs/
```

### 日志文件过大

调整滚动策略：
```xml
<maxFileSize>50MB</maxFileSize>
<maxHistory>15</maxHistory>
```

### 丢失请求 ID 追踪

检查 MDC 清理：
```java
try {
    MDCTools.addRequestId(requestId);
    // 业务逻辑
} finally {
    MDC.clear();  // 确保清理
}
```

## 最佳实践

1. **使用占位符而非字符串拼接**
2. **记录关键业务节点**
3. **异常必须记录堆栈**
4. **敏感信息必须脱敏**
5. **生产环境关闭 DEBUG**
6. **定期清理日志文件**
7. **重要操作记录操作人和时间**
