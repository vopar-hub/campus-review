import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'
import { getToken, removeToken, getUserId } from '@/utils/storage'
import { getLogger } from '@/utils/logger'

const logger = getLogger('api-admin')

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

/**
 * 创建管理端专用的 Axios 实例
 * 使用相对路径，nginx 会将 /api/admin 请求代理到管理端网关（8002）
 */
function createAdminAxiosInstance(): AxiosInstance {
  const instance = axios.create({
    // 使用环境变量配置的 baseURL，默认为空字符串（相对路径）
    // nginx 会将 /api/admin 请求代理到管理端网关
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 15000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // 请求拦截器 - 添加管理端所需的身份头
  instance.interceptors.request.use(
    (config) => {
      const token = getToken()
      const userId = getUserId()

      // 添加 JWT Token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }

      // 添加管理端所需的身份头
      if (userId) {
        config.headers['X-User-Id'] = String(userId)
      }
      // 注意：roles 需要由调用方通过 config.headers 传入，或从 token 中解析
      // 默认使用 USER 角色
      if (!config.headers['X-User-Roles']) {
        config.headers['X-User-Roles'] = 'USER'
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

        return Promise.reject(new BusinessError(res.code, res.message, res.requestId || null))
      }

      return response
    },
    (error) => {
      logger.error('HTTP 错误:', error)

      // HTTP 状态码错误处理
      if (error.response) {
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

// 导出管理端 Axios 实例
const adminRequest = createAdminAxiosInstance()

/**
 * 封装 GET 请求
 */
export function get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return adminRequest.get(url, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 POST 请求
 */
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return adminRequest.post(url, data, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 DELETE 请求
 */
export function del<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return adminRequest.delete(url, config).then((res) => res.data as ApiResponse<T>)
}

/**
 * 封装 PUT 请求
 */
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return adminRequest.put(url, data, config).then((res) => res.data as ApiResponse<T>)
}

export default adminRequest
