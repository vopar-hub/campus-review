import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/storage'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/HomeView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
    },
    {
      path: '/restaurants',
      name: 'restaurants',
      component: () => import('@/views/restaurant/RestaurantListView.vue'),
    },
    {
      path: '/restaurants/:id',
      name: 'restaurant-detail',
      component: () => import('@/views/restaurant/RestaurantDetailView.vue'),
    },
    {
      path: '/rankings',
      name: 'rankings',
      component: () => import('@/views/ranking/RankingView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/user/ProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('@/views/notification/NotificationView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      redirect: '/admin/users',
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/AdminUserManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/restaurants',
      name: 'admin-restaurants',
      component: () => import('@/views/admin/AdminRestaurantManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/restaurants/new',
      name: 'admin-restaurant-create',
      component: () => import('@/views/admin/RestaurantCreateView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const hasToken = !!getToken()

  // 检查是否需要登录
  if (to.meta.requiresAuth && !hasToken) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // 如果用户信息还未加载，先获取用户信息
    if (!userStore.user && hasToken) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        // 获取用户信息失败，可能是 token 过期
        ElMessage.error('用户信息加载失败，请重新登录')
        next({ name: 'login' })
        return
      }
    }

    if (!userStore.isAdmin) {
      ElMessage.error('无权访问，仅管理员可访问此页面')
      next({ name: 'home' })
      return
    }
  }

  // 已登录用户访问登录/注册页时，重定向到首页
  if (hasToken && (to.name === 'login' || to.name === 'register')) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
