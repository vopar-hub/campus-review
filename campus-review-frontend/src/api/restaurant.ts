import { get, post } from './request'
import type { RestaurantDTO, RestaurantCreateRequest, RestaurantSearchParams } from '@/types'

/**
 * 创建餐馆（管理员）
 */
export function createRestaurant(data: RestaurantCreateRequest) {
  return post<RestaurantDTO>('/api/admin/restaurants', data)
}

/**
 * 查询餐馆详情
 */
export function getRestaurant(id: number) {
  return get<RestaurantDTO>(`/api/restaurants/${id}`)
}

/**
 * 搜索餐馆
 */
export function searchRestaurants(params?: RestaurantSearchParams) {
  return get<RestaurantDTO[]>('/api/restaurants', { params })
}

/**
 * 批量查询餐馆
 */
export function getRestaurantsByIds(ids: number[]) {
  return get<RestaurantDTO[]>('/api/restaurants/by-ids', {
    params: { ids: ids.join(',') },
  })
}

/**
 * 创建餐馆(带图片)（管理员）
 */
export function createRestaurantWithImage(data: {
  name: string
  campus: string
  address?: string
  description?: string
  file?: File
}) {
  const formData = new FormData()
  formData.append('name', data.name)
  formData.append('campus', data.campus)
  if (data.address) formData.append('address', data.address)
  if (data.description) formData.append('description', data.description)
  if (data.file) formData.append('file', data.file)

  return post<RestaurantDTO>('/api/admin/restaurants/with-image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
