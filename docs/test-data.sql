-- 校园美食点评平台 - 测试数据初始化脚本
-- 用于本地开发环境快速填充测试数据
-- 与 Flyway V1_0_0__initial_schema.sql 保持一致

-- 插入测试用户
INSERT INTO users (email, student_no, password_hash, nickname, avatar_url, roles, banned, ban_reason, ban_until, created_at, updated_at)
VALUES
    ('admin@campus.edu', 'A0001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTfZCrjtqaqDqPbCJYzMiXQqOKe', '管理员', NULL, 'ADMIN,USER', FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user1@campus.edu', '20250001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTfZCrjtqaqDqPbCJYzMiXQqOKe', '张三', NULL, 'USER', FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user2@campus.edu', '20250002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTfZCrjtqaqDqPbCJYzMiXQqOKe', '李四', NULL, 'USER', FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user3@campus.edu', '20250003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTfZCrjtqaqDqPbCJYzMiXQqOKe', '王五', NULL, 'USER', FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试餐馆
INSERT INTO restaurants (name, campus, address, description, cover_image_url, images, avg_rating, total_reviews, status, created_at, updated_at)
VALUES
    ('香园餐厅', '主校区', '学生服务中心一楼', '提供各类家常菜品，价格实惠', 'http://example.com/cover1.jpg', NULL, 4.50, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('麦当劳', '主校区', '学生服务中心二楼', '国际连锁快餐品牌', 'http://example.com/cover2.jpg', NULL, 4.00, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('川香阁', '主校区', '校园商业街 1 号', '正宗川菜，麻辣鲜香', 'http://example.com/cover3.jpg', NULL, 4.00, 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('粤味轩', '东校区', '食堂二楼', '清淡粤菜，原汁原味', 'http://example.com/cover4.jpg', NULL, 5.00, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('西北风味', '西校区', '美食广场 A 区', '兰州拉面、肉夹馍等地道西北美食', 'http://example.com/cover5.jpg', NULL, 4.00, 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试评价
INSERT INTO reviews (restaurant_id, user_id, rating, content, images, status, audit_reason, audited_at, created_at, updated_at)
VALUES
    (1, 2, 5, '味道很好，价格实惠，经常来吃！', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, 3, 4, '分量足，口味不错，就是人有点多', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 4, '标准化出品，品质稳定', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 2, 5, '水煮鱼很正宗，强烈推荐！', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, 3, '辣度可以接受，但感觉不够入味', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 2, 5, '早茶很地道，虾饺烧卖都很好吃', NULL, 'APPROVED', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 3, 4, '拉面劲道，羊肉泡馍很香', NULL, 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试点赞
INSERT INTO likes (user_id, target_type, target_id, created_at)
VALUES
    (2, 'review', 1, CURRENT_TIMESTAMP),
    (3, 'review', 1, CURRENT_TIMESTAMP),
    (4, 'review', 2, CURRENT_TIMESTAMP),
    (2, 'review', 3, CURRENT_TIMESTAMP),
    (4, 'review', 4, CURRENT_TIMESTAMP),
    (2, 'restaurant', 1, CURRENT_TIMESTAMP),
    (3, 'restaurant', 3, CURRENT_TIMESTAMP);

-- 插入测试收藏
INSERT INTO favorites (user_id, target_type, target_id, created_at)
VALUES
    (2, 'restaurant', 1, CURRENT_TIMESTAMP),
    (2, 'restaurant', 3, CURRENT_TIMESTAMP),
    (3, 'restaurant', 1, CURRENT_TIMESTAMP),
    (4, 'restaurant', 4, CURRENT_TIMESTAMP);

-- 插入测试消息
INSERT INTO messages (to_user_id, title, content, type, read_flag, read_at, created_at)
VALUES
    (2, '系统通知', '欢迎使用校园美食点评平台！', 'SYSTEM', FALSE, NULL, CURRENT_TIMESTAMP),
    (3, '评价审核通知', '您发布的评价已通过审核', 'SYSTEM', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '活动通知', '本月美食节即将开始，敬请期待！', 'NOTIFICATION', FALSE, NULL, CURRENT_TIMESTAMP);

-- 插入排行榜数据（初始）
INSERT INTO hot_restaurant_rank (restaurant_id, score, rank, updated_at)
VALUES
    (1, 95.50, 1, CURRENT_TIMESTAMP),
    (3, 88.00, 2, CURRENT_TIMESTAMP),
    (4, 82.50, 3, CURRENT_TIMESTAMP),
    (2, 75.00, 4, CURRENT_TIMESTAMP),
    (5, 68.50, 5, CURRENT_TIMESTAMP);

-- 查询统计
SELECT '=== 测试数据汇总 ===' AS info;
SELECT '用户数：' || COUNT(*) AS count FROM users;
SELECT '餐馆数：' || COUNT(*) AS count FROM restaurants;
SELECT '评价数：' || COUNT(*) AS count FROM reviews;
SELECT '点赞数：' || COUNT(*) AS count FROM likes;
SELECT '收藏数：' || COUNT(*) AS count FROM favorites;
SELECT '消息数：' || COUNT(*) AS count FROM messages;
SELECT '排行榜记录数：' || COUNT(*) AS count FROM hot_restaurant_rank;
