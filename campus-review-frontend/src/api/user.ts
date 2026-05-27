import { get, put } from './request'
import type { UserDTO, UpdateProfileRequest } from '@/types'

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return get<UserDTO>('/api/users/me')
}

/**
 * 更新用户资料
 */
export function updateProfile(data: UpdateProfileRequest) {
  return put<UserDTO>('/api/users/me', data)
}
