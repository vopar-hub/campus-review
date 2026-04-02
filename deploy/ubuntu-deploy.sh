#!/bin/bash

# Campus Review 自动部署脚本（Ubuntu 4G内存优化版）
# 使用方法: sudo bash deploy.sh

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否以 root 运行
if [ "$EUID" -ne 0 ]; then
    log_error "请使用 sudo 运行此脚本"
    exit 1
fi

# 配置变量
read -p "请输入 MySQL root 密码 [默认: CampusReview2026!]: " MYSQL_ROOT_PASSWORD
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-CampusReview2026!}

read -p "请输入应用数据库用户密码 [默认: CampusUser2026!]: " DB_USER_PASSWORD
DB_USER_PASSWORD=${DB_USER_PASSWORD:-CampusUser2026!}

read -p "请输入 Redis 密码 [默认: CampusRedis2026!]: " REDIS_PASSWORD
REDIS_PASSWORD=${REDIS_PASSWORD:-CampusRedis2026!}

read -p "请输入 MinIO 密钥 [默认: MinioAdmin2026!]: " MINIO_SECRET_KEY
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-MinioAdmin2026!}

read -p "请输入 JWT 密钥 [至少32个字符，默认: campus-review-production-jwt-secret-key-2026-minimum-32-chars]: " JWT_SECRET
JWT_SECRET=${JWT_SECRET:-campus-review-production-jwt-secret-key-2026-minimum-32-chars}

if [ ${#JWT_SECRET} -lt 32 ]; then
    log_error "JWT 密钥必须至少32个字符"
    exit 1
fi

read -p "请输入服务器域名或IP: " SERVER_DOMAIN
if [ -z "$SERVER_DOMAIN" ]; then
    log_error "服务器域名或IP不能为空"
    exit 1
fi

log_info "========== 开始部署 =========="

# 1. 创建 Swap 分区
log_info "步骤 1/10: 配置 Swap 分区..."
if [ ! -f /swapfile ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    sysctl vm.swappiness=10
    echo 'vm.swappiness=10' >> /etc/sysctl.conf
    log_info "Swap 分区创建完成"
else
    log_warn "Swap 分区已存在，跳过"
fi

# 2. 更新系统
log_info "步骤 2/10: 更新系统..."
apt update && apt upgrade -y

# 3. 安装基础工具
log_info "步骤 3/10: 安装基础工具..."
apt install -y wget curl git vim net-tools htop iotop \
  software-properties-common apt-transport-https ca-certificates \
  gnupg lsb-release zip unzip

# 4. 安装 Java 17
log_info "步骤 4/10: 安装 Java 17..."
if ! command -v java &> /dev/null; then
    apt install -y openjdk-17-jdk
    echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
    log_info "Java 17 安装完成"
else
    log_warn "Java 已安装，跳过"
fi

# 5. 安装 MySQL 8.0
log_info "步骤 5/10: 安装 MySQL 8.0..."
if ! command -v mysql &> /dev/null; then
    apt install -y mysql-server
    systemctl start mysql
    systemctl enable mysql

    # 设置 root 密码
    mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';
FLUSH PRIVILEGES;
EOF

    log_info "MySQL 安装完成"
else
    log_warn "MySQL 已安装，跳过"
fi

# 优化 MySQL 配置
log_info "优化 MySQL 配置..."
cat > /etc/mysql/mysql.conf.d/optimization.cnf <<EOF
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
max_connections = 100

# InnoDB 优化（4G内存）
innodb_buffer_pool_size = 512M
innodb_log_file_size = 128M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/mysql-slow.log
long_query_time = 2

# 临时表大小
tmp_table_size = 64M
max_heap_table_size = 64M
EOF

systemctl restart mysql

# 6. 安装 Redis
log_info "步骤 6/10: 安装 Redis..."
if ! command -v redis-server &> /dev/null; then
    apt install -y redis-server

    # 配置 Redis
    sed -i "s/# requirepass foobared/requirepass $REDIS_PASSWORD/" /etc/redis/redis.conf
    sed -i "s/# maxmemory <bytes>/maxmemory 256mb/" /etc/redis/redis.conf
    echo "maxmemory-policy allkeys-lru" >> /etc/redis/redis.conf
    echo "appendonly yes" >> /etc/redis/redis.conf

    # 禁用危险命令
    cat >> /etc/redis/redis.conf <<EOF
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command KEYS ""
rename-command CONFIG ""
EOF

    systemctl restart redis-server
    systemctl enable redis-server
    log_info "Redis 安装完成"
else
    log_warn "Redis 已安装，跳过"
fi

# 7. 安装 Nacos
log_info "步骤 7/10: 安装 Nacos..."
if [ ! -d /opt/nacos ]; then
    mkdir -p /opt/nacos
    cd /opt/nacos
    wget -q https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.tar.gz
    tar -xzf nacos-server-2.3.0.tar.gz
    rm nacos-server-2.3.0.tar.gz

    # 创建 Nacos 数据库
    mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

    mysql -u root -p"$MYSQL_ROOT_PASSWORD" nacos < /opt/nacos/nacos/conf/mysql-schema.sql

    # 配置 Nacos
    cat > /opt/nacos/nacos/conf/application.properties <<EOF
server.servlet.contextPath=/nacos
server.port=8848
nacos.mode=standalone
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=$MYSQL_ROOT_PASSWORD
EOF

    # 创建 Systemd 服务
    cat > /etc/systemd/system/nacos.service <<EOF
[Unit]
Description=Nacos Server
After=network.target mysql.service

[Service]
Type=forking
User=root
ExecStart=/opt/nacos/nacos/bin/startup.sh -m standalone
ExecStop=/opt/nacos/nacos/bin/shutdown.sh
Restart=on-failure
RestartSec=10
Environment="JAVA_OPT=-Xms384m -Xmx384m -Xmn128m"

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl start nacos
    systemctl enable nacos
    log_info "Nacos 安装完成"
else
    log_warn "Nacos 已安装，跳过"
fi

# 8. 安装 MinIO
log_info "步骤 8/10: 安装 MinIO..."
if [ ! -f /opt/minio ]; then
    cd /opt
    wget -q https://dl.min.io/server/minio/release/linux-amd64/minio
    chmod +x minio
    mkdir -p /data/minio

    # 创建 Systemd 服务
    cat > /etc/systemd/system/minio.service <<EOF
[Unit]
Description=MinIO
Documentation=https://docs.min.io
Wants=network-online.target
After=network-online.target

[Service]
User=root
Group=root
Environment="MINIO_ROOT_USER=minioadmin"
Environment="MINIO_ROOT_PASSWORD=$MINIO_SECRET_KEY"
ExecStart=/opt/minio server /data/minio --console-address ":9001"
Restart=always
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl start minio
    systemctl enable minio
    log_info "MinIO 安装完成"
else
    log_warn "MinIO 已安装，跳过"
fi

# 9. 安装 Nginx
log_info "步骤 9/10: 安装 Nginx..."
if ! command -v nginx &> /dev/null; then
    apt install -y nginx
    systemctl start nginx
    systemctl enable nginx
    log_info "Nginx 安装完成"
else
    log_warn "Nginx 已安装，跳过"
fi

# 10. 创建数据库
log_info "步骤 10/10: 创建应用数据库..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS campus_review_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_review_restaurant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_review_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'campus_user'@'localhost' IDENTIFIED BY '$DB_USER_PASSWORD';
GRANT ALL PRIVILEGES ON campus_review_* TO 'campus_user'@'localhost';
FLUSH PRIVILEGES;
EOF

log_info "数据库创建完成"

# 创建应用目录
log_info "创建应用目录..."
mkdir -p /opt/campus-review/{services,gateways,logs,config}
mkdir -p /opt/backups/mysql
mkdir -p /opt/scripts

# 创建环境变量文件
log_info "创建环境变量文件..."
cat > /etc/profile.d/campus-review.sh <<EOF
#!/bin/bash
export NACOS_SERVER_ADDR=localhost:8848
export DB_URL_USER="jdbc:mysql://localhost:3306/campus_review_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
export DB_URL_RESTAURANT="jdbc:mysql://localhost:3306/campus_review_restaurant?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
export DB_URL_REVIEW="jdbc:mysql://localhost:3306/campus_review_review?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
export DB_USERNAME="campus_user"
export DB_PASSWORD="$DB_USER_PASSWORD"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export REDIS_PASSWORD="$REDIS_PASSWORD"
export REDIS_DATABASE="0"
export JWT_SECRET="$JWT_SECRET"
export MINIO_ENDPOINT="http://localhost:9000"
export MINIO_ACCESS_KEY="minioadmin"
export MINIO_SECRET_KEY="$MINIO_SECRET_KEY"
export MINIO_BUCKET="campus-review"
export JWT_EXPIRATION="86400000"
EOF

chmod +x /etc/profile.d/campus-review.sh

# 创建备份脚本
log_info "创建备份脚本..."
cat > /opt/scripts/backup-mysql.sh <<EOF
#!/bin/bash
BACKUP_DIR="/opt/backups/mysql"
DATE=\$(date +%Y%m%d_%H%M%S)

mkdir -p \$BACKUP_DIR

mysqldump -u campus_user -p'$DB_USER_PASSWORD' \\
  --databases campus_review_user campus_review_restaurant campus_review_review \\
  > \$BACKUP_DIR/campus_review_\$DATE.sql

gzip \$BACKUP_DIR/campus_review_\$DATE.sql

find \$BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "Backup completed: campus_review_\$DATE.sql.gz"
EOF

chmod +x /opt/scripts/backup-mysql.sh

# 验证安装
log_info "========== 验证安装 =========="
log_info "检查服务状态..."

services=("mysql" "redis-server" "nacos" "minio" "nginx")
for service in "${services[@]}"; do
    if systemctl is-active --quiet "$service"; then
        log_info "✓ $service 运行正常"
    else
        log_error "✗ $service 未运行"
    fi
done

# 显示内存使用
log_info "内存使用情况:"
free -h

# 显示端口监听
log_info "端口监听情况:"
netstat -tlnp | grep -E '3306|6379|8848|9000|80'

log_info "========== 部署完成 =========="
log_info ""
log_info "后续步骤："
log_info "1. 上传 JAR 包到 /opt/campus-review/services/ 和 /opt/campus-review/gateways/"
log_info "2. 创建 Systemd 服务（参考部署指南）"
log_info "3. 上传前端文件到 /var/www/campus-review/dist/"
log_info "4. 配置 Nginx（参考部署指南）"
log_info "5. 在 Nacos 中创建配置文件"
log_info ""
log_info "访问地址："
log_info "- Nacos 控制台: http://$SERVER_DOMAIN:8848/nacos (nacos/nacos)"
log_info "- MinIO 控制台: http://$SERVER_DOMAIN:9001 (minioadmin/$MINIO_SECRET_KEY)"
log_info ""
log_info "数据库信息："
log_info "- MySQL root 密码: $MYSQL_ROOT_PASSWORD"
log_info "- 应用数据库用户: campus_user"
log_info "- 应用数据库密码: $DB_USER_PASSWORD"
log_info "- Redis 密码: $REDIS_PASSWORD"
log_info "- JWT 密钥: $JWT_SECRET"
log_info ""
log_warn "请妥善保存以上密码信息！"
