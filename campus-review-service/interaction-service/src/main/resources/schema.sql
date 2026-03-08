-- schema.sql for H2 development database
-- interaction-service 专用 schema

-- 点赞表
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_target ON likes(user_id, target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_likes_user_target ON likes(user_id, target_type, target_id);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_favorite ON favorites(user_id, target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user_target ON favorites(user_id, target_type, target_id);
