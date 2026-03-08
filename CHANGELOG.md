# 更新日志

本文档记录项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- Docker 容器化支持（10 个 Dockerfile + docker-compose.yml）
- GitHub Actions CI/CD 流水线
- 密码强度校验注解
- 健康检查端点（/api/health）
- 请求日志过滤器
- OpenAPI/Swagger 文档集成
- Redis 分布式缓存支持
- Redis 滑动窗口限流器
- Spring Boot Actuator 监控集成
- Prometheus 指标导出
- 部署指南文档
- 性能优化指南文档
- 安全配置指南文档
- 日志配置指南文档
- Flyway 数据库迁移指南
- 结构化日志配置（Logback）
- 敏感信息脱敏工具类
- Flyway 数据库版本管理
- Nacos 服务发现配置指南
- 通用模块单元测试（LogUtils、ApiResponse、ErrorCode 等）
- JWT 工具类单元测试
- Flyway 依赖集成（user-service）
- docker-compose 基础设施（Nacos、Redis、MySQL）
- UserContextUtil 单元测试

### 改进
- 添加数据库复合索引优化查询性能
- 排行榜接口添加 Redis 缓存支持（5 分钟 TTL）
- 全局异常处理增强日志记录
- 所有 Controller 添加 OpenAPI 注解
- 限流器升级为 Redis 分布式实现（支持降级）
- 修复 YAML 配置重复块问题
- 网关层添加 CORS 配置
- 网关层添加安全响应头（XSS、点击劫持防护等）
- 添加请求体大小限制
- 日志滚动策略优化（按天滚动 + 大小限制）
- 添加异步日志提升性能
- 网关和服务启用 Nacos 服务发现
- 服务间调用改用负载均衡（lb://）前缀
- JWT 密钥外部化配置（环境变量）
- 清理空模块（feign-api、basic、test）
- PasswordValidator 添加静态工具方法
- 添加 risk-control-service 网关路由（/api/risk/**）
- Gateway 添加 HTTP 客户端超时配置
- 统一各服务 schema.sql 与 Flyway 迁移脚本
- 更新 test-data.sql 测试数据脚本
- 为所有服务添加 SpringDoc OpenAPI 和 Actuator 配置
- 为所有使用数据库的服务添加 H2 控制台配置
- 清理 admin-service 不需要的依赖（MyBatis-Plus、H2、Redis、JWT）
- 更新快速启动文档，添加详细的启动说明和 API 示例

### 修复
- 修复部分服务缺少数据库配置问题
- 修复 risk-control-service 配置解析错误

## [1.0.0] - 2026-03-08

### 新增
- 用户服务（注册、登录、个人信息）
- 餐馆服务（创建、查询、搜索）
- 评价服务（发布、审核、查询）
- 互动服务（点赞、收藏）
- 排行榜服务（热门餐馆榜单）
- 通知服务（站内消息）
- 风控服务（内容审核、限流）
- 管理服务（后台审核、用户管理）
- 用户网关（端口 8001）
- 管理网关（端口 8002）

### 技术栈
- Java 17
- Spring Boot 3.5.11
- Spring Cloud 2023.0.5
- MyBatis-Plus 3.5.5
- H2 内存数据库
- JWT 认证

---

## 版本说明

### 语义化版本规则

- **主版本号（Major）**：不兼容的 API 变更
- **次版本号（Minor）**：向后兼容的功能新增
- **修订号（Patch）**：向后兼容的问题修复

### 发布流程

1. 更新本更新日志
2. 更新 pom.xml 版本号
3. 创建 Git 标签
4. 发布到 Maven 仓库（如适用）
