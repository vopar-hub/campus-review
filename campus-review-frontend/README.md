# Campus Review Frontend

校园美食点评平台前端 - 基于 Vue 3 + TypeScript + Vite

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | Composition API |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Element Plus | 2.6.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| Tailwind CSS | 3.x | 原子化 CSS |
| Day.js | 1.x | 时间处理 |
| VueUse | 10.x | 组合式工具库 |

## 项目结构

```
campus-review-frontend/
├── src/
│   ├── api/                    # API 接口层
│   │   ├── request.ts          # Axios 实例配置
│   │   ├── auth.ts             # 认证接口
│   │   ├── user.ts             # 用户接口
│   │   ├── restaurant.ts       # 餐馆接口
│   │   ├── review.ts           # 评价接口
│   │   ├── interaction.ts      # 互动接口
│   │   ├── ranking.ts          # 排行榜接口
│   │   └── notification.ts     # 通知接口
│   ├── components/             # 公共组件
│   │   ├── layout/             # 布局组件
│   │   ├── restaurant/         # 餐馆组件
│   │   └── review/             # 评价组件
│   ├── router/                 # 路由配置
│   ├── stores/                 # Pinia 状态管理
│   ├── styles/                 # 全局样式
│   ├── types/                  # TypeScript 类型定义
│   ├── utils/                  # 工具函数
│   ├── views/                  # 页面视图
│   ├── App.vue
│   └── main.ts
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
└── .env
```

## 快速开始

### 环境要求

- Node.js 18+
- npm 或 yarn 或 pnpm

### 安装依赖

```bash
cd campus-review-frontend
npm install
```

### 启动开发服务器

```bash
# 确保后端服务已启动（http://localhost:8001）
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 功能模块

### 认证模块
- 用户注册
- 用户登录
- Token 管理
- 自动登出

### 餐馆模块
- 餐馆列表
- 餐馆搜索
- 餐馆详情
- 创建餐馆

### 评价模块
- 发布评价
- 评价列表
- 评分组件
- 评价审核状态

### 互动模块
- 点赞/取消点赞
- 收藏/取消收藏
- 互动统计

### 排行榜模块
- 热门餐馆榜
- 热度分值显示

### 通知模块
- 消息列表
- 标记已读
- 全部已读

### 用户模块
- 个人信息展示
- 我的评价

## 配置说明

### 环境变量

编辑 `.env` 文件：

```env
# 开发环境
VITE_API_BASE_URL=http://localhost:8001

# 生产环境
# VITE_API_BASE_URL=https://api.campus-review.com
```

### 代理配置

开发环境下，请求通过 Vite 代理转发到后端网关：

```ts
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8001',
    changeOrigin: true
  }
}
```

## API 响应格式

所有接口返回统一格式：

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "requestId": null,
  "timestamp": 1730000000000
}
```

- `code=0` 表示成功
- 其他错误码由 Axios 拦截器统一处理

## 错误码

| Code | 说明 |
|------|------|
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40100 | 未登录或登录已过期 |
| 40300 | 无权限 |
| 40400 | 资源不存在 |
| 42900 | 请求过于频繁 |
| 50000 | 服务器内部错误 |

## 开发规范

### 代码风格

- 使用 Composition API（`<script setup>`）
- 组件名使用大驼峰命名
- 文件命名与组件名一致
- 类型定义放在 `src/types` 目录

### 提交规范

```
<type>: <subject>

类型：
- feat: 新功能
- fix: Bug 修复
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/工具
```

## 浏览器支持

- Chrome >= 90
- Edge >= 90
- Firefox >= 88
- Safari >= 14

## 相关文档

- [后端 API 文档](../docs/api.md)
- [快速启动指南](../docs/quick-start.md)
- [技术方案](../docs/tech-solution.md)

## 许可证

MIT
