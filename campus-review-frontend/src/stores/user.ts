import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getCurrentUser } from '@/api/user'
import type { LoginRequest, RegisterRequest, UserDTO } from '@/types'
import { setToken, removeToken, setUserId, getUserId, getToken, setRefreshToken } from '@/utils/storage'
import { getLogger } from '@/utils/logger'

const logger = getLogger('stores-user')

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null)
  const user = ref<UserDTO | null>(null)
  const userId = ref<number | undefined>(getUserId())

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  /**
   * 登录
   */
  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    const { token: newToken, userId: newUserId, refreshToken } = res.data

    token.value = newToken
    userId.value = newUserId
    setToken(newToken)
    setUserId(newUserId)

    // 存储 Refresh Token
    if (refreshToken) {
      setRefreshToken(refreshToken)
    }

    // 获取用户信息
    await fetchUserInfo()

    return res
  }

  /**
   * 注册
   */
  async function register(data: RegisterRequest) {
    const res = await registerApi(data)
    return res
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      user.value = res.data
    } catch (error) {
      logger.error('获取用户信息失败:', error)
      logout()
    }
  }

  /**
   * 登出
   */
  function logout() {
    token.value = null
    user.value = null
    userId.value = undefined
    removeToken()
  }

  /**
   * 初始化用户状态（从本地存储恢复）
   */
  async function init() {
    // 只有在本地存储有 token 时才获取用户信息
    const savedToken = getToken()
    if (savedToken) {
      token.value = savedToken
      await fetchUserInfo()
    }
  }

  return {
    // 状态
    token,
    user,
    userId,
    // 计算属性
    isAuthenticated,
    isAdmin,
    isLoggedIn,
    // 方法
    login,
    register,
    fetchUserInfo,
    logout,
    init,
  }
})
