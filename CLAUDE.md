# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码仓库中工作提供指导。

## 后端服务管理

**重要**: 启动后端 Java 微服务时，必须使用优化启动脚本：

```bash
# 启动所有服务
bash /home/ubuntu/download/campus-review/deploy/start-services.sh start

# 停止所有服务
bash /home/ubuntu/download/campus-review/deploy/start-services.sh stop

# 重启所有服务
bash /home/ubuntu/download/campus-review/deploy/start-services.sh restart

# 查看服务状态
bash /home/ubuntu/download/campus-review/deploy/start-services.sh status

# 查看日志
bash /home/ubuntu/download/campus-review/deploy/start-services.sh logs user-service
```

**服务列表**:
| 服务 | 端口 | 说明 |
|------|------|------|
| user-service | 8104 | 用户服务 |
| restaurant-service | 8102 | 餐厅服务 |
| review-service | 8103 | 评价服务 |
| user-gateway | 8001 | 用户网关 |
| admin-gateway | 8002 | 管理网关 |

**JVM 优化参数** (适用于 4GB 内存服务器):
```
-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication
```

## 前端开发

### 开发命令

```bash
cd campus-review-frontend

npm run dev          # 启动开发服务器 (端口 3000)
npm run build        # 类型检查并构建生产版本
npm run preview      # 预览生产构建
npm run lint         # ESLint 检查并自动修复
npm run format       # Prettier 格式化 src/
npm run test         # 运行 Vitest 测试
```

### 前端设计系统

项目使用 **"活力校园"** 多彩设计系统，定义在 `src/styles/main.css`。

### 前端架构

**技术栈**: Vue 3 (组合式 API) + TypeScript + Vite + Pinia + Element Plus + Tailwind CSS

**核心结构**:
- `src/api/` - API 客户端
- `src/stores/` - Pinia 状态管理
- `src/router/` - Vue Router 路由
- `src/types/` - TypeScript 类型定义
- `src/components/` - 可复用组件
  - `layout/` - 布局组件 (AppLayout)
  - `restaurant/` - 餐厅组件 (RestaurantCard)
  - `review/` - 评价组件 (ReviewCard, StarRating)
- `src/views/` - 页面视图
- `src/styles/` - 全局样式 (main.css)

## 部署

### 前端部署到 Nginx

```bash
# 1. 构建前端
cd campus-review-frontend
npm run build

# 2. 复制到 nginx 静态目录
sudo rm -rf /var/www/campus-review/*
sudo cp -r dist/* /var/www/campus-review/

# 3. 重新加载 nginx
sudo nginx -t && sudo systemctl reload nginx
```

### Nginx 配置

系统 nginx 配置位于 `/etc/nginx/sites-available/campus-review`:
- 静态文件目录: `/var/www/campus-review`
- API 代理: `/api/` → `localhost:8001` (用户网关)
- 管理端 API: `/api/admin/` → `localhost:8002` (管理网关)
- MinIO 代理: `/minio/` → `localhost:9000`

## 项目结构

```
campus-review/
├── campus-review-service/      # 微服务
│   ├── user-service/           # 用户服务 (8104)
│   ├── restaurant-service/     # 餐厅服务 (8102)
│   └── review-service/         # 评价服务 (8103)
├── campus-review-gateway/      # 网关
│   ├── campus-review-user-gateway/    # 用户网关 (8001)
│   └── campus-review-admin-gateway/   # 管理网关 (8002)
├── campus-review-frontend/     # Vue 3 前端
├── deploy/                     # 部署脚本
│   ├── start-services.sh       # 服务启动脚本
│   └── ubuntu-deploy.sh        # 环境安装脚本
└── docs/                       # 文档
```

## 基础设施服务

- MySQL: localhost:3306
- Redis: localhost:6379
- MinIO: localhost:9000 (控制台: 9001)
- Nginx: 80
