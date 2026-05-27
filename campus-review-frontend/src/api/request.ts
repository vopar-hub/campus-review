import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'
import { getToken, removeToken, setToken, getRefreshToken } from '@/utils/storage'
import { getLogger } from '@/utils/logger'

const logger = getLogger('api-request')

// 创建业务错误类
export class BusinessError extends Error {
  code: number
  requestId: string | null

  constructor(code: number, message: string, requestId: string | null = null) {
    super(message)
    this.name = 'BusinessError'
    this.code = code
    this.requestId = requestId
  }
}

// 刷新 Token 锁和请求队列
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * 添加请求到刷新队列
 */
function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

/**
 * 执行刷新队列
 */
function onRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

/**
 * 刷新 Token
 */
async function doRefreshToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token')
  }

  try {
    const response = await axios.post(
      import.meta.env.VITE_API_BASE_URL + '/api/auth/refresh',
      { refreshToken } as { refreshToken: string },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    )
    const res = response.data as ApiResponse<{ token: string; expiresAt: string }>
    if (res.code === 0) {
      setToken(res.data.token)
      return res.data.token
    } else {
      throw new Error(res.message || 'Token refresh failed')
    }
  } catch (error) {
    logger.error('刷新 Token 失败:', error)
    removeToken()
    window.location.href = '/login'
    throw error
  }
}

/**
 * 创建 Axios 实例
 */
function createAxiosInstance(): AxiosInstance {
  const instance = axios.create({
    // 使用环境变量配置的 baseURL，默认为空字符串（相对路径）
    // nginx 会将 /api 请求代理到对应的后端服务
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 15000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // 请求拦截器
  instance.interceptors.request.use(
    (config) => {
      const token = getToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      logger.error('请求错误:', error)
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  instance.interceptors.response.use(
    (response: AxiosResponse<ApiResponse<unknown>>) => {
      const res = response.data

      // 业务错误处理
      if (res.code !== 0) {
        // 40100: 未登录或登录已过期
        if (res.code === 40100) {
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          // 跳转到登录页
          window.location.href = '/login'
        }
        // 40300: 无权限
        else if (res.code === 40300) {
          ElMessage.error('无权限访问')
        }
        // 42900: 请求过于频繁
        else if (res.code === 42900) {
          ElMessage.warning('请求过于频繁，请稍后再试')
        }
        // 其他业务错误
        else {
          ElMessage.error(res.message || '请求失败')
        }

        return Promise.reject(new BusinessError(res.code, res.message, res.requestId))
      }

      return response
    },
    async (error) => {
      logger.error('HTTP 错误:', error)

      // HTTP 状态码错误处理
      if (error.response) {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

        // 401: Token 过期，尝试刷新
        if (error.response.status === 401 && !originalRequest._retry) {
          if (isRefreshing) {
            // 如果正在刷新，将请求加入队列
            return new Promise((resolve) => {
              subscribeTokenRefresh((token: string) => {
                if (originalRequest.headers) {
                  originalRequest.headers.Authorization = `Bearer ${token}`
                }
                resolve(request(originalRequest))
              })
            })
          }

          const refreshToken = getRefreshToken()
          if (!refreshToken) {
            // 没有 Refresh Token，跳转登录
            ElMessage.error('未授权，请登录后重试')
            removeToken()
            window.location.href = '/login'
            return Promise.reject(error)
          }

          originalRequest._retry = true
          isRefreshing = true

          try {
            const newToken = await doRefreshToken()
            isRefreshing = false
            onRefreshed(newToken)

            // 重试原请求
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`
            }
            return request(originalRequest)
          } catch (refreshError) {
            isRefreshing = false
            removeToken()
            window.location.href = '/login'
            return Promise.reject(refreshError)
          }
        }

        switch (error.response.status) {
          case 400:
            ElMessage.error('请求参数错误')
            break
          case 401:
            ElMessage.error('未授权，请登录后重试')
            removeToken()
            window.location.href = '/login'
            break
          case 403:
            ElMessage.error('拒绝访问')
            break
          case 404:
            ElMessage.error('请求资源不存在')
            break
          case 500:
            ElMessage.error('服务器内部错误')
            break
          case 503:
            ElMessage.error('服务不可用')
            break
          default:
            ElMessage.error(`请求失败：${error.response.status}`)
        }
      } else if (error.request) {
        ElMessage.error('网络错误，请检查网络连接')
      } else {
        ElMessage.error(error.message || '请求失败')
      }

      return Promise.reject(error)
    }
  )

  return instance
}

// 导出 Axios 实例
export const request = createAxiosInstance()

/**
 * 封装 GET 请求
 */
export function get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.get(url, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 POST 请求
 */
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.post(url, data, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 PUT 请求
 */
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.put(url, data, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 DELETE 请求
 */
export function del<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.delete(url, config).then((res) => res.data as ApiResponse<T>)
}

export default request
