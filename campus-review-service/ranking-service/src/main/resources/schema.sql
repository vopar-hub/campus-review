-- schema.sql for MySQL database
-- ranking-service 专用 schema

-- 热门餐馆排行榜表
CREATE TABLE IF NOT EXISTS hot_restaurant_rank
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT         NOT NULL UNIQUE,
    score         DECIMAL(10, 2) NOT NULL,
    `rank`        INT            NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_hot_rank ON hot_restaurant_rank (`rank`);