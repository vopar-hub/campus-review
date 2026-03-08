# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Campus Review 是一个基于 Spring Boot 3.x 的校园美食点评平台微服务项目，采用 Maven 多模块聚合结构。

## 技术栈

- **Java**: 17
- **构建工具**: Maven（多模块聚合工程，根工程 `packaging=pom`）
- **框架**: Spring Boot 3.5.11
- **网关**: Spring Cloud Gateway（Reactive）
- **注册/配置中心**: Nacos（本地单机默认关闭）
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: JWT（`io.jsonwebtoken:jjwt`）
- **数据库**: H2 内存库（本地开发）/ 支持 MySQL

## 模块结构

- `campus-review-common`: 通用基础设施（统一响应体、错误码、全局异常处理）
- `campus-review-model`: 跨服务 DTO/请求响应模型
- `campus-review-utils`: 工具组件（JWT 工具、校验器等）
- `campus-review-feign-api`: Feign 客户端接口
- `campus-review-service`: 微服务聚合模块
  - `user-service` (8101), `restaurant-service` (8102), `review-service` (8103)
  - `interaction-service` (8104), `ranking-service` (8105), `notification-service` (8106)
  - `risk-control-service` (8107), `admin-service` (8108)
- `campus-review-gateway`: 网关聚合模块
  - `campus-review-user-gateway` (8001): 用户侧入口
  - `campus-review-admin-gateway` (8002): 管理侧入口

## 常用命令

```bash
# 全量构建
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 运行单个服务
mvn -pl campus-review-service/user-service -am spring-boot:run

# 运行用户网关
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run

# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
mvn -pl campus-review-service/review-service -am test
mvn -pl campus-review-service/risk-control-service -am test
```

## 启动顺序（推荐）

1. 核心服务：`user-service` → `restaurant-service` → `review-service` → `interaction-service`
2. 依赖服务：`notification-service` → `risk-control-service` → `ranking-service` → `admin-service`
3. 网关：`user-gateway` → `admin-gateway`

## 关键配置

- 本地开发默认关闭 Nacos，使用 H2 内存数据库
- JWT 密钥需在 `user-service`、`user-gateway`、`admin-gateway` 中保持一致
- 包名统一使用 `com.vapor.*`

## 代码约定

- Controller 对外返回统一使用 `ApiResponse<T>` 包装
- 业务异常优先复用 `BizException` + `ErrorCode`
- 跨服务 DTO/请求响应对象放在 `campus-review-model` 模块
- 用户侧入口经 `campus-review-user-gateway` (8001) 路由到具体服务
- 管理侧入口经 `campus-review-admin-gateway` (8002) 路由到 `admin-service`

## 文档索引

- `docs/quick-start.md`: 快速启动指南和 API 示例
- `docs/api.md`: 完整接口文档
- `docs/tech-solution.md`: 技术方案与微服务拆分说明
- `AGENTS.md`: 面向自动化代码助手的协作说明
