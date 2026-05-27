# Nginx 反向代理部署指南

## 架构说明

前端统一请求当前域名（默认 80 端口），通过 nginx 反向代理将请求分发到不同的后端网关服务：

```
前端 (localhost:80)
    ↓
  Nginx 反向代理
    ├─→ /api/auth, /api/user, /api/restaurant 等 → 用户网关 (localhost:8001)
    └─→ /api/admin/* → 管理端网关 (localhost:8002)
```

## 环境变量配置

### 开发环境 (`.env.development`)

如果本地开发**没有** nginx 反向代理：

```env
VITE_API_BASE_URL=http://localhost:8001
```

如果本地**有** nginx 反向代理：

```env
VITE_API_BASE_URL=
```

### 生产环境 (`.env.production`)

```env
VITE_API_BASE_URL=
```

## Nginx 配置步骤

### 1. 复制配置文件

将 `nginx.conf.example` 复制到 nginx 配置目录：

```bash
# Windows
copy nginx.conf.example C:\nginx\conf\nginx.conf

# Linux/Mac
sudo cp nginx.conf.example /etc/nginx/sites-available/campus-review
sudo ln -s /etc/nginx/sites-available/campus-review /etc/nginx/sites-enabled/
```

### 2. 修改配置文件

根据实际情况修改 `nginx.conf.example`：

```nginx
server {
    listen 80;  # 可以修改为其他端口，如 8080
    server_name localhost;

    # 开发环境：代理到 Vite 开发服务器
    location / {
        proxy_pass http://localhost:3000;
        # ... 其他配置
    }

    # 用户网关 API
    location ~ ^/api/(?!admin) {
        proxy_pass http://localhost:8001;
        # ... 其他配置
    }

    # 管理端网关 API
    location /api/admin/ {
        proxy_pass http://localhost:8002/api/admin/;
        # ... 其他配置
    }
}
```

### 3. 测试配置

```bash
# 测试 nginx 配置语法
nginx -t

# 重新加载配置
nginx -s reload
```

### 4. 启动服务

确保以下服务已启动：

1. **用户网关** (端口 8001)
2. **管理端网关** (端口 8002)
3. **前端开发服务器** (端口 3000) - 仅开发环境
4. **Nginx** (端口 80)

## 验证部署

### 1. 检查前端访问

访问 `http://localhost`，应该能看到前端页面。

### 2. 检查 API 代理

```bash
# 测试用户网关 API
curl http://localhost/api/auth/login

# 测试管理端网关 API
curl http://localhost/api/admin/users
```

### 3. 检查请求头

管理端 API 会自动添加 `X-User-Id` 和 `X-User-Roles` 请求头，nginx 会将这些头传递给后端。

## 生产环境部署

生产环境下，前端静态文件由 nginx 直接提供：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    root /usr/share/nginx/html;
    index index.html;

    # 所有非 API 请求返回 index.html (支持 Vue Router history 模式)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理配置同上
    location ~ ^/api/(?!admin) {
        proxy_pass http://localhost:8001;
        # ...
    }

    location /api/admin/ {
        proxy_pass http://localhost:8002/api/admin/;
        # ...
    }
}
```

构建生产版本：

```bash
npm run build
```

将 `dist/` 目录下的文件复制到 nginx 的 `root` 目录。

## 常见问题

### 1. 跨域问题

nginx 反向代理已解决跨域问题，前端无需额外配置 CORS。

### 2. WebSocket 支持

开发环境下 Vite HMR 需要 WebSocket 支持，nginx 配置已包含：

```nginx
proxy_http_version 1.1;
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection 'upgrade';
```

### 3. 端口冲突

如果 80 端口被占用，可以修改 nginx 监听端口：

```nginx
server {
    listen 8080;  # 修改为其他端口
    # ...
}
```

然后修改 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 4. 请求超时

如果后端响应较慢，可以增加 nginx 超时时间：

```nginx
proxy_connect_timeout 30s;
proxy_send_timeout 30s;
proxy_read_timeout 30s;
```

## 安全建议

1. **启用 HTTPS**：生产环境建议配置 SSL 证书
2. **限制请求大小**：防止大文件上传攻击
   ```nginx
   client_max_body_size 10M;
   ```
3. **启用 Gzip 压缩**：减少传输数据量
   ```nginx
   gzip on;
   gzip_types text/plain application/json;
   ```
4. **添加安全头**：
   ```nginx
   add_header X-Frame-Options "SAMEORIGIN" always;
   add_header X-Content-Type-Options "nosniff" always;
   ```
