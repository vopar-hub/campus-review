import { get, post } from './request'
import type { InteractRequest, InteractionCountDTO } from '@/types'

/**
 * 点赞
 */
export function like(data: InteractRequest) {
  return post<void>('/api/interactions/like', data)
}

/**
 * 取消点赞
 */
export function unlike(data: InteractRequest) {
  return post<void>('/api/interactions/unlike', data)
}

/**
 * 收藏
 */
export function favorite(data: InteractRequest) {
  return post<void>('/api/interactions/favorite', data)
}

/**
 * 取消收藏
 */
export function unfavorite(data: InteractRequest) {
  return post<void>('/api/interactions/unfavorite', data)
}

/**
 * 查询互动统计
 */
export function getInteractionCount(targetType: string, targetId: number) {
  return get<InteractionCountDTO>('/api/interactions/count', {
    params: { targetType, targetId },
  })
}
