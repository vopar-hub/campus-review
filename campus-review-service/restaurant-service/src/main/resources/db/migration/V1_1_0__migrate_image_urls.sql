-- 数据迁移：将完整 URL 转换为对象路径
-- 执行前请先备份数据库！
-- 执行时间：2026-04-03

-- 备份表（可选，建议执行）
CREATE TABLE IF NOT EXISTS restaurant_backup_20260403 AS
SELECT * FROM restaurant;

-- 更新 cover_image_url 字段
-- 将完整 URL 转换为对象路径
UPDATE restaurant
SET cover_image_url = TRIM(BOTH '/' FROM
    REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(
                    REPLACE(cover_image_url, 'http://localhost:9000/', ''),
                    'http://localhost:9000', ''
                ),
                CONCAT('http://', :server_domain, '/minio/'), ''
            ),
            CONCAT('http://', :server_domain, ':9000/'), ''
        ),
        CONCAT('http://', :server_domain, ':9000'), ''
    )
)
WHERE cover_image_url IS NOT NULL
  AND cover_image_url != ''
  AND cover_image_url LIKE 'http%';

-- 验证迁移结果
SELECT
    id,
    name,
    cover_image_url,
    CASE
        WHEN cover_image_url LIKE 'http%' THEN '❌ 未转换'
        WHEN cover_image_url LIKE '%/%' THEN '✅ 已转换'
        ELSE '⚠️ 未知格式'
    END AS migration_status
FROM restaurant
WHERE cover_image_url IS NOT NULL
  AND cover_image_url != ''
ORDER BY id DESC
LIMIT 10;

-- 说明：
-- 1. :server_domain 是参数，执行时需要替换为实际的服务器域名或IP
-- 2. 例如：SET @server_domain = '192.168.1.100';
-- 3. 迁移后的格式示例：restaurants/2024/04/03/abc123.jpg
-- 4. 如果需要回滚，可以使用 restaurant_backup_20260403 表
