-- Campus Review 数据库初始化脚本
-- 创建数据库和用户

-- 创建用户数据库
CREATE DATABASE IF NOT EXISTS campus_restaurant
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建餐厅数据库
CREATE DATABASE IF NOT EXISTS campus_user
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建评价数据库
CREATE DATABASE IF NOT EXISTS campus_review
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;




create table campus_review.favorites
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                              not null,
    target_type varchar(50)                         not null,
    target_id   bigint                              not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_user_favorite
        unique (user_id, target_type, target_id)
);

create index idx_favorites_user_target
    on campus_review.favorites (user_id, target_type, target_id);

create table campus_user.flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on campus_user.flyway_schema_history (success);

create table campus_restaurant.hot_restaurant_rank
(
    id            bigint auto_increment
        primary key,
    restaurant_id bigint                                  not null,
    score         decimal(10, 2)                          not null,
    `rank`        int                                     not null,
    updated_at    timestamp     default CURRENT_TIMESTAMP null,
    avg_rating    decimal(3, 2) default 0.00              null comment '平均评分',
    constraint restaurant_id
        unique (restaurant_id)
);

create index idx_hot_rank
    on campus_restaurant.hot_restaurant_rank (`rank`);

create table campus_review.likes
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                              not null,
    target_type varchar(50)                         not null,
    target_id   bigint                              not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_user_target
        unique (user_id, target_type, target_id)
);

create index idx_likes_user_target
    on campus_review.likes (user_id, target_type, target_id);

create table campus_user.messages
(
    id         bigint auto_increment
        primary key,
    to_user_id bigint                                not null,
    title      varchar(255)                          not null,
    content    text                                  null,
    type       varchar(50) default 'SYSTEM'          null,
    read_flag  tinyint(1)  default 0                 null,
    read_at    timestamp                             null,
    created_at timestamp   default CURRENT_TIMESTAMP null
);

create index idx_messages_to_user
    on campus_user.messages (to_user_id, read_flag);

create table campus_restaurant.restaurants
(
    id              bigint auto_increment
        primary key,
    name            varchar(255)                            not null,
    campus          varchar(100)                            null,
    address         varchar(500)                            null,
    description     text                                    null,
    cover_image_url varchar(500)                            null,
    avg_rating      decimal(3, 2) default 0.00              null,
    total_reviews   int           default 0                 null,
    status          varchar(20)   default 'ACTIVE'          null,
    created_at      timestamp     default CURRENT_TIMESTAMP null,
    updated_at      timestamp     default CURRENT_TIMESTAMP null
);

create index idx_restaurants_campus
    on campus_restaurant.restaurants (campus);

create index idx_restaurants_status
    on campus_restaurant.restaurants (status);

create table campus_review.reviews
(
    id            bigint auto_increment
        primary key,
    restaurant_id bigint                                not null,
    user_id       bigint                                not null,
    rating        int                                   not null,
    content       text                                  null,
    status        varchar(20) default 'PENDING'         null,
    audit_reason  varchar(500)                          null,
    audited_at    timestamp                             null,
    created_at    timestamp   default CURRENT_TIMESTAMP null,
    updated_at    timestamp   default CURRENT_TIMESTAMP null,
    check ((`rating` >= 1) and (`rating` <= 5))
);

create index idx_reviews_restaurant_status
    on campus_review.reviews (restaurant_id, status);

create index idx_reviews_status
    on campus_review.reviews (status);

create index idx_reviews_user_id
    on campus_review.reviews (user_id);

create table campus_user.users
(
    id            bigint auto_increment
        primary key,
    email         varchar(255)                           not null,
    student_no    varchar(50)                            not null,
    password_hash varchar(255)                           not null,
    nickname      varchar(100)                           null,
    avatar_url    varchar(500)                           null,
    roles         varchar(255) default 'USER'            null,
    banned        tinyint(1)   default 0                 null,
    ban_reason    varchar(500)                           null,
    ban_until     timestamp                              null,
    created_at    timestamp    default CURRENT_TIMESTAMP null,
    updated_at    timestamp    default CURRENT_TIMESTAMP null,
    constraint email
        unique (email),
    constraint student_no
        unique (student_no)
);

create index idx_users_email
    on campus_user.users (email);

create index idx_users_student_no
    on campus_user.users (student_no);




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
