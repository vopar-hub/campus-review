-- schema.sql for H2 development database
-- ranking-service 专用 schema

-- 热门餐馆排行榜表
CREATE TABLE IF NOT EXISTS hot_restaurant_rank (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL UNIQUE,
    score DECIMAL(10,2) NOT NULL,
    rank INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hot_rank ON hot_restaurant_rank(rank);
