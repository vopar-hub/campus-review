-- Campus Review 数据库初始化脚本
-- 创建数据库和用户

-- 创建用户数据库
CREATE DATABASE IF NOT EXISTS campus_review_user
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建餐厅数据库
CREATE DATABASE IF NOT EXISTS campus_review_restaurant
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建评价数据库
CREATE DATABASE IF NOT EXISTS campus_review_review
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建应用用户（可选，生产环境建议使用独立用户）
-- CREATE USER IF NOT EXISTS 'campus_review'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON campus_review_*.* TO 'campus_review'@'localhost';
-- FLUSH PRIVILEGES;

-- 说明：
-- 1. 数据库表结构由 Flyway 自动创建，无需手动执行
-- 2. 启动微服务后，Flyway 会自动执行 db/migration/V1_0_0__initial_schema.sql
-- 3. 如需手动创建表，请参考各服务的 schema.sql 文件：
--    - user-service/src/main/resources/schema.sql
--    - restaurant-service/src/main/resources/schema.sql
--    - review-service/src/main/resources/schema.sql
