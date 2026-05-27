<template>
  <div class="app-layout">
    <!-- 头部导航 -->
    <header class="app-header">
      <div class="header-content">
        <router-link to="/" class="logo">
          <div class="logo-icon">
            <img src="/logo.png" alt="中原科技学院" class="logo-image" />
          </div>
          <div class="logo-text">
            <span class="logo-title">食品评价平台</span>
            <span class="logo-subtitle">发现校园美味</span>
          </div>
        </router-link>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/restaurants" class="nav-item nav-item-coral">
            <div class="nav-icon-wrapper">
              <el-icon class="nav-icon"><Shop /></el-icon>
            </div>
            <span>餐馆</span>
          </router-link>
          <router-link to="/rankings" class="nav-item nav-item-mint">
            <div class="nav-icon-wrapper">
              <el-icon class="nav-icon"><Trophy /></el-icon>
            </div>
            <span>排行榜</span>
          </router-link>
        </nav>

        <!-- 用户操作区 -->
        <div class="user-actions">
          <!-- 暗夜模式切换按钮 -->
          <button class="btn-theme-toggle" @click="toggleDarkMode">
            <div class="theme-icon-wrapper">
              <el-icon :size="18">
                <Moon v-if="!isDarkMode" />
                <Sunny v-else />
              </el-icon>
            </div>
          </button>

          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-avatar">
                <el-avatar :size="40" class="avatar-bg">
                  <User />
                </el-avatar>
                <span class="user-name">{{ userStore.user?.nickname }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <router-link to="/profile">
                    <el-dropdown-item>
                      <el-icon><User /></el-icon>
                      个人中心
                    </el-dropdown-item>
                  </router-link>
                  <router-link to="/notifications">
                    <el-dropdown-item>
                      <el-icon><Bell /></el-icon>
                      消息通知
                    </el-dropdown-item>
                  </router-link>
                  <template v-if="userStore.isAdmin">
                    <el-divider style="margin: 4px 0;" />
                    <div class="admin-section">
                      <span class="admin-label">管理后台</span>
                    </div>
                    <router-link to="/admin/users">
                      <el-dropdown-item>
                        <el-icon><User /></el-icon>
                        用户管理
                      </el-dropdown-item>
                    </router-link>
                    <router-link to="/admin/restaurants">
                      <el-dropdown-item>
                        <el-icon><Shop /></el-icon>
                        餐厅管理
                      </el-dropdown-item>
                    </router-link>
                  </template>
                  <el-divider style="margin: 4px 0;" />
                  <el-dropdown-item @click="handleLogout" class="logout-item">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="btn-login" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" class="btn-register" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>

      <!-- 底部装饰线 -->
      <div class="header-decoration">
        <div class="deco-line deco-line-1"></div>
        <div class="deco-line deco-line-2"></div>
        <div class="deco-line deco-line-3"></div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="app-main">
      <div class="main-content">
        <slot />
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-bg">
        <div class="footer-wave"></div>
      </div>
      <div class="footer-content">
        <div class="footer-left">
          <div class="footer-logo-wrapper">
            <img src="/logo.png" alt="中原科技学院" class="footer-logo" />
          </div>
          <div class="footer-info">
            <span class="footer-text">© 2026 中原科技学院食品评价平台</span>
            <span class="footer-slogan">发现校园里的每一味美好</span>
          </div>
        </div>
        <div class="footer-right">
          <div class="footer-links">
            <router-link to="/restaurants" class="footer-link">餐馆</router-link>
            <router-link to="/rankings" class="footer-link">排行榜</router-link>
          </div>
        </div>
      </div>
    </footer>

    <!-- 移动端底部导航栏 -->
    <nav class="mobile-nav">
      <router-link to="/" class="mobile-nav-item" :class="{ active: $route.path === '/' }">
        <el-icon class="mobile-nav-icon"><HomeFilled /></el-icon>
        <span>首页</span>
      </router-link>
      <router-link to="/restaurants" class="mobile-nav-item" :class="{ active: $route.path.startsWith('/restaurants') }">
        <el-icon class="mobile-nav-icon"><Shop /></el-icon>
        <span>餐馆</span>
      </router-link>
      <router-link to="/rankings" class="mobile-nav-item" :class="{ active: $route.path === '/rankings' }">
        <el-icon class="mobile-nav-icon"><Trophy /></el-icon>
        <span>排行榜</span>
      </router-link>
      <router-link to="/profile" class="mobile-nav-item" :class="{ active: $route.path === '/profile' }">
        <el-icon class="mobile-nav-icon"><User /></el-icon>
        <span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Shop,
  Trophy,
  User,
  Bell,
  SwitchButton,
  ArrowDown,
  Moon,
  Sunny,
  HomeFilled,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 暗夜模式状态
const isDarkMode = ref(false)

// 切换暗夜模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('darkMode', 'true')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('darkMode', 'false')
  }
}

// 初始化时检查本地存储
onMounted(() => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode === 'true') {
    isDarkMode.value = true
    document.documentElement.classList.add('dark')
  }
  // 初始化用户状态（从本地存储恢复 token 和用户信息）
  userStore.init()
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style>
/* ========== 暗夜模式全局样式 ========== */
html.dark {
  color-scheme: dark;
}

html.dark body {
  background: var(--neutral-50);
}

/* 暗夜模式 - 头部导航 */
html.dark .app-header {
  background: rgba(22, 27, 34, 0.95);
  border-bottom-color: rgba(255, 107, 107, 0.15);
}

html.dark .logo {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(167, 139, 250, 0.1) 100%);
  border: 1px solid rgba(255, 107, 107, 0.2);
}

html.dark .logo:hover {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.15) 0%, rgba(167, 139, 250, 0.15) 100%);
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.2);
}

html.dark .logo-title {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

html.dark .logo-subtitle {
  color: var(--neutral-400);
}

/* 暗夜模式 - 导航菜单 */
html.dark .nav-menu {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.08);
}

html.dark .nav-item {
  color: var(--neutral-600);
}

html.dark .nav-item:hover {
  background: rgba(255, 107, 107, 0.1);
}

html.dark .nav-item-coral:hover .nav-icon-wrapper {
  background: rgba(255, 107, 107, 0.2);
  color: var(--color-coral);
}

html.dark .nav-item-mint:hover .nav-icon-wrapper {
  background: rgba(78, 205, 196, 0.2);
  color: var(--color-mint);
}

html.dark .nav-item.router-link-active {
  background: rgba(255, 107, 107, 0.1);
}

html.dark .nav-item-coral.router-link-active .nav-icon-wrapper {
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-coral);
}

html.dark .nav-item-mint.router-link-active .nav-icon-wrapper {
  background: var(--gradient-fresh);
  color: white;
  box-shadow: var(--shadow-mint);
}

/* 暗夜模式 - 用户区域 */
html.dark .user-avatar {
  background: rgba(255, 107, 107, 0.08);
}

html.dark .user-avatar:hover {
  background: rgba(255, 107, 107, 0.15);
  border-color: rgba(255, 107, 107, 0.3);
}

html.dark .user-name {
  color: var(--neutral-700);
}

html.dark .dropdown-icon {
  color: var(--neutral-500);
}

/* 暗夜模式 - 主题切换按钮 */
html.dark .btn-theme-toggle {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(167, 139, 250, 0.1) 100%);
  border-color: rgba(255, 107, 107, 0.2);
}

html.dark .btn-theme-toggle:hover {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.2) 0%, rgba(167, 139, 250, 0.2) 100%);
  box-shadow: 0 0 20px rgba(255, 107, 107, 0.3);
}

html.dark .btn-theme-toggle .theme-icon-wrapper {
  background: var(--gradient-warm);
  color: white;
}

/* 暗夜模式 - 按钮 */
html.dark .btn-login {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.3);
  color: var(--color-coral);
}

html.dark .btn-login:hover {
  background: rgba(255, 107, 107, 0.2);
  border-color: var(--color-coral);
}

/* 暗夜模式 - 主内容区 */
html.dark .app-main {
  background: var(--neutral-50);
}

/* 暗夜模式 - 页脚 */
html.dark .app-footer {
  background: linear-gradient(135deg, #161B22 0%, #21262D 100%);
}

html.dark .footer-wave {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.05) 0%, rgba(78, 205, 196, 0.05) 100%);
}

html.dark .footer-text {
  color: var(--neutral-500);
}

html.dark .footer-slogan {
  color: var(--neutral-600);
}

html.dark .footer-link {
  color: var(--neutral-500);
}

html.dark .footer-link:hover {
  color: var(--color-coral);
}

/* 暗夜模式 - 移动端底部导航栏 */
html.dark .mobile-nav {
  background: rgba(22, 27, 34, 0.98);
  border-top-color: rgba(255, 107, 107, 0.15);
}

html.dark .mobile-nav-item {
  color: var(--neutral-500);
}

html.dark .mobile-nav-item.active {
  color: var(--color-coral);
}

/* 暗夜模式 - 管理后台标签 */
html.dark .admin-section {
  padding: 8px 16px;
}

html.dark .admin-label {
  color: var(--color-coral);
  font-weight: 700;
  font-size: 12px;
}

/* 光明模式 - 管理后台标签 */
.admin-section {
  padding: 8px 16px;
}

.admin-label {
  color: var(--color-coral);
  font-weight: 700;
  font-size: 12px;
  display: block;
}
</style>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  transition: all var(--duration-normal) var(--ease-smooth);
}

/* ========== 头部导航 ========== */
.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ========== Logo ========== */
.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.05) 0%, rgba(167, 139, 250, 0.05) 100%);
  padding: 8px 16px 8px 8px;
  border-radius: var(--radius-xl);
  transition: all var(--duration-normal) var(--ease-bounce);
  border: 1px solid rgba(255, 107, 107, 0.1);
}

.logo:hover {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(167, 139, 250, 0.1) 100%);
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.15);
  transform: translateY(-2px);
}

.logo-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--gradient-primary);
  padding: 2px;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: calc(var(--radius-lg) - 2px);
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-family: "Noto Serif SC", serif;
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.logo-subtitle {
  font-size: 11px;
  color: var(--neutral-400);
  letter-spacing: 0.5px;
}

/* ========== 导航菜单 ========== */
.nav-menu {
  display: flex;
  gap: 8px;
  background: var(--neutral-100);
  padding: 6px;
  border-radius: var(--radius-xl);
  border: 1px solid var(--neutral-200);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--neutral-600);
  text-decoration: none;
  padding: 10px 16px;
  border-radius: var(--radius-lg);
  transition: all var(--duration-normal) var(--ease-bounce);
  font-weight: 500;
  font-size: 14px;
}

.nav-icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.nav-item-coral:hover .nav-icon-wrapper {
  background: rgba(255, 107, 107, 0.15);
  color: var(--color-coral);
}

.nav-item-mint:hover .nav-icon-wrapper {
  background: rgba(78, 205, 196, 0.15);
  color: var(--color-mint);
}

.nav-item.router-link-active {
  background: white;
  box-shadow: var(--shadow-md);
}

.nav-item-coral.router-link-active .nav-icon-wrapper {
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-coral);
}

.nav-item-mint.router-link-active .nav-icon-wrapper {
  background: var(--gradient-fresh);
  color: white;
  box-shadow: var(--shadow-mint);
}

.nav-item.router-link-active span:last-child {
  font-weight: 600;
}

/* ========== 用户操作区 ========== */
.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px 6px 6px;
  border-radius: var(--radius-xl);
  transition: all var(--duration-normal) var(--ease-bounce);
  background: rgba(255, 107, 107, 0.05);
  border: 1px solid transparent;
}

.user-avatar:hover {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.2);
  transform: translateY(-2px);
}

.avatar-bg {
  background: var(--gradient-primary);
  color: white !important;
  font-weight: 600;
}

.user-name {
  color: var(--neutral-700);
  font-size: 14px;
  font-weight: 500;
}

.dropdown-icon {
  color: var(--neutral-400);
  font-size: 12px;
  transition: transform var(--duration-fast) var(--ease-smooth);
}

.user-avatar:hover .dropdown-icon {
  transform: translateY(2px);
}

/* ========== 主题切换按钮 ========== */
.btn-theme-toggle {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--neutral-200);
  background: var(--neutral-100);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-normal) var(--ease-bounce);
}

.btn-theme-toggle:hover {
  background: var(--neutral-200);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.theme-icon-wrapper {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--gradient-dreamy);
  color: white;
  transition: all var(--duration-normal) var(--ease-bounce);
}

.btn-theme-toggle:hover .theme-icon-wrapper {
  transform: rotate(15deg);
}

/* ========== 登录/注册按钮 ========== */
.btn-login {
  height: 42px;
  padding: 0 20px;
  border-radius: var(--radius-lg);
  font-weight: 600;
  background: rgba(255, 107, 107, 0.08);
  color: var(--color-coral);
  border: 1px solid rgba(255, 107, 107, 0.2);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.btn-login:hover {
  background: rgba(255, 107, 107, 0.15);
  border-color: var(--color-coral);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.2);
}

.btn-register {
  height: 42px;
  padding: 0 24px;
  border-radius: var(--radius-lg);
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-coral);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.btn-register:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.4);
}

/* ========== 头部装饰线 ========== */
.header-decoration {
  display: flex;
  height: 3px;
  background: var(--neutral-100);
}

.deco-line {
  height: 100%;
  transition: width var(--duration-slow) var(--ease-smooth);
}

.deco-line-1 {
  flex: 2;
  background: var(--gradient-primary);
}

.deco-line-2 {
  flex: 1;
  background: var(--gradient-fresh);
}

.deco-line-3 {
  flex: 1;
  background: var(--gradient-dreamy);
}

/* ========== 主内容区 ========== */
.app-main {
  flex: 1;
  background: var(--neutral-50);
}

.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* ========== 页脚 ========== */
.app-footer {
  position: relative;
  background: linear-gradient(135deg, var(--neutral-800) 0%, var(--neutral-900) 100%);
  padding: 48px 24px 32px;
  overflow: hidden;
}

.footer-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.footer-wave {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.03) 0%, rgba(78, 205, 196, 0.03) 100%);
  clip-path: polygon(0 0, 100% 0, 100% 30%, 0 70%);
}

.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.footer-logo-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  padding: 2px;
  box-shadow: var(--shadow-coral);
}

.footer-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: calc(var(--radius-lg) - 2px);
}

.footer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.footer-text {
  color: var(--neutral-400);
  font-size: 14px;
}

.footer-slogan {
  color: var(--neutral-500);
  font-size: 12px;
  font-style: italic;
}

.footer-right {
  display: flex;
  align-items: center;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-link {
  color: var(--neutral-400);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--duration-fast) var(--ease-smooth);
  position: relative;
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--gradient-primary);
  transition: width var(--duration-normal) var(--ease-smooth);
}

.footer-link:hover {
  color: var(--color-coral);
}

.footer-link:hover::after {
  width: 100%;
}

/* ========== 移动端底部导航栏 ========== */
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--neutral-200);
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  z-index: 1000;
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: var(--neutral-500);
  font-size: 11px;
  font-weight: 500;
  padding: 6px 0;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.mobile-nav-icon {
  font-size: 22px;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.mobile-nav-item.active {
  color: var(--color-coral);
}

.mobile-nav-item.active .mobile-nav-icon {
  transform: scale(1.1);
}

.mobile-nav-item:active {
  opacity: 0.7;
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 60px;
  }

  .nav-menu {
    display: none;
  }

  .logo-text {
    display: none;
  }

  .user-name {
    display: none;
  }

  /* 移动端显示底部导航 */
  .mobile-nav {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
  }

  /* 移动端优化按钮尺寸 */
  .btn-login,
  .btn-register {
    height: 36px;
    padding: 0 14px;
    font-size: 13px;
  }

  .btn-theme-toggle {
    width: 36px;
    height: 36px;
  }

  .theme-icon-wrapper {
    width: 24px;
    height: 24px;
  }

  /* 移动端页脚隐藏 */
  .app-footer {
    display: none;
  }

  /* 主内容区添加底部间距 */
  .app-main {
    padding-bottom: calc(60px + env(safe-area-inset-bottom));
  }

  .main-content {
    padding: 20px 16px;
  }
}

/* ========== 下拉菜单样式修复 ========== */
:deep(.el-dropdown-menu) {
  border-radius: var(--radius-lg);
  border: 1px solid var(--neutral-200);
  box-shadow: var(--shadow-xl);
  padding: 8px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: var(--radius-md);
  padding: 10px 16px;
  margin: 2px 0;
  transition: all var(--duration-fast) var(--ease-smooth);
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(255, 107, 107, 0.08);
  color: var(--color-coral);
}

.logout-item:hover {
  background: rgba(239, 68, 68, 0.08) !important;
  color: var(--color-error) !important;
}
</style>
