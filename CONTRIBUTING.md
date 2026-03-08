# 贡献指南

欢迎为 Campus Review 项目贡献代码！本文档将帮助您快速上手。

## 开发环境准备

### 必需工具

- JDK 17+
- Maven 3.8+
- Git
- IDE（推荐 IntelliJ IDEA 或 VS Code）

### 可选工具

- Docker（用于容器化测试）
- Postman（用于 API 测试）

## 本地启动

1. 克隆仓库
```bash
git clone https://github.com/your-org/campus-review.git
cd campus-review
```

2. 编译项目
```bash
mvn clean install -DskipTests
```

3. 启动服务
```bash
# 启动用户服务
mvn -pl campus-review-service/user-service -am spring-boot:run

# 启动用户网关
mvn -pl campus-review-gateway/campus-review-user-gateway -am spring-boot:run
```

## 代码规范

### 命名规范

- 类名：大驼峰命名（如 `UserService`）
- 方法名：小驼峰命名（如 `getUserById`）
- 常量：全大写下划线分隔（如 `MAX_RETRY_COUNT`）
- 包名：全小写（如 `com.vapor.user.service`）

### 代码风格

- 使用 4 个空格缩进
- 类/方法/字段必须添加 JavaDoc 注释
- 保持方法简洁（建议不超过 50 行）
- 一个类只做一件事（单一职责原则）

### 提交规范

提交消息格式：`<type>: <subject>`

**type 类型：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码风格调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

**示例：**
```
feat: 添加用户积分系统
fix: 修复排行榜缓存失效问题
docs: 更新 API 文档示例
refactor: 重构评价审核流程
```

## 提交流程

1. Fork 仓库
2. 创建功能分支
```bash
git checkout -b feature/your-feature-name
```

3. 提交更改
```bash
git add .
git commit -m "feat: 你的功能描述"
```

4. 推送到远程
```bash
git push origin feature/your-feature-name
```

5. 创建 Pull Request

## 测试要求

- 新功能必须包含单元测试
- 核心业务逻辑需有集成测试
- 确保所有测试通过后再提交 PR

运行测试：
```bash
# 全量测试
mvn test

# 单个服务测试
mvn -pl campus-review-service/user-service -am test
```

## 问题反馈

遇到问题？欢迎通过以下方式反馈：

- GitHub Issues: 提交 Bug 或功能建议
- 邮件：support@campus.edu

## 许可证

本项目采用 MIT 许可证。
