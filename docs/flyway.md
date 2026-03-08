# Flyway 数据库迁移配置

本文档介绍 Campus Review 项目的数据库迁移配置和使用方法。

## 概述

项目使用 **Flyway** 进行数据库版本管理，支持：
- 自动迁移
- 版本控制
- 回滚支持（通过 undo 文件）
- 多环境配置

## 目录结构

```
src/main/resources/db/migration/
├── V1_0_0__initial_schema.sql      # 初始 schema
├── V1_0_1__add_indexes.sql         # 添加索引
├── V1_1_0__add_columns.sql         # 添加字段
└── R__view_summary.sql             # 重复迁移（视图等）
```

## 命名规范

### 版本迁移文件

格式：`V<major>_<minor>_<patch>__description.sql`

示例：
- `V1_0_0__initial_schema.sql` - 初始版本
- `V1_0_1__add_indexes.sql` - 小修正
- `V1_1_0__add_user_profile.sql` - 新功能

### 重复迁移文件

格式：`R__description.sql`

用于视图、存储过程等需要重复执行的对象。

## 配置说明

### 开发环境（H2）

```yaml
spring:
  flyway:
    enabled: false  # 开发使用 sql.init
    locations: classpath:db/migration
```

### 生产环境（MySQL）

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    clean-on-validation-error: false
```

## 使用方式

### 本地开发

```bash
# 运行 Flyway 迁移
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 清理数据库（慎用！）
mvn flyway:clean

# 基准化（现有数据库）
mvn flyway:baseline
```

### CI/CD 集成

```yaml
# GitHub Actions 示例
- name: Run Flyway Migration
  run: mvn flyway:migrate -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
```

## 迁移脚本编写规范

### 1. 幂等性

确保脚本可以重复执行：

```sql
-- ✅ 推荐：使用 CREATE TABLE IF NOT EXISTS
CREATE TABLE IF NOT EXISTS users (...);

-- ✅ 推荐：使用 ALTER TABLE ADD COLUMN IF NOT EXISTS
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- ❌ 避免：直接 CREATE TABLE
CREATE TABLE users (...);  -- 重复执行会失败
```

### 2. 事务控制

```sql
-- Flyway 默认自动开启事务
-- 对于不能事务内的操作（如 CREATE INDEX CONCURRENTLY）

-- MySQL
SET autocommit=1;

-- PostgreSQL
ALTER TABLE users OWNER TO app;  -- 不能事务内执行
```

### 3. 回滚脚本

为每个迁移创建对应的回滚脚本（可选）：

```
V1_0_0__initial_schema.sql
U1_0_0__initial_schema.sql  -- 回滚脚本
```

## 迁移历史表

Flyway 自动创建 `flyway_schema_history` 表：

```sql
DESC flyway_schema_history;
-- installed_rank: 安装顺序
-- version: 版本号
-- description: 描述
-- type: 类型（SQL/JDBC）
-- script: 脚本文件名
-- checksum: 校验和
-- success: 是否成功
```

## 常见问题

### 1. 迁移失败

检查错误日志：
```bash
grep "Flyway" logs/application.log
```

查看迁移历史：
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
```

### 2. 校验和错误

当脚本内容变更时：
```sql
-- 更新校验和
UPDATE flyway_schema_history SET checksum = NULL WHERE version = '1.0.0';
```

### 3. 跳过特定版本

```yaml
spring:
  flyway:
    ignore-migration-patterns: "*:ignored"
```

## 最佳实践

1. **迁移脚本不可变** - 已发布的脚本不要修改
2. **小步快跑** - 每次迁移只做一件事
3. **测试迁移** - 在测试环境验证后再部署
4. **备份数据** - 重大变更前备份数据
5. **记录变更** - 在提交信息中说明变更内容

## 示例迁移

### 添加字段

```sql
-- V1_1_0__add_user_profile.sql
ALTER TABLE users
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN bio TEXT,
    ADD COLUMN last_login_at TIMESTAMP;

CREATE INDEX idx_users_phone ON users(phone);
```

### 添加数据

```sql
-- V1_1_1__add_admin_user.sql
INSERT INTO users (email, student_no, password_hash, nickname, roles)
VALUES ('admin@campus.edu', 'ADMIN001', '$2a$10$...', '管理员', 'ADMIN,USER');
```

### 创建视图

```sql
-- R__v_restaurant_summary.sql
CREATE OR REPLACE VIEW v_restaurant_summary AS
SELECT
    r.id,
    r.name,
    r.avg_rating,
    COUNT(rev.id) as review_count
FROM restaurants r
LEFT JOIN reviews rev ON r.id = rev.restaurant_id
WHERE rev.status = 'APPROVED'
GROUP BY r.id, r.name, r.avg_rating;
```
