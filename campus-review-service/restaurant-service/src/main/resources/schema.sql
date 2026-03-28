-- schema.sql for MySQL database
-- restaurant-service 专用 schema

-- 餐馆表
CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    campus VARCHAR(100),
    address VARCHAR(500),
    description TEXT,
    cover_image_url VARCHAR(500),
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_restaurants_campus ON restaurants(campus);
CREATE INDEX idx_restaurants_status ON restaurants(status);

-- 热门餐馆排行榜表（从 ranking-service 合并）
CREATE TABLE IF NOT EXISTS hot_restaurant_rank
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT         NOT NULL UNIQUE,
    score         DECIMAL(10, 2) NOT NULL,
    avg_rating    DECIMAL(3, 2),
    `rank`        INT            NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_hot_rank ON hot_restaurant_rank (`rank`);
