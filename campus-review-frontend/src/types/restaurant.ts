/**
 * 餐馆信息
 */
export interface RestaurantDTO {
  id: number
  name: string
  campus: string
  address: string
  description: string
  coverImageUrl: string
  createdAt: string
}

/**
 * 创建餐馆请求
 */
export interface RestaurantCreateRequest {
  name: string
  campus: string
  address: string
  description: string
  coverImageUrl?: string
}

/**
 * 餐馆搜索参数
 */
export interface RestaurantSearchParams {
  name?: string
  campus?: string
}
