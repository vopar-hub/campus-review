-- schema.sql for H2 development database
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
);

CREATE INDEX IF NOT EXISTS idx_restaurants_campus ON restaurants(campus);
CREATE INDEX IF NOT EXISTS idx_restaurants_status ON restaurants(status);
