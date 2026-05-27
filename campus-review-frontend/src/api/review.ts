import { get, post } from './request'
import type { ReviewDTO, ReviewCreateRequest } from '@/types'

/**
 * 发布评价
 */
export function createReview(data: ReviewCreateRequest) {
  return post<ReviewDTO>('/api/reviews', data)
}

/**
 * 查询餐馆的评价列表
 */
export function getReviewsByRestaurant(restaurantId: number) {
  return get<ReviewDTO[]>(`/api/reviews`, { params: { restaurantId } })
}

/**
 * 查询我的评价
 */
export function getMyReviews() {
  return get<ReviewDTO[]>('/api/reviews/me')
}
