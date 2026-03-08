# AGENTS.md

面向自动化代码助手/智能体的仓库协作说明：如何理解工程结构、如何本地启动与验证改动、以及本仓库的常见约定。

## 技术栈与约束

- Java: 17
- 构建工具: Maven（多模块聚合工程，根工程 `packaging=pom`）
- 框架: Spring Boot 3.5.11
- 网关: Spring Cloud Gateway（Reactive）
- 注册/配置中心: Nacos（本地单机默认关闭）
- ORM: MyBatis-Plus
- 认证: JWT（`io.jsonwebtoken:jjwt`）
- 本地默认数据库: H2 内存库（各服务按需初始化 `schema.sql`）

## 目录结构速览

- `campus-review-common`: 通用基础设施（统一响应体、错误码、全局异常处理、请求链路 ID、用户上下文等）
- `campus-review-model`: 跨服务 DTO/请求响应模型（建议所有对外接口 DTO 都放这里）
- `campus-review-utils`: 工具组件（JWT、校验器等）
- `campus-review-service`: 微服务聚合模块（每个子模块是独立可运行 Spring Boot 应用）
  - `user-service`、`restaurant-service`、`review-service`、`interaction-service`、`ranking-service`、`notification-service`、`risk-control-service`、`admin-service`
- `campus-review-gateway`: 网关聚合模块
  - `campus-review-user-gateway`: 用户侧网关（对外入口，路由到各业务服务）
  - `campus-review-admin-gateway`: 管理侧网关（对外入口，路由到 `admin-service`）
- `docs`: 项目文档（建议优先阅读 `quick-start.md` / `api.md` / `tech-solution.md`）

## 本地启动（建议流程）

### 端口约定

- user-service: 8101
- restaurant-service: 8102
- review-service: 8103
- interaction-service: 8104
- ranking-service: 8105
- notification-service: 8106
- risk-control-service: 8107
- admin-service: 8108
- user-gateway: 8001
- admin-gateway: 8002

### 关键配置

- 本地单机默认关闭 Nacos（避免依赖外部组件）
  - 各服务/网关的 `application.yml` 一般包含：
    - `spring.cloud.nacos.config.enabled=false`
    - `spring.cloud.nacos.discovery.enabled=false`
- Spring Cloud/Boot 兼容检查默认关闭（便于本地直接跑）
  - `spring.cloud.compatibility-verifier.enabled=false`
- JWT 密钥需保持一致（否则网关验签失败）
  - `user-service`、`user-gateway`、`admin-gateway` 的 `security.jwt.secret` 需一致

### 启动顺序（推荐）

1. 先启动核心服务：`user-service` / `restaurant-service` / `review-service` / `interaction-service`
2. 再启动依赖服务：`notification-service` / `risk-control-service` / `ranking-service` / `admin-service`
3. 最后启动网关：`user-gateway` / `admin-gateway`

### 如何启动

- 推荐在 IDE 中直接运行各模块的 `*Application` 启动类（每个服务/网关都是独立 Spring Boot 应用）。
- 或使用 Maven：
  - 单服务启动：`mvn -pl campus-review-service/user-service -am spring-boot:run`
  - 网关启动：`mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run`

## 验证方式（改动后必做）

### 单元/集成测试

- 全量测试（慢）：`mvn test`
- 只跑某个服务测试：
  - `mvn -pl campus-review-service/user-service -am test`
  - `mvn -pl campus-review-service/review-service -am test`
  - `mvn -pl campus-review-service/risk-control-service -am test`

### 冒烟验证（HTTP）

参见 [quick-start.md](file:///e:/_CODE_/campus-review/docs/quick-start.md) 的 curl 示例，通过 `user-gateway:8001` 调用注册/登录、创建餐馆、发布评价、点赞、查询榜单等接口。

## 代码与接口约定

- 包名统一使用 `com.vapor.*`。
- Controller 对外返回统一封装：
  - `ApiResponse.ok(data)` / `ApiResponse.fail(...)`（位于 `campus-review-common`）
- 全局异常处理在 `campus-review-common`，新增业务异常优先复用既有 `BizException`/`ErrorCode`。
- 跨服务 DTO/请求响应对象优先放在 `campus-review-model`，避免在服务模块之间相互依赖实体类。
- 新增对外 API 时注意同步网关路由：
  - 用户侧入口通常经 `campus-review-user-gateway` 路由到具体服务
  - 管理侧入口经 `campus-review-admin-gateway` 路由到 `admin-service`

## 安全与配置注意事项

- 不要把任何真实密钥/Token/个人信息写入代码或提交到仓库；本地仅使用开发用 `security.jwt.secret`。
- 不要在日志中打印 JWT、密码等敏感字段。
- 需要更换端口或路由时，先改 `application.yml`，再更新文档与用例。

