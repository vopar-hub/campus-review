import { post } from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserDTO } from '@/types'

/**
 * 用户注册
 */
export function register(data: RegisterRequest) {
  return post<UserDTO>('/api/auth/register', data)
}

/**
 * 用户登录
 */
export function login(data: LoginRequest) {
  return post<LoginResponse>('/api/auth/login', data)
}

/**
 * 用户登出（前端只需清除 token）
 */
export function logout() {
  // 前端处理，清除本地存储
}
