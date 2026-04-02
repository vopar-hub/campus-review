# 快速部署清单

> 4核4G内存服务器部署步骤速查表

## 📋 部署前准备

### 本地准备（开发机器）

```bash
# 1. 构建后端
cd campus-review
mvn clean package -DskipTests

# 2. 构建前端
cd campus-review-frontend
npm install
npm run build
tar -czf dist.tar.gz dist/

# 3. 准备文件
# - JAR 包（5个）
# - dist.tar.gz
# - static/init-databases.sql
# - static/nacos-config/*.yaml（5个配置文件）
```

### 服务器信息收集

- [ ] 服务器 IP 地址：`________________`
- [ ] SSH 登录用户名：`________________`
- [ ] SSH 登录密码/密钥：`________________`
- [ ] 域名（可选）：`________________`

---

## 🚀 部署步骤

### 第一步：上传部署脚本

```bash
# 在本地执行
scp deploy/ubuntu-deploy.sh user@your-server-ip:/tmp/
```

### 第二步：运行部署脚本

```bash
# SSH 登录服务器
ssh user@your-server-ip

# 运行脚本
sudo bash /tmp/ubuntu-deploy.sh

# 按提示输入密码（建议使用默认值）
# 等待安装完成（约10-15分钟）
```

### 第三步：上传后端 JAR 包

```bash
# 在本地执行
cd campus-review

# 上传服务
scp campus-review-service/user-service/target/user-service-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/opt/campus-review/services/

scp campus-review-service/restaurant-service/target/restaurant-service-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/opt/campus-review/services/

scp campus-review-service/review-service/target/review-service-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/opt/campus-review/services/

# 上传网关
scp campus-review-gateway/campus-review-user-gateway/target/campus-review-user-gateway-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/opt/campus-review/gateways/

scp campus-review-gateway/campus-review-admin-gateway/target/campus-review-admin-gateway-0.0.1-SNAPSHOT.jar \
    user@your-server-ip:/opt/campus-review/gateways/
```

### 第四步：创建 Systemd 服务

```bash
# SSH 登录服务器
ssh user@your-server-ip

# 下载服务配置脚本（见下方）
# 或手动创建（参考部署指南第4.5节）
```

创建快速部署脚本 `create-services.sh`：

```bash
#!/bin/bash
# 创建所有服务的 systemd 配置

# JWT 密钥（从部署脚本输出中获取）
JWT_SECRET="campus-review-production-jwt-secret-key-2026-minimum-32-chars"
DB_PASSWORD="CampusUser2026!"
REDIS_PASSWORD="CampusRedis2026!"

# user-service
cat > /etc/systemd/system/campus-user-service.service <<EOF
[Unit]
Description=Campus Review User Service
After=syslog.target network.target mysql.service redis.service nacos.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus-review/services
ExecStart=/usr/bin/java -Xms256m -Xmx384m -Xmn128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/campus-review/services/user-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=20
Environment="NACOS_SERVER_ADDR=localhost:8848"
Environment="DB_URL=jdbc:mysql://localhost:3306/campus_review_user?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
Environment="DB_USERNAME=campus_user"
Environment="DB_PASSWORD=$DB_PASSWORD"
Environment="REDIS_HOST=localhost"
Environment="REDIS_PORT=6379"
Environment="REDIS_PASSWORD=$REDIS_PASSWORD"
Environment="JWT_SECRET=$JWT_SECRET"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# restaurant-service
cat > /etc/systemd/system/campus-restaurant-service.service <<EOF
[Unit]
Description=Campus Review Restaurant Service
After=syslog.target network.target mysql.service redis.service nacos.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus-review/services
ExecStart=/usr/bin/java -Xms256m -Xmx384m -Xmn128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/campus-review/services/restaurant-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=20
Environment="NACOS_SERVER_ADDR=localhost:8848"
Environment="DB_URL=jdbc:mysql://localhost:3306/campus_review_restaurant?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
Environment="DB_USERNAME=campus_user"
Environment="DB_PASSWORD=$DB_PASSWORD"
Environment="REDIS_HOST=localhost"
Environment="REDIS_PORT=6379"
Environment="REDIS_PASSWORD=$REDIS_PASSWORD"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# review-service
cat > /etc/systemd/system/campus-review-service.service <<EOF
[Unit]
Description=Campus Review Review Service
After=syslog.target network.target mysql.service redis.service nacos.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus-review/services
ExecStart=/usr/bin/java -Xms256m -Xmx384m -Xmn128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/campus-review/services/review-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=20
Environment="NACOS_SERVER_ADDR=localhost:8848"
Environment="DB_URL=jdbc:mysql://localhost:3306/campus_review_review?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
Environment="DB_USERNAME=campus_user"
Environment="DB_PASSWORD=$DB_PASSWORD"
Environment="REDIS_HOST=localhost"
Environment="REDIS_PORT=6379"
Environment="REDIS_PASSWORD=$REDIS_PASSWORD"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# user-gateway
cat > /etc/systemd/system/campus-user-gateway.service <<EOF
[Unit]
Description=Campus Review User Gateway
After=syslog.target network.target nacos.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus-review/gateways
ExecStart=/usr/bin/java -Xms192m -Xmx256m -Xmn96m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/campus-review/gateways/campus-review-user-gateway-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=20
Environment="NACOS_SERVER_ADDR=localhost:8848"
Environment="JWT_SECRET=$JWT_SECRET"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# admin-gateway
cat > /etc/systemd/system/campus-admin-gateway.service <<EOF
[Unit]
Description=Campus Review Admin Gateway
After=syslog.target network.target nacos.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus-review/gateways
ExecStart=/usr/bin/java -Xms192m -Xmx256m -Xmn96m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/campus-review/gateways/campus-review-admin-gateway-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=20
Environment="NACOS_SERVER_ADDR=localhost:8848"
Environment="JWT_SECRET=$JWT_SECRET"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 重载 systemd
systemctl daemon-reload

echo "Systemd 服务创建完成"
```

```bash
# 运行脚本
sudo bash create-services.sh
```

### 第五步：启动后端服务

```bash
# 按顺序启动
sudo systemctl start campus-user-service
sleep 30

sudo systemctl start campus-restaurant-service
sleep 30

sudo systemctl start campus-review-service
sleep 30

sudo systemctl start campus-user-gateway
sleep 10

sudo systemctl start campus-admin-gateway

# 设置开机自启
sudo systemctl enable campus-*

# 查看状态
sudo systemctl status campus-*
```

### 第六步：上传前端文件

```bash
# 在本地执行
scp campus-review-frontend/dist.tar.gz user@your-server-ip:/tmp/

# SSH 登录服务器
ssh user@your-server-ip

# 解压前端文件
sudo mkdir -p /var/www/campus-review
sudo tar -xzf /tmp/dist.tar.gz -C /var/www/campus-review/
sudo chown -R www-data:www-data /var/www/campus-review
```

### 第七步：配置 Nginx

```bash
# SSH 登录服务器
ssh user@your-server-ip

# 创建 Nginx 配置
sudo vim /etc/nginx/sites-available/campus-review
```

粘贴以下内容（替换 `your-domain.com` 为你的域名或IP）：

```nginx
upstream user_gateway {
    server localhost:8001;
}

upstream admin_gateway {
    server localhost:8002;
}

server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或IP

    root /var/www/campus-review/dist;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_min_length 1000;

    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~ ^/api/(?!admin) {
        proxy_pass http://user_gateway;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 15s;
        proxy_send_timeout 15s;
        proxy_read_timeout 15s;
        client_max_body_size 10M;
    }

    location /api/admin/ {
        proxy_pass http://admin_gateway/api/admin/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-User-Id $http_x_user_id;
        proxy_set_header X-User-Roles $http_x_user_roles;
        proxy_connect_timeout 15s;
        proxy_send_timeout 15s;
        proxy_read_timeout 15s;
        client_max_body_size 10M;
    }

    location ~* \.(jpg|jpeg|png|gif|ico|css|js|woff|woff2|ttf|svg)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location ~ /\. {
        deny all;
    }
}
```

```bash
# 启用配置
sudo ln -s /etc/nginx/sites-available/campus-review /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

### 第八步：配置 Nacos

```bash
# 访问 Nacos 控制台
# http://your-server-ip:8848/nacos
# 账号密码: nacos/nacos

# 创建 5 个配置文件：
# 1. user-service.yaml
# 2. restaurant-service.yaml
# 3. review-service.yaml
# 4. user-gateway.yaml
# 5. admin-gateway.yaml

# 配置内容从项目的 static/nacos-config/ 目录复制
```

---

## ✅ 验证部署

### 检查服务状态

```bash
# 检查所有服务
sudo systemctl status campus-*

# 检查端口
sudo netstat -tlnp | grep -E '80|8001|8002|8102|8103|8104|3306|6379|8848|9000'

# 检查内存
free -h

# 检查 Nacos 注册
# 访问 http://your-server-ip:8848/nacos
# 查看服务列表，应该有 5 个服务
```

### 测试接口

```bash
# 测试健康检查
curl http://localhost:8001/actuator/health
curl http://localhost:8002/actuator/health

# 测试前端
curl http://localhost/

# 测试用户注册
curl -X POST http://localhost/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@campus.edu",
    "studentNo": "20250001",
    "password": "test123456",
    "nickname": "test"
  }'
```

---

## 🔧 常用命令

### 服务管理

```bash
# 启动所有服务
sudo systemctl start campus-user-service
sudo systemctl start campus-restaurant-service
sudo systemctl start campus-review-service
sudo systemctl start campus-user-gateway
sudo systemctl start campus-admin-gateway

# 停止所有服务
sudo systemctl stop campus-*

# 重启所有服务
sudo systemctl restart campus-*

# 查看日志
sudo journalctl -u campus-user-service -f
```

### 监控

```bash
# 实时监控内存
htop

# 查看内存使用
free -h

# 查看 Java 进程
ps aux | grep java

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

---

## 🚨 故障排查

### 内存不足

```bash
# 检查内存
free -h

# 如果 Swap 使用率高，增加 Swap
sudo fallocate -l 4G /swapfile2
sudo chmod 600 /swapfile2
sudo mkswap /swapfile2
sudo swapon /swapfile2
```

### 服务启动失败

```bash
# 查看日志
sudo journalctl -u campus-user-service -n 200

# 检查端口占用
sudo netstat -tlnp | grep 8104

# 检查配置
sudo systemctl show campus-user-service --property=Environment
```

### 数据库连接失败

```bash
# 测试连接
mysql -u campus_user -p -h localhost campus_review_user

# 检查 MySQL 状态
sudo systemctl status mysql

# 查看 MySQL 日志
sudo tail -f /var/log/mysql/error.log
```

---

## 📊 性能优化建议

### 监控内存使用

```bash
# 设置内存监控定时任务
crontab -e

# 每小时记录一次内存使用
0 * * * * free -h >> /opt/logs/memory.log
```

### 定期备份

```bash
# 数据库备份（已配置）
# 每天凌晨 2 点自动备份
# 备份文件位置: /opt/backups/mysql/

# 手动备份
/opt/scripts/backup-mysql.sh
```

### 日志轮转

```bash
# 配置日志轮转
sudo vim /etc/logrotate.d/campus-review
```

```
/opt/campus-review/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0640 root root
}
```

---

## 📝 部署信息记录

**服务器信息**
- 服务器 IP：`________________`
- 域名：`________________`
- SSH 用户：`________________`

**密码信息**
- MySQL root 密码：`________________`
- 应用数据库密码：`________________`
- Redis 密码：`________________`
- MinIO 密钥：`________________`
- JWT 密钥：`________________`

**访问地址**
- 前端：`http://________________`
- Nacos：`http://________________:8848/nacos`
- MinIO：`http://________________:9001`

**备份位置**
- 数据库备份：`/opt/backups/mysql/`
- 日志文件：`/opt/campus-review/logs/`

---

## 🎯 下一步

- [ ] 配置 HTTPS（推荐使用 Let's Encrypt）
- [ ] 配置域名解析
- [ ] 设置监控告警
- [ ] 配置防火墙规则
- [ ] 定期检查内存使用
- [ ] 定期备份数据库

---

## 📚 参考文档

- [详细部署指南](./Ubuntu部署指南-4G内存优化版.md)
- [项目 README](../README.md)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
