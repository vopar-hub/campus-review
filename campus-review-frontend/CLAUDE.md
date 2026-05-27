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
- user-service (8104)
- restaurant-service (8102)
- review-service (8103)
- user-gateway (8001)
- admin-gateway (8002)

## 快速命令

```bash
npm run dev          # 启动开发服务器 (端口 3000)
npm run build        # 类型检查并构建生产版本
npm run preview      # 预览生产构建
npm run lint         # ESLint 检查并自动修复
npm run format       # Prettier 格式化 src/
npm run test         # 运行 Vitest 测试
npm run test:watch   # 监视模式运行测试
npm run test:ui      # 打开 Vitest UI 界面
```

## 架构概览

**技术栈**: Vue 3 (组合式 API) + TypeScript + Vite + Pinia + Element Plus + Tailwind CSS

**核心结构**:
- `src/api/` - API 客户端 (auth, user, restaurant, review, interaction, ranking, notification)
- `src/stores/` - Pinia 状态管理 (用户商店与 JWT 认证)
- `src/router/` - Vue Router 路由守卫 (`meta: { requiresAuth: true }`)
- `src/types/` - TypeScript 类型定义 (DTOs, ApiResponse, ErrorCode)
- `src/components/` - 可复用组件 (layout, restaurant, review)
- `src/views/` - 页面级视图
- `src/styles/` - 全局样式 (main.css 包含设计系统)

## 设计系统

项目使用 **"活力校园"** 多彩设计系统，定义在 `src/styles/main.css`。

**主色调**:
| 颜色 | 变量名 | 用途 |
|------|--------|------|
| 珊瑚橙 #FF6B6B | `--color-coral` | 主品牌色 |
| 薄荷绿 #4ECDC4 | `--color-mint` | 清新辅助色 |
| 阳光黄 #FFE66D | `--color-sunshine` | 活力点缀色 |
| 天空蓝 #45B7D1 | `--color-sky` | 信任色 |
| 薰衣草紫 #A78BFA | `--color-lavender` | 创意色 |
| 蜜桃粉 #F9A8D4 | `--color-peach` | 温暖色 |

**渐变系统**:
- `--gradient-primary`: 珊瑚橙到蜜桃粉 (主品牌渐变)
- `--gradient-fresh`: 薄荷绿到天空蓝 (清新渐变)
- `--gradient-vibrant`: 阳光黄到珊瑚橙 (活力渐变)
- `--gradient-dreamy`: 薰衣草紫到天空蓝 (梦幻渐变)
- `--gradient-rainbow`: 多彩组合 (彩虹渐变)

**使用示例**:
```css
/* 渐变背景 */
background: var(--gradient-primary);

/* 多彩阴影 */
box-shadow: var(--shadow-coral);

/* 渐变文字 */
.gradient-text-rainbow {
  background: var(--gradient-rainbow);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
```

**暗夜模式**: 通过 `html.dark` 类切换，设计系统自动适配。

## API 层

**请求配置** (`src/api/request.ts`):
- Axios 实例，自动注入 JWT token
- 统一响应格式：`{ code, message, data, requestId, timestamp }`
- 错误码：`0=成功`, `40100=未授权`, `40300=禁止访问`, `42900=请求频繁`
- 401 错误自动跳转到 `/login`

**认证流程**:
1. 通过 `src/api/auth.ts` 登录/注册
2. Token 通过 `src/utils/storage.ts` 存储到 Cookie
3. 用户商店 (`src/stores/user.ts`) 管理会话状态
4. 路由守卫重定向未认证用户

**后端集成**:
- 前端请求 `/api` 路径
- Nginx 反向代理到后端网关：
  - `/api/*` → `localhost:8001` (用户网关)
  - `/api/admin/*` → `localhost:8002` (管理端网关)

## 部署

```bash
# 构建
npm run build

# 部署到 nginx
sudo rm -rf /var/www/campus-review/*
sudo cp -r dist/* /var/www/campus-review/
sudo systemctl reload nginx
```

## 开发规范

- 使用组合式 API `<script setup lang="ts">`
- 路径别名：`@/*` → `src/*`
- 组件命名：大驼峰 (PascalCase)
- TypeScript 严格模式
- 使用 CSS 变量保持设计一致性
