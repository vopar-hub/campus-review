/**
 * 评价信息
 */
export interface ReviewDTO {
  id: number
  restaurantId: number
  userId: number
  rating: number
  content: string
  status: string
  createdAt: string
}

/**
 * 创建评价请求
 */
export interface ReviewCreateRequest {
  restaurantId: number
  rating: number
  content: string
}

/**
 * 评价状态枚举
 */
export enum ReviewStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}
