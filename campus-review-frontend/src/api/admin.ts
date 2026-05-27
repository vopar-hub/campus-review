import { get, post, del } from './admin-request'
import type { PendingReviewDTO, UserDTO, RestaurantDTO } from '@/types'

/**
 * 获取待审核评价列表
 */
export function getPendingReviews() {
  return get<PendingReviewDTO[]>('/api/admin/reviews/pending')
}

/**
 * 通过评价
 */
export function approveReview(id: number) {
  return post<null>(`/api/admin/reviews/${id}/approve`)
}

/**
 * 驳回评价
 */
export function rejectReview(id: number) {
  return post<null>(`/api/admin/reviews/${id}/reject`)
}

/**
 * 获取用户列表
 */
export function getUserList() {
  return get<UserDTO[]>('/api/admin/users')
}

/**
 * 封禁用户
 */
export function banUser(id: number) {
  return post<null>(`/api/admin/users/${id}/ban`)
}

/**
 * 解封用户
 */
export function unbanUser(id: number) {
  return post<null>(`/api/admin/users/${id}/unban`)
}

/**
 * 获取餐厅列表
 */
export function getAdminRestaurants() {
  return get<RestaurantDTO[]>('/api/admin/restaurants')
}

/**
 * 删除餐厅
 */
export function deleteRestaurant(id: number) {
  return del<null>(`/api/admin/restaurants/${id}`)
}
